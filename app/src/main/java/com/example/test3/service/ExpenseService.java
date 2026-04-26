package com.example.test3.service;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.test3.dao.ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID;
import static com.example.test3.dao.ExpenseSQLite.DEPOSIT_EXPENSE_ID;
import static com.example.test3.dao.ExpenseSQLite.DEPOSIT_IS_DELETED;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_DATETIME;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_DESCRIPTION;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_EXPENSE_TYPE_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_IS_DELETED;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_NAME;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_REFUND_EXPENSE_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_REFUND_MONTH_COUNT;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_REFUND_START_DATE;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_ROW_COLOR;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_PAYMENT;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_PAYMENT_EXPENSE_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_PAYMENT_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_TYPE_ID;
import static com.example.test3.dao.ExpenseSQLite.TABLE_DEPOSIT;
import static com.example.test3.dao.ExpenseSQLite.TABLE_EXPENSE;
import static com.example.test3.dao.ExpenseSQLite.TABLE_EXPENSE_PAYMENT;
import static com.example.test3.dao.ExpenseSQLite.TABLE_EXPENSE_REFUND;
import static com.example.test3.service.DepositService.TYPE_CREDIT_LOAN_REPAYMENT;
import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_PLANNED_REFUND_PLANNING;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.test3.dao.ExpenseSQLite;
import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.Expense;
import com.example.test3.monthly.expense.refund.planning.ExpenseRefund;
import com.example.test3.util.Util;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpenseService {

    private static final String TAG = "ExpenseService";

    private Context context;

    private ExpenseSQLite dbHelper;

    private SQLiteDatabase dbWrite;

    private SQLiteDatabase dbRead;



    public ExpenseService(Context baseContext) {
        this.context = baseContext;
        this.dbHelper = new ExpenseSQLite(this.context);
        this.dbWrite = dbHelper.getWritableDatabase();
        this.dbRead = dbHelper.getReadableDatabase();
    }


    /** ExpenseList только ежемесячных расходов */
    public ArrayList<Expense> getExpenseList() {
        return getExpenseList(1L);
    }


    public ArrayList<Expense> getExpenseList(Long typeId) {

        ArrayList<Expense> expenseList = getExpenseRowList(typeId);

        setExpenseListPayments(expenseList);

        return expenseList;
    }


    public ArrayList<Expense> getExpenseRowList(Long typeId) {

        ArrayList<Expense> expenseList = new ArrayList<>();

        Cursor query = dbRead.rawQuery(
                "SELECT * FROM " + TABLE_EXPENSE + " " +
                " where " + EXPENSE_EXPENSE_TYPE_ID + " = " + typeId.toString() + " " +
                " order by " + EXPENSE_ID + " desc;",
                null
        );

        while(query.moveToNext()){

            Long expenseId = query.getLong(0);
            Long returnTypeId = query.getLong(1);
            String expenseName = query.getString(2);
            String expenseDescription = query.getString(3);
            String expenseDateTime = query.getString(4);
            int expenseIsDelete = query.getInt(5);
            int expenseRowColor = query.getInt(6);                                                /** Визуальное оформление позднее выделить в отдельную таблицу, со связью по expenseId */

            Expense expense = new Expense(expenseId, returnTypeId, expenseName, expenseDescription,
                    ZonedDateTime.parse(expenseDateTime, Util.dateFormatterInsert), (expenseIsDelete == 1),
                    expenseRowColor);

            expenseList.add(expense);

        }

        query.close();


        return expenseList;                                                                         /* Cursor cursor = db.rawQuery("Select *" +  " FROM "+ TABLE_USERS + " WHERE " + USER_ID + " = " + number, null); */
    }


    /**
     * Получает расход по его ID
     * @param id ID расхода
     * @return объект Expense или null, если не найден
     */
    public Expense getExpenseById(long id) {

        Cursor query = dbRead.rawQuery(
                "SELECT * FROM " + TABLE_EXPENSE + " " +
                        " WHERE " + EXPENSE_ID + " = " + id,
                null
        );

        Expense expense = null;

        if (query.moveToFirst()) {
            Long expenseId = query.getLong(0);
            Long returnTypeId = query.getLong(1);
            String expenseName = query.getString(2);
            String expenseDescription = query.getString(3);
            String expenseDateTime = query.getString(4);
            int expenseIsDelete = query.getInt(5);
            int expenseRowColor = query.getInt(6);

            expense = new Expense(expenseId, returnTypeId, expenseName, expenseDescription,
                    ZonedDateTime.parse(expenseDateTime, Util.dateFormatterInsert),
                    (expenseIsDelete == 1), expenseRowColor);


            /** Загружает платежи для этого расхода */
            setExpensePayments(expense);
        }

        query.close();

        return expense;
    }


    public void setExpenseListPayments(ArrayList<Expense> expenseList) {
        for (Expense expense : expenseList) setExpensePayments(expense);
    }

    public void setExpensePayments(Expense expense) {

        Cursor query = dbRead.rawQuery(
                "SELECT * FROM " + TABLE_EXPENSE_PAYMENT +
                    " where " + EXPENSE_PAYMENT_EXPENSE_ID + " = " + expense.getId() +
                    " order by " + EXPENSE_ID + " asc;" ,
                null);

        while(query.moveToNext()){
            Double payment = query.getDouble(2);                                                  /* BigDecimal payment = BigDecimal.valueOf(query.getDouble(2)); */
            expense.addPayment(payment);
        }

        query.close();
    }


    /** Переписать реализацию на ручной commit и дополнить структуру таблицы Payment foreign key */
    public boolean insertExpense(Expense expense) {

        Expense insertedExpense = insertExpenseRow(expense);

        /*
        dbWrite.beginTransaction();
        try {

            dbWrite.execSQL("INSERT INTO " + TABLE_EXPENSE + " (" + EXPENSE_NAME + ", " + EXPENSE_DATETIME + ", " + EXPENSE_IS_DELETED + ") VALUES ('value1', 'value2', 'value3') RETURNING id;");
            dbWrite.execSQL("INSERT INTO " + TABLE_PAYMENT + "...");

            dbWrite.setTransactionSuccessful();
        } finally {
            dbWrite.endTransaction();
        }
        */

        return insertedExpense.getId() != -1;
    }


    public Expense insertExpenseRow(Expense expense) {

        ContentValues cv = new ContentValues();


        if (expense.getTypeId() == null) {throw new IllegalArgumentException("TypeId не установлен для Expense: " + expense.getName());}
        cv.put(EXPENSE_EXPENSE_TYPE_ID, expense.getTypeId());

        cv.put(EXPENSE_NAME, expense.getName());
        if (expense.getDescription() != null) cv.put(EXPENSE_DESCRIPTION, expense.getDescription());
        cv.put(EXPENSE_DATETIME, expense.getDateTime().format(Util.dateFormatterInsert));
        cv.put(EXPENSE_IS_DELETED, 0);
        cv.put(EXPENSE_ROW_COLOR, -1);

        long result = dbWrite.insert(TABLE_EXPENSE, null, cv);                                      //    dbWrite.execSQL("INSERT OR IGNORE INTO TABLE_EXPENSE VALUES ('Coffee', 23);");            //  return false;

        expense.setId(result);

        if(expense.getExpenseList() != null) insertPaymentRow(expense);

        return expense;
    }


    public boolean insertPaymentRow(Expense expense) {

        ContentValues cv = new ContentValues();
        cv.put(EXPENSE_PAYMENT_EXPENSE_ID, expense.getId());
        cv.put(EXPENSE_PAYMENT, expense.getExpenseList().get(0));                                           /** Забираем первый элемент, т.к. при создании м.б. не более одного элемента */

        long result = dbWrite.insert(TABLE_EXPENSE_PAYMENT, null, cv);

        return result != -1;
    }


    /** Для ExpenseRefund : */
    @Deprecated
    public boolean insertExpense(ExpenseRefund expense) {
        Log.d(TAG, "insertExpense() startMethod, ExpenseRefund: " + expense);
        ExpenseRefund insertedExpense = insertExpenseRow(expense);
        return insertedExpense.getId() != -1;
    }


    public ExpenseRefund insertExpenseRow(ExpenseRefund expense) {
        Log.d(TAG, "insertExpenseRow() startMethod, ExpenseRefund: " + expense);

        /** Поля из Expense : */
        ContentValues cv = new ContentValues();


        if (expense.getTypeId() == null) {throw new IllegalArgumentException("TypeId не установлен для Expense: " + expense.getName());}
        if (expense.getTypeId() != Util.TYPE_EXPENSE_MONTH_REFUND_PLANNING) {throw new IllegalArgumentException("Получен некорректный TypeId: " + expense.getTypeId());}
        cv.put(EXPENSE_EXPENSE_TYPE_ID, expense.getTypeId());

        cv.put(EXPENSE_NAME, expense.getName());
        if (expense.getDescription() != null) cv.put(EXPENSE_DESCRIPTION, expense.getDescription());
        cv.put(EXPENSE_DATETIME, expense.getDateTime().format(Util.dateFormatterInsert));
        cv.put(EXPENSE_IS_DELETED, 0);
        cv.put(EXPENSE_ROW_COLOR, -1);

        long result = dbWrite.insert(TABLE_EXPENSE, null, cv);                                      //    dbWrite.execSQL("INSERT OR IGNORE INTO TABLE_EXPENSE VALUES ('Coffee', 23);");            //  return false;

        expense.setId(result);

        if(expense.getExpenseList() != null) insertPaymentRow(expense);
        /** !Поля из Expense */


        /** Поля из ExpenseRefund : */
        boolean isSuccess = insertExpenseRefundPropsRow(expense);

        return expense;
    }


    public boolean insertExpenseRefundPropsRow(ExpenseRefund expense) {
        Log.d(TAG, "insertExpenseRefundPropsRow() startMethod, ExpenseRefund: " + expense);

        ContentValues expenseRefundProps = new ContentValues();
        expenseRefundProps.put(EXPENSE_REFUND_EXPENSE_ID, expense.getId());
        expenseRefundProps.put(EXPENSE_REFUND_START_DATE, expense.getStartDate().format(Util.dateFormatterInsert));
        expenseRefundProps.put(EXPENSE_REFUND_MONTH_COUNT, expense.getMonthCount());
        long result = dbWrite.insert(TABLE_EXPENSE_REFUND, null, expenseRefundProps);

        return result != -1;
    }


    /** DS : */
    // todo: переписать с вызовом getExpenseList(Long typeId)
    /** Получает список ExpenseRefund (тип TYPE_EXPENSE_MONTH_REFUND_PLANNING) */
    public List<ExpenseRefund> getExpenseRefundList() {
        Log.d(TAG, "getExpenseRefundList() start");

        List<ExpenseRefund> refunds = new ArrayList<>();
        Cursor cursor = null;

        try {

            cursor = dbRead.rawQuery(
                    "SELECT * FROM " + TABLE_EXPENSE +
                            " WHERE " + EXPENSE_EXPENSE_TYPE_ID + " = " + Util.TYPE_EXPENSE_MONTH_REFUND_PLANNING +
                            " AND " + EXPENSE_IS_DELETED + " = 0" +
                            " ORDER BY " + EXPENSE_DATETIME + " DESC", null);

            while (cursor.moveToNext()) {

                long id = cursor.getLong(cursor.getColumnIndexOrThrow(EXPENSE_ID));
                long typeId = cursor.getLong(cursor.getColumnIndexOrThrow(EXPENSE_EXPENSE_TYPE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(EXPENSE_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(EXPENSE_DESCRIPTION));
                String dateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(EXPENSE_DATETIME));
                boolean isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(EXPENSE_IS_DELETED)) == 1;
                int rowColor = cursor.getInt(cursor.getColumnIndexOrThrow(EXPENSE_ROW_COLOR));
                ZonedDateTime dateTime = ZonedDateTime.parse(dateTimeStr, Util.dateFormatterInsert);

                ExpenseRefund expense = new ExpenseRefund(id, typeId, name, description, dateTime, isDeleted, rowColor);

                // Загружаем детали из expense_refund
                loadExpenseRefundDetails(expense);

                // Загружаем платежи самого расхода (если есть)
                setExpensePayments(expense);

                refunds.add(expense);
            }

        } finally {
            if (cursor != null) cursor.close();
        }

        Log.d(TAG, "getExpenseRefundList() end, size=" + refunds.size());
        return refunds;
    }

    private void loadExpenseRefundDetails(ExpenseRefund expense) {
        Log.d(TAG, "loadExpenseRefundDetails() start for expenseId=" + expense.getId());

        Cursor cursor = null;

        try {

            cursor = dbRead.rawQuery(
                    "SELECT " + EXPENSE_REFUND_START_DATE + ", " + EXPENSE_REFUND_MONTH_COUNT +
                            " FROM " + TABLE_EXPENSE_REFUND +
                            " WHERE " + EXPENSE_REFUND_EXPENSE_ID + " = " + expense.getId(), null);

            if (cursor.moveToFirst()) {

                String startDateStr = cursor.getString(0);
                if (startDateStr != null && !startDateStr.isEmpty()) {
                    expense.setStartDate(ZonedDateTime.parse(startDateStr, Util.dateFormatterInsert));
                }

                expense.setMonthCount(cursor.getInt(1));
            }

        } finally {
            if (cursor != null) cursor.close();
        }

        Log.d(TAG, "loadExpenseRefundDetails() end");
    }
    /** !DS */
    /** !Для ExpenseRefund */


    public boolean removeExpense(Expense expense) {


//        long result = dbWrite.delete(TABLE_EXPENSE,
//                EXPENSE_ID + " = " + expense.getId().toString(),
//                null /*new String {"name"} */);


        long result = 0;

        try {

            dbWrite.beginTransaction();

            /** Удаляет все связанные платежи из expense_payment для переданной expense */
            dbWrite.delete(TABLE_EXPENSE_PAYMENT, "expense_id = ?",
                    new String[]{String.valueOf(expense.getId().toString())});

            result = dbWrite.delete(TABLE_EXPENSE, EXPENSE_ID + " = ?",
                    new String[]{String.valueOf(expense.getId().toString())});

            dbWrite.setTransactionSuccessful();
            Log.d("ExpenseService", "Удалено записей: " + result);

        } catch (Exception e) {
            Log.e("ExpenseService", "Ошибка при удалении расхода expense: ".concat(expense.toString()), e);
        } finally {
            dbWrite.endTransaction();
        }

        return result != -1;
    }


    public boolean removeExpenseRefundRow(ExpenseRefund expense) {
        Log.d(TAG, "removeExpenseRefundRow() startMethod, ExpenseRefund: " + expense);

        long result = 0;

        try {

            /** Удаляет все связанные платежи из expense_payment для переданной expense */
            dbWrite.delete(TABLE_EXPENSE_REFUND, EXPENSE_REFUND_EXPENSE_ID + " = ?",
                    new String[]{String.valueOf(expense.getId().toString())});

            Log.d(TAG, "removeExpenseRefundRow() удалено записей: " + result);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении расхода ExpenseRefund: ".concat(expense.toString()), e);
        }

        return result != -1;
    }


    public boolean addPaymentToExpense(Expense expense, double payment) {

        if (expense.getId() == null) {
            Log.e(TAG, "Cannot add payment to expense without ID");
            return false;
        }

        SQLiteDatabase db = null;

        try {

            db = dbHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(EXPENSE_PAYMENT_EXPENSE_ID, expense.getId());
            cv.put(EXPENSE_PAYMENT, payment);

            long result = db.insert(TABLE_EXPENSE_PAYMENT, null, cv);

            if (result != -1) {
                /** Обновляет объект в памяти */
                expense.addPayment(payment);
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error adding payment", e);
        }

        return false;
    }


    public boolean updatePayment(Expense expense, int paymentIndex, double newPayment) {

        Long paymentId = getPaymentId(expense.getId(), paymentIndex);
        if (paymentId == null) return false;

        ContentValues cv = new ContentValues();
        cv.put(EXPENSE_PAYMENT, newPayment);

        int result = dbWrite.update(TABLE_EXPENSE_PAYMENT, cv,
                EXPENSE_PAYMENT_ID + " = ?",
                new String[]{String.valueOf(paymentId)});

        if (result > 0) {
            expense.getExpenseList().set(paymentIndex, newPayment);
            return true;
        }
        return false;
    }

    public boolean deletePayment(Expense expense, int paymentIndex) {

        Long paymentId = getPaymentId(expense.getId(), paymentIndex);
        if (paymentId == null) return false;

        int result = dbWrite.delete(TABLE_EXPENSE_PAYMENT,
                EXPENSE_PAYMENT_ID + " = ?",
                new String[]{String.valueOf(paymentId)});

        if (result > 0) {
            expense.getExpenseList().remove(paymentIndex);
            return true;
        }
        return false;
    }


    private Long getPaymentId(long expenseId, int index) {
        Cursor cursor = dbRead.rawQuery(
                "SELECT " + EXPENSE_PAYMENT_ID + " FROM " + TABLE_EXPENSE_PAYMENT +
                        " WHERE " + EXPENSE_PAYMENT_EXPENSE_ID + " = " + expenseId +
                        " ORDER BY " + EXPENSE_PAYMENT_ID + " ASC LIMIT 1 OFFSET " + index,
                null);

        Long paymentId = null;
        if (cursor.moveToFirst()) {
            paymentId = cursor.getLong(0);
        }
        cursor.close();
        return paymentId;
    }


    /** Обновляет цвет текста строки */
    public boolean updateExpenseRowColor(Expense expense) {
        ContentValues cv = new ContentValues();
        cv.put(EXPENSE_ROW_COLOR, expense.getRowColor());

        int result = dbWrite.update(TABLE_EXPENSE, cv,
                EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expense.getId())});

        return result > 0;
    }


    /**
     * Обновляет описание Expense
     * @param expense расходная операуия с обновлённым описанием
     * @return true если успешно, false в противном случае
     */
    public boolean updateExpenseDescription(Expense expense) {

        ContentValues cv = new ContentValues();
        cv.put(EXPENSE_DESCRIPTION, expense.getDescription());

        int result = dbWrite.update(TABLE_EXPENSE, cv,
                EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expense.getId())});

        Log.d(TAG, "Результат сохранения описания: " + result);

        return result > 0;
    }


    public double getCurrentLoanAmount(Expense expense) {

        double totalPayments = expense.getExpenseListTotalAmount();


        double totalRepayments = 0.0;

        Cursor cursor = dbRead.rawQuery(
                "SELECT SUM(" + ExpenseSQLite.DEPOSIT_PAYMENT + ") " +
                        "FROM " + ExpenseSQLite.TABLE_DEPOSIT_PAYMENT + " dp " +
                        "JOIN " + TABLE_DEPOSIT + " d ON dp." + ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID + " = d." + ExpenseSQLite.DEPOSIT_ID + " " +
                        "WHERE d." + DEPOSIT_DEPOSIT_TYPE_ID + " = " + TYPE_CREDIT_LOAN_REPAYMENT + " " +
                        "AND d." + DEPOSIT_EXPENSE_ID + " = " + expense.getId(),
                null);

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            totalRepayments = cursor.getDouble(0);
        }
        cursor.close();

        return totalPayments - totalRepayments;
    }


}
