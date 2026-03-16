package com.example.test3.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.test3.dao.ExpenseSQLite;
import com.example.test3.deposit.Deposit;
import com.example.test3.month.Month;
import com.example.test3.util.Util;

import java.util.ArrayList;
import java.util.List;

public class DepositService {

    private static final String TAG = "DepositService";

    public static Long TYPE_MONTHLY_DEPOSIT = 1L;                                                   /** Погашение ежемесячных затрат */

    private Context context;
    private ExpenseSQLite dbHelper;
    private SQLiteDatabase dbWrite;
    private SQLiteDatabase dbRead;
    private MonthService monthService;


    public DepositService(Context context) {
        this.context = context;
        this.dbHelper = new ExpenseSQLite(context);
        this.dbWrite = dbHelper.getWritableDatabase();
        this.dbRead = dbHelper.getReadableDatabase();
        this.monthService = new MonthService(context);
    }


    /**
     * Вставляет новый deposit в БД
     * @param deposit объект Deposit для вставки
     * @return ID вставленной записи или -1 в случае ошибки
     */
    public long insertDeposit(Deposit deposit) {

        Log.d(TAG, "insertDeposit: " + deposit.getName() + ", typeId=" + deposit.getTypeId());

        ContentValues cv = new ContentValues();

        /** Заполняет ContentValues из объекта Deposit */
        if (deposit.getExpenseId() != null) {
            cv.put(ExpenseSQLite.DEPOSIT_EXPENSE_ID, deposit.getExpenseId());
        }

        cv.put(ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID, deposit.getTypeId());
        cv.put(ExpenseSQLite.DEPOSIT_NAME, deposit.getName());

        if (deposit.getDescription() != null) {
            cv.put(ExpenseSQLite.DEPOSIT_DESCRIPTION, deposit.getDescription());
        }

        if (deposit.getDateTime() != null) {
            cv.put(ExpenseSQLite.DEPOSIT_DATETIME,
                    deposit.getDateTime().format(Util.dateFormatterInsert));
        } else {
            cv.put(ExpenseSQLite.DEPOSIT_DATETIME,
                    java.time.ZonedDateTime.now().format(Util.dateFormatterInsert));
        }

        cv.put(ExpenseSQLite.DEPOSIT_IS_DELETED, deposit.isDeleted() ? 1 : 0);

        if (deposit.getRowColor() != null) {
            cv.put(ExpenseSQLite.DEPOSIT_ROW_COLOR, deposit.getRowColor());
        } else {
            cv.put(ExpenseSQLite.DEPOSIT_ROW_COLOR, -1);
        }

        /** Сохраняет запись */
        long id = dbWrite.insert(ExpenseSQLite.TABLE_DEPOSIT, null, cv);

        if (id == -1) {
            Log.e(TAG, "Ошибка при вставке deposit");
            return -1;
        }

        Log.d(TAG, "Deposit вставлен с ID: " + id);
        deposit.setId(id);

        /** Если у взноса (с type=1, погашение ежемесячных затрат) expenseId не установлен,
         * определяет Month по дате взноса */
        if (deposit.getTypeId() == TYPE_MONTHLY_DEPOSIT && deposit.getExpenseId() == null) {

            /** Получает дату из deposit */
            String dateStr;
            if (deposit.getDateTime() != null) {
                dateStr = deposit.getDateTime().format(Util.dateFormatterInsert);
            } else {
                dateStr = java.time.ZonedDateTime.now().format(Util.dateFormatterInsert);
            }


            /** Извлекает месяц и год из даты (формат "dd.MM.yy") */
            try {

                int month = Integer.parseInt(dateStr.substring(3, 5));
                int year = 2000 + Integer.parseInt(dateStr.substring(6, 8));

                Log.d(TAG, "Для deposit type=1 определяем месяц: " + month + "/" + year);

                /** Получает или создаём объект месяца */
                Month monthObj = monthService.getOrCreateMonth(year, month);

                if (monthObj != null && monthObj.getId() != null) {

                    /** Обновляет deposit, устанавливая expenseId = ID месяца */
                    ContentValues updateCv = new ContentValues();
                    updateCv.put(ExpenseSQLite.DEPOSIT_EXPENSE_ID, monthObj.getId());

                    int updated = dbWrite.update(
                            ExpenseSQLite.TABLE_DEPOSIT,
                            updateCv,
                            ExpenseSQLite.DEPOSIT_ID + " = ?",
                            new String[]{String.valueOf(id)}
                    );

                    if (updated > 0) {
                        Log.d(TAG, "Deposit ID = " + id + " связан с месяцем ID = " +
                                monthObj.getId() + " (" + monthObj.getMonthYear() + ")");
                        deposit.setExpenseId(monthObj.getId());
                    } else {
                        Log.e(TAG, "Не удалось обновить deposit с monthId");
                    }

                } else {
                    Log.e(TAG, "Не удалось получить/создать месяц для " + month + "/" + year);
                }

            } catch (Exception e) {
                Log.e(TAG, "Ошибка при парсинге даты: " + dateStr, e);
            }

        }


        /** Если есть платежи, вставляет их */
        if (deposit.getPayments() != null && !deposit.getPayments().isEmpty()) {
            insertDepositPayments(deposit);
        }

        return id;
    }


    /**
     * Обновляет конкретный платеж взноса
     * @param deposit объект Deposit
     * @param paymentIndex индекс платежа
     * @param newPayment новая сумма
     * @return true если успешно
     */
    public boolean updateDepositPayment(Deposit deposit, int paymentIndex, double newPayment) {

        Long paymentId = getDepositPaymentId(deposit.getId(), paymentIndex);
        if (paymentId == null) return false;

        ContentValues cv = new ContentValues();
        cv.put(ExpenseSQLite.DEPOSIT_PAYMENT, newPayment);

        int result = dbWrite.update(
                ExpenseSQLite.TABLE_DEPOSIT_PAYMENT,
                cv,
                ExpenseSQLite.DEPOSIT_PAYMENT_ID + " = ?",
                new String[]{String.valueOf(paymentId)}
        );

        if (result > 0) {
            /** Обновляет объект в памяти */
            deposit.getPayments().set(paymentIndex, newPayment);
            return true;
        }

        return false;
    }


    /**
     * Удаляет конкретный платеж взноса
     * @param deposit объект Deposit
     * @param paymentIndex индекс платежа
     * @return true если успешно
     */
    public boolean deleteDepositPayment(Deposit deposit, int paymentIndex) {

        Long paymentId = getDepositPaymentId(deposit.getId(), paymentIndex);
        if (paymentId == null) return false;

        int result = dbWrite.delete(
                ExpenseSQLite.TABLE_DEPOSIT_PAYMENT,
                ExpenseSQLite.DEPOSIT_PAYMENT_ID + " = ?",
                new String[]{String.valueOf(paymentId)}
        );

        if (result > 0) {
            /** Удаляет из объекта в памяти */
            deposit.getPayments().remove(paymentIndex);
            return true;
        }

        return false;
    }


    /** Получает ID платежа по индексу */
    private Long getDepositPaymentId(long depositId, int index) {
        Cursor cursor = dbRead.rawQuery(
                "SELECT " + ExpenseSQLite.DEPOSIT_PAYMENT_ID +
                        " FROM " + ExpenseSQLite.TABLE_DEPOSIT_PAYMENT +
                        " WHERE " + ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID + " = " + depositId +
                        " ORDER BY " + ExpenseSQLite.DEPOSIT_PAYMENT_ID + " ASC LIMIT 1 OFFSET " + index,
                null);

        Long paymentId = null;
        if (cursor.moveToFirst()) {
            paymentId = cursor.getLong(0);
        }
        cursor.close();
        return paymentId;
    }


    /**
     * Добавляет новый платеж к существующему взносу
     * @param deposit объект Deposit
     * @param payment сумма платежа
     * @return true если успешно
     */
    public boolean addPaymentToDeposit(Deposit deposit, double payment) {

        if (deposit.getId() == null) {
            Log.e(TAG, "Нельзя добавить платеж: deposit без ID");
            return false;
        }

        ContentValues cv = new ContentValues();
        cv.put(ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID, deposit.getId());
        cv.put(ExpenseSQLite.DEPOSIT_PAYMENT, payment);

        long result = dbWrite.insert(ExpenseSQLite.TABLE_DEPOSIT_PAYMENT, null, cv);

        if (result != -1) {
            deposit.addPayment(payment);
            return true;
        }

        return false;
    }


    /**
     * Вставляет платежи для deposit
     * @param deposit объект Deposit с платежами
     */
    private void insertDepositPayments(Deposit deposit) {

        if (deposit.getId() == null) {
            Log.e(TAG, "Нельзя вставить платежи: deposit без ID");
            return;
        }

        for (Double payment : deposit.getPayments()) {
            ContentValues cv = new ContentValues();
            cv.put(ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID, deposit.getId());
            cv.put(ExpenseSQLite.DEPOSIT_PAYMENT, payment);

            long paymentId = dbWrite.insert(ExpenseSQLite.TABLE_DEPOSIT_PAYMENT, null, cv);

            if (paymentId != -1) {
                Log.d(TAG, "Платёж " + payment + " вставлен с ID: " + paymentId);
            } else {
                Log.e(TAG, "Ошибка при вставке платежа " + payment);
            }

        }

    }


    /**
     * Получает deposit по ID
     * @param id ID deposit
     * @return объект Deposit или null
     */
    public Deposit getDepositById(long id) {

        Cursor cursor = dbRead.rawQuery(
                "SELECT * FROM " + ExpenseSQLite.TABLE_DEPOSIT +
                        " WHERE " + ExpenseSQLite.DEPOSIT_ID + " = " + id,
                null);

        Deposit deposit = null;

        if (cursor.moveToFirst()) {
            deposit = cursorToDeposit(cursor);
            loadPaymentsForDeposit(deposit);
        }
        cursor.close();

        return deposit;
    }


    /**
     * Получает все deposit по типу
     * @param typeId тип взноса
     * @return список Deposit
     */
    public List<Deposit> getDepositsByType(Long typeId) {

        List<Deposit> deposits = new ArrayList<>();

        Cursor cursor = dbRead.rawQuery(
                "SELECT * FROM " + ExpenseSQLite.TABLE_DEPOSIT +
                        " WHERE " + ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID + " = " + typeId +
                        " AND " + ExpenseSQLite.DEPOSIT_IS_DELETED + " = 0" +
                        " ORDER BY " + ExpenseSQLite.DEPOSIT_DATETIME + " DESC",
                null);

        while (cursor.moveToNext()) {
            Deposit deposit = cursorToDeposit(cursor);
            loadPaymentsForDeposit(deposit);
            deposits.add(deposit);
        }

        cursor.close();

        return deposits;
    }


    /**
     * Получает все deposit
     * @return список всех Deposit
     */
    public List<Deposit> getAllDeposits() {

        List<Deposit> deposits = new ArrayList<>();

        Cursor cursor = dbRead.rawQuery(
                "SELECT * FROM " + ExpenseSQLite.TABLE_DEPOSIT +
                        " WHERE " + ExpenseSQLite.DEPOSIT_IS_DELETED + " = 0" +
                        " ORDER BY " + ExpenseSQLite.DEPOSIT_DATETIME + " DESC",
                null);

        while (cursor.moveToNext()) {
            Deposit deposit = cursorToDeposit(cursor);
            loadPaymentsForDeposit(deposit);
            deposits.add(deposit);
        }

        cursor.close();

        return deposits;
    }


    /**
     * Получает deposit для указанного месяца (type=1)
     * @param monthId ID месяца
     * @return список Deposit для этого месяца
     */
    public List<Deposit> getDepositsForMonth(long monthId) {

        List<Deposit> deposits = new ArrayList<>();

        Cursor cursor = dbRead.rawQuery(
                "SELECT * FROM " + ExpenseSQLite.TABLE_DEPOSIT +
                        " WHERE " + ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID + " = " + TYPE_MONTHLY_DEPOSIT +
                        " AND " + ExpenseSQLite.DEPOSIT_IS_DELETED + " = 0" +
                        " AND " + ExpenseSQLite.DEPOSIT_EXPENSE_ID + " = " + monthId,
                null);

        while (cursor.moveToNext()) {
            Deposit deposit = cursorToDeposit(cursor);
            loadPaymentsForDeposit(deposit);
            deposits.add(deposit);
        }

        cursor.close();

        return deposits;
    }


    /**
     * Обновляет существующий deposit
     * @param deposit объект Deposit с обновлёнными данными
     * @return true если успешно, false в противном случае
     */
    public boolean updateDeposit(Deposit deposit) {

        if (deposit.getId() == null) {
            Log.e(TAG, "Нельзя обновить deposit без ID");
            return false;
        }

        ContentValues cv = new ContentValues();

        if (deposit.getExpenseId() != null) {
            cv.put(ExpenseSQLite.DEPOSIT_EXPENSE_ID, deposit.getExpenseId());
        } else {
            cv.putNull(ExpenseSQLite.DEPOSIT_EXPENSE_ID);
        }

        cv.put(ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID, deposit.getTypeId());
        cv.put(ExpenseSQLite.DEPOSIT_NAME, deposit.getName());

        if (deposit.getDescription() != null) {
            cv.put(ExpenseSQLite.DEPOSIT_DESCRIPTION, deposit.getDescription());
        } else {
            cv.putNull(ExpenseSQLite.DEPOSIT_DESCRIPTION);
        }

        if (deposit.getDateTime() != null) {
            cv.put(ExpenseSQLite.DEPOSIT_DATETIME,
                    deposit.getDateTime().format(Util.dateFormatterInsert));
        }

        cv.put(ExpenseSQLite.DEPOSIT_IS_DELETED, deposit.isDeleted() ? 1 : 0);

        if (deposit.getRowColor() != null) {
            cv.put(ExpenseSQLite.DEPOSIT_ROW_COLOR, deposit.getRowColor());
        } else {
            cv.put(ExpenseSQLite.DEPOSIT_ROW_COLOR, -1);
        }


        dbWrite.beginTransaction();


        try {
            /** 1. Обновляет основную информацию о взносе */
            int updated = dbWrite.update(
                    ExpenseSQLite.TABLE_DEPOSIT,
                    cv,
                    ExpenseSQLite.DEPOSIT_ID + " = ?",
                    new String[]{String.valueOf(deposit.getId())}
            );

            if (updated == 0) {
                Log.e(TAG, "Не удалось обновить deposit ID=" + deposit.getId());
                return false;
            }


            /** 2. Удаляет все старые платежи */
            int deletedPayments = dbWrite.delete(
                    ExpenseSQLite.TABLE_DEPOSIT_PAYMENT,
                    ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID + " = ?",
                    new String[]{String.valueOf(deposit.getId())}
            );

            Log.d(TAG, "Удалено старых платежей: " + deletedPayments);


            /** 3. Вставляет новые платежи */
            if (deposit.getPayments() != null && !deposit.getPayments().isEmpty()) {

                for (Double payment : deposit.getPayments()) {

                    ContentValues paymentCv = new ContentValues();
                    paymentCv.put(ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID, deposit.getId());
                    paymentCv.put(ExpenseSQLite.DEPOSIT_PAYMENT, payment);

                    long paymentId = dbWrite.insert(ExpenseSQLite.TABLE_DEPOSIT_PAYMENT, null, paymentCv);

                    if (paymentId != -1) {
                        Log.d(TAG, "Вставлен платёж " + payment + " с ID=" + paymentId);
                    } else {
                        Log.e(TAG, "Ошибка при вставке платежа " + payment);
                    }

                }

            }


            dbWrite.setTransactionSuccessful();
            Log.d(TAG, "Deposit ID=" + deposit.getId() + " полностью обновлён с платежами");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении deposit ID=" + deposit.getId(), e);
            return false;
        } finally {
            dbWrite.endTransaction();
        }

    }


    /**
     * Полностью удаляет deposit и все его платежи из БД
     * @param depositId ID deposit
     * @return true если успешно
     */
    public boolean deleteDeposit(long depositId) {

        try {

            dbWrite.beginTransaction();


            /** 1. Удаляет все платежи */
            int deletedPayments = dbWrite.delete(
                    ExpenseSQLite.TABLE_DEPOSIT_PAYMENT,
                    ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID + " = ?",
                    new String[]{String.valueOf(depositId)}
            );
            Log.d(TAG, "Удалено платежей: " + deletedPayments + " для deposit ID=" + depositId);


            /** 2. Проверяет, остались ли ещё платежи */
            Cursor checkCursor = dbRead.rawQuery(
                    "SELECT COUNT(*) FROM " + ExpenseSQLite.TABLE_DEPOSIT_PAYMENT +
                            " WHERE " + ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID + " = " + depositId,
                    null);

            int remainingPayments = 0;
            if (checkCursor.moveToFirst()) {
                remainingPayments = checkCursor.getInt(0);
            }
            checkCursor.close();

            if (remainingPayments > 0) {
                Log.e(TAG, "Остались платежи: " + remainingPayments + " - удаление отменяется");
                dbWrite.endTransaction();
                return false;
            }


            /** 3. Удаляет сам взнос */
            int deleted = dbWrite.delete(
                    ExpenseSQLite.TABLE_DEPOSIT,
                    ExpenseSQLite.DEPOSIT_ID + " = ?",
                    new String[]{String.valueOf(depositId)}
            );

            if (deleted > 0) {
                dbWrite.setTransactionSuccessful();
                Log.d(TAG, "Deposit ID=" + depositId + " полностью удалён");
                return true;
            } else {
                Log.e(TAG, "Deposit ID=" + depositId + " не найден");
                dbWrite.endTransaction();
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении deposit ID=" + depositId, e);
            return false;
        } finally {
            dbWrite.endTransaction();
        }

    }


    /** Загружает платежи для конкретного Deposit */
    private void loadPaymentsForDeposit(Deposit deposit) {

        Cursor cursor = dbRead.rawQuery(
                "SELECT " + ExpenseSQLite.DEPOSIT_PAYMENT +
                        " FROM " + ExpenseSQLite.TABLE_DEPOSIT_PAYMENT +
                        " WHERE " + ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID + " = " + deposit.getId() +
                        " ORDER BY " + ExpenseSQLite.DEPOSIT_PAYMENT_ID + " ASC",
                null);


        while (cursor.moveToNext()) {
            deposit.addPayment(cursor.getDouble(0));
        }

        cursor.close();
    }


    /** Преобразует курсор в объект Deposit */
    private Deposit cursorToDeposit(Cursor cursor) {

        Deposit deposit = new Deposit();

        deposit.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_ID)));

        int expenseIdIndex = cursor.getColumnIndex(ExpenseSQLite.DEPOSIT_EXPENSE_ID);
        if (expenseIdIndex >= 0 && !cursor.isNull(expenseIdIndex)) {
            deposit.setExpenseId(cursor.getLong(expenseIdIndex));
        }

        deposit.setTypeId(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID)));
        deposit.setName(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_NAME)));

        int descIndex = cursor.getColumnIndex(ExpenseSQLite.DEPOSIT_DESCRIPTION);
        if (descIndex >= 0 && !cursor.isNull(descIndex)) {
            deposit.setDescription(cursor.getString(descIndex));
        }

        String dateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_DATETIME));
        deposit.setDateTime(java.time.ZonedDateTime.parse(dateTimeStr, Util.dateFormatterInsert));

        deposit.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_IS_DELETED)) == 1);

        int colorIndex = cursor.getColumnIndex(ExpenseSQLite.DEPOSIT_ROW_COLOR);
        if (colorIndex >= 0 && !cursor.isNull(colorIndex)) {
            deposit.setRowColor(cursor.getInt(colorIndex));
        }

        return deposit;
    }


}