package com.example.test3.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExpenseSQLite extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "ExpenseDB";

    /** Таблица хранит сами записи Расходов */
    private static final String TABLE_EXPENSE = "expense";

    /** Таблица хранит список платежей, относящихся к каждой из записей Расходов (M-to-O) */
    private static final String TABLE_PAYMENT = "payment";
    private static final String EXPENSE_ID = "expenseId";
    private static final String EXPENSE_NAME = "name";
    private static final String EXPENSE_DESCRIPTION = "description";


    public ExpenseSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //  SC

        /*
            String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
        USER_ID + " INTEGER PRIMARY KEY," + USER_NAME + " TEXT," +
        USER_DESCR + " TEXT, " + sqLiteDatabase.execSQL(CREATE_USERS_TABLE);
         */

    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        //  SC

        /*
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
         */

    }

}
