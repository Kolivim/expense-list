package com.example.test3.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class ExpenseSQLite extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "ExpenseDB";

    /** Таблица хранит сами записи Расходов */
    public static final String TABLE_EXPENSE = "expense";
    public static final String EXPENSE_ID = "expenseId";
    public static final String EXPENSE_NAME = "name";
    public static final String EXPENSE_DESCRIPTION = "description";
    public static final String EXPENSE_DATETIME = "dateTime";
    public static final String EXPENSE_IS_DELETED = "isDeleted";
    public static final String EXPENSE_ROW_COLOR = "rowColor";


    /** Таблица хранит список платежей, относящихся к каждой из записей Расходов (M-to-O) */
    public static final String TABLE_PAYMENT = "payment";
    public static final String PAYMENT_ID = "paymentId";
    public static final String PAYMENT_EXPENSE_ID = "paymentExpenseId";
    public static final String PAYMENT = "payment";


    public ExpenseSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String createExpenseTableSql = "CREATE TABLE " + TABLE_EXPENSE + " ( " +
                EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + EXPENSE_NAME + " TEXT, " +
                EXPENSE_DESCRIPTION + " TEXT, " + EXPENSE_DATETIME + " TEXT, " +
                EXPENSE_IS_DELETED + " INTEGER, " + EXPENSE_ROW_COLOR + " INTEGER " +
                " )";

        sqLiteDatabase.execSQL(createExpenseTableSql);


        String createPaymentTableSql = "CREATE TABLE " + TABLE_PAYMENT + " ( " +
                PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PAYMENT_EXPENSE_ID + " INTEGER, " +
                PAYMENT + " REAL " + " )";

        sqLiteDatabase.execSQL(createPaymentTableSql);

    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        /** Дополнить выводом в файл либо миграцией */
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENT);

    }


    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
