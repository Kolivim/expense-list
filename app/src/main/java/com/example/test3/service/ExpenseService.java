package com.example.test3.service;


import static com.example.test3.dao.ExpenseSQLite.EXPENSE_DATETIME;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_DESCRIPTION;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_IS_DELETED;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_NAME;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_ROW_COLOR;
import static com.example.test3.dao.ExpenseSQLite.PAYMENT;
import static com.example.test3.dao.ExpenseSQLite.PAYMENT_EXPENSE_ID;
import static com.example.test3.dao.ExpenseSQLite.TABLE_EXPENSE;
import static com.example.test3.dao.ExpenseSQLite.TABLE_PAYMENT;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.test3.dao.ExpenseSQLite;
import com.example.test3.expenseList.Expense;
import com.example.test3.util.Util;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class ExpenseService {

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


    public ArrayList<Expense> getExpenseList() {

        ArrayList<Expense> expenseList = getExpenseRowList();

        setExpenseListPayments(expenseList);

        return expenseList;
    }


    public ArrayList<Expense> getExpenseRowList() {

        ArrayList<Expense> expenseList = new ArrayList<>();

        Cursor query = dbRead.rawQuery("SELECT * FROM " + TABLE_EXPENSE + " order by expenseId desc;", null);

        while(query.moveToNext()){

            Long expenseId = query.getLong(0);
            String expenseName = query.getString(1);
            String expenseDescription = query.getString(2);
            String expenseDateTime = query.getString(3);
            int expenseIsDelete = query.getInt(4);
            int expenseRowColor = query.getInt(5);                                                /** Визуальное оформление позднее выделить в отдельную таблицу, со связью по expenseId */

            Expense expense = new Expense(expenseId, expenseName, expenseDescription,
                    ZonedDateTime.parse(expenseDateTime, Util.dateFormatterInsert), (expenseIsDelete == 1),
                    expenseRowColor);

            expenseList.add(expense);

        }

        query.close();


        return expenseList;                                                                         /* Cursor cursor = db.rawQuery("Select *" +  " FROM "+ TABLE_USERS + " WHERE " + USER_ID + " = " + number, null); */
    }


    public void setExpenseListPayments(ArrayList<Expense> expenseList) {
        for (Expense expense : expenseList) setExpensePayments(expense);
    }

    public void setExpensePayments(Expense expense) {

        Cursor query = dbRead.rawQuery(
                "SELECT * FROM " + TABLE_PAYMENT +
                    " where " + PAYMENT_EXPENSE_ID + " = " + expense.getId() +
                    " order by paymentId asc;" ,
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
        cv.put(PAYMENT_EXPENSE_ID, expense.getId());
        cv.put(PAYMENT, expense.getExpenseList().get(0));                                           /** Забираем первый элемент, т.к. при создании м.б. не более одного элемента */

        long result = dbWrite.insert(TABLE_PAYMENT, null, cv);

        return result != -1;
    }


    public boolean removeExpense(Expense expense) {

        long result = dbWrite.delete(TABLE_EXPENSE, EXPENSE_ID + " = " + expense.getId().toString(), null /*new String {"name"} */);

        return result != -1;
    }

}
