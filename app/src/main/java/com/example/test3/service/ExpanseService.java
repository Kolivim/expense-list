package com.example.test3.service;


import static com.example.test3.dao.ExpenseSQLite.EXPENSE_DATETIME;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_DESCRIPTION;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_IS_DELETED;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_NAME;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_ROW_COLOR;
import static com.example.test3.dao.ExpenseSQLite.TABLE_EXPENSE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.test3.dao.ExpenseSQLite;
import com.example.test3.expenseList.Expense;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class ExpanseService {

    private Context context;

    ExpenseSQLite dbHelper;

    SQLiteDatabase dbWrite;

    SQLiteDatabase dbRead;



    public ExpanseService(Context baseContext) {
        this.context = baseContext;
        this.dbHelper = new ExpenseSQLite(this.context);
        this.dbWrite = dbHelper.getWritableDatabase();
        this.dbRead = dbHelper.getReadableDatabase();
    }


    public ArrayList<Expense> getExpanseList() {

        ArrayList<Expense> expenseList = new ArrayList<>();

        Cursor query = dbRead.rawQuery("SELECT * FROM " + TABLE_EXPENSE + " order by expenseId desc;", null);

        while(query.moveToNext()){

            int expenseId = query.getInt(0);
            String expenseName = query.getString(1);
            String expenseDescription = query.getString(2);
            String expenseDateTime = query.getString(3);
            int expenseIsDelete = query.getInt(4);
            int expenseRowColor = query.getInt(5);                                                /** Визуальное оформление позднее выделить в отдельную таблицу, со связью по expenseId */

            Expense expense = new Expense(expenseName, expenseDescription, 0.0, ZonedDateTime.now());
            expense.setId(expenseId);
            expense.setDeleted(expenseIsDelete != 0);

            expenseList.add(expense);

        }

        query.close();


        return expenseList;

        /* Cursor cursor = db.rawQuery("Select *" +  " FROM "+ TABLE_USERS + " WHERE " + USER_ID + " = " + number, null); */

    }


    public boolean insertExpense(Expense expense) {

        ContentValues cv = new ContentValues();
        cv.put(EXPENSE_NAME, expense.getName());
        cv.put(EXPENSE_DESCRIPTION, expense.getName());
        cv.put(EXPENSE_DATETIME, "01.01.2025 11:25:36");
        cv.put(EXPENSE_IS_DELETED, 0);
        cv.put(EXPENSE_ROW_COLOR, 0);

//        dbWrite.execSQL("INSERT OR IGNORE INTO TABLE_EXPENSE VALUES ('Coffee', 23);");
//        return false;

        long result = dbWrite.insert(TABLE_EXPENSE, null, cv);

        return result != -1;
    }


    public boolean removeExpense(Expense expense) {

        long result = dbWrite.delete(TABLE_EXPENSE, EXPENSE_ID + " = " + expense.getId().toString(), null /*new String {"name"} */);

        return result != -1;
    }

}
