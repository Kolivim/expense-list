package com.example.test3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExpenseSQLite extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    private static final String DATABASE_NAME = "ExpenseDB";

    /** Таблица хранит сами записи Расходов */
    public static final String TABLE_EXPENSE = "expense";
    public static final String EXPENSE_ID = "id";
    public static final String EXPENSE_EXPENSE_TYPE_ID = "type_id";
    public static final String EXPENSE_NAME = "name";
    public static final String EXPENSE_DESCRIPTION = "description";
    public static final String EXPENSE_DATETIME = "date_time";
    public static final String EXPENSE_IS_DELETED = "is_deleted";
    public static final String EXPENSE_ROW_COLOR = "row_color";


    /** Таблица хранит список платежей, относящихся к каждой из записей Расходов (M-to-O) */
    public static final String TABLE_EXPENSE_PAYMENT = "expense_payment";
    public static final String EXPENSE_PAYMENT_ID = "id";
    public static final String EXPENSE_PAYMENT_EXPENSE_ID = "expense_id";
    public static final String EXPENSE_PAYMENT = "payment";


    /** Таблица типов расходов */
    public static final String TABLE_EXPENSE_TYPE = "type_expense";
    public static final String EXPENSE_TYPE_ID = "id";
    public static final String EXPENSE_TYPE_NAME = "name";
    /** !Таблица типов расходов */


    /** Таблица хранит список взносов, как погашений, так и просто взносов */
    public static final String TABLE_DEPOSIT = "deposit";

    public static final String DEPOSIT_ID = "id";

    /** Ссылка на платёж, к погашению которого относится внесение,
     * может быть, может не быть, в зависимости от:
     * - если внесение происходит до осуществления трат, то ссылки не будет, её ещё нет
     * - если внесение происходит после осуществления трат, то ссылка будет */
    public static final String DEPOSIT_EXPENSE_ID = "expense_id";
    /**
    Ссылка на тип:
                    ежемесячный возврат долга за предыдущий месяц, по кредитке, на месяц(число)
                    либо для ежемесячного распределения финансов
                    либо для ежемесячного взносов на кредитку - бензин/коммуналка и т.п.

                    либо для возврата задолженностей по длинным займам, оплаченным с кредитки
                    либо для возврата задолженностей по длинным займам, оплаченным с моих наличных
    */
    /** Ссылка на таблицу с перечисленными типами внесений (FK), все внесения могут быть только зарегестрированных типов */
    public static final String DEPOSIT_DEPOSIT_TYPE_ID = "type_id";

    public static final String DEPOSIT_NAME = "name";
    public static final String DEPOSIT_DESCRIPTION = "description";
    public static final String DEPOSIT_DATETIME = "date_time";

    public static final String DEPOSIT_IS_DELETED = "is_deleted";
    public static final String DEPOSIT_ROW_COLOR = "row_color";
    /** !Таблица хранит список взносов, как погашений, так и просто взносов */


    /** Таблица месяцев */
    public static final String TABLE_MONTH = "month";
    public static final String MONTH_ID = "id";
    public static final String MONTH_YEAR = "year";
    public static final String MONTH_MONTH = "month";
    /** !Таблица месяцев */


    /** Таблица хранит список платежей, относящихся к каждой из записей Deposit (M-to-O) */
    public static final String TABLE_DEPOSIT_PAYMENT = "deposit_payment";
    public static final String DEPOSIT_PAYMENT_ID = "id";
    public static final String DEPOSIT_PAYMENT_DEPOSIT_ID = "deposit_id";
    /** Сумма платежа */
    public static final String DEPOSIT_PAYMENT = "payment";
    /** !Таблица хранит список платежей, относящихся к каждой из записей Deposit (M-to-O) */


    /** Таблица типов взносов */
    public static final String TABLE_DEPOSIT_TYPE = "type_deposit";
    public static final String DEPOSIT_TYPE_ID = "id";
    public static final String DEPOSIT_TYPE_NAME = "name";
    /** !Таблица типов взносов */


    public ExpenseSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        createExpenseTable(sqLiteDatabase);
        createExpensePaymentTable(sqLiteDatabase);
        createExpenseTypeTable(sqLiteDatabase);
        insertExpenseType(sqLiteDatabase);
        createMonthTable(sqLiteDatabase);

        createDepositTable(sqLiteDatabase);
        createDepositPaymentTable(sqLiteDatabase);
        createDepositTypeTable(sqLiteDatabase);
        insertDepositType(sqLiteDatabase);

    }


    public void createExpenseTable(SQLiteDatabase sqLiteDatabase) {

        String createExpenseTableSql = "CREATE TABLE " + TABLE_EXPENSE + " ( " +
                EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXPENSE_EXPENSE_TYPE_ID + " INTEGER NOT NULL, " +
                EXPENSE_NAME + " TEXT, " +
                EXPENSE_DESCRIPTION + " TEXT, " +
                EXPENSE_DATETIME + " TEXT, " +
                EXPENSE_IS_DELETED + " INTEGER DEFAULT 0, " +
                EXPENSE_ROW_COLOR + " INTEGER ," +
                "FOREIGN KEY (" + EXPENSE_EXPENSE_TYPE_ID + ") REFERENCES " + TABLE_EXPENSE_TYPE + "(" + EXPENSE_TYPE_ID + ") ON DELETE RESTRICT ," +
                "CHECK (" + EXPENSE_IS_DELETED + " IN (0, 1)) " +
                " )";

        sqLiteDatabase.execSQL(createExpenseTableSql);

    }


    public void createExpensePaymentTable(SQLiteDatabase sqLiteDatabase) {

        String createPaymentTableSql = "CREATE TABLE " + TABLE_EXPENSE_PAYMENT + " ( " +
                EXPENSE_PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXPENSE_PAYMENT_EXPENSE_ID + " INTEGER NOT NULL, " +
                EXPENSE_PAYMENT + " REAL, " +
                "FOREIGN KEY (" + EXPENSE_PAYMENT_EXPENSE_ID + ") REFERENCES " + TABLE_EXPENSE + "(" + EXPENSE_ID + ") ON DELETE RESTRICT " +
                " )";

        sqLiteDatabase.execSQL(createPaymentTableSql);

    }


    public void createExpenseTypeTable(SQLiteDatabase sqLiteDatabase) {

        String sql = "CREATE TABLE " + TABLE_EXPENSE_TYPE + " ( " +
                EXPENSE_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXPENSE_TYPE_NAME + " TEXT NOT NULL " +
                ")";

        sqLiteDatabase.execSQL(sql);

    }


    public void insertExpenseType(SQLiteDatabase sqLiteDatabase) {

        /** Вариант с ContentValues */
//        ContentValues values = new ContentValues();
//
//        values.put(EXPENSE_TYPE_NAME, "Один");
//        sqLiteDatabase.insert(TABLE_EXPENSE_TYPE, null, values);
//
//        values.clear();
//        values.put(EXPENSE_TYPE_NAME, "Два");
//        sqLiteDatabase.insert(TABLE_EXPENSE_TYPE, null, values);
//
//        values.clear();
//        values.put(EXPENSE_TYPE_NAME, "Три");
//        sqLiteDatabase.insert(TABLE_EXPENSE_TYPE, null, values);
        /** !Вариант с ContentValues */


        String sql = "INSERT INTO " + TABLE_EXPENSE_TYPE +
                " (" + EXPENSE_TYPE_NAME + ") VALUES " +
                "('Ежемесячные расходы'), " +
                "('Ежемесячное планирование бюджета'), " +
//                "('Ежемесячные взносы на кредитку'), " +                                            /** Коммуналка и etc. */
                "('Длинные займы с кредитных средств'), " +
                "('Длинные займы с собственных средств'), " +
                "('Коммунальные платежи') ";

        sqLiteDatabase.execSQL(sql);


        /**
         ежемесячный возврат долга за предыдущий месяц, по кредитке, на месяц(число)
         либо для ежемесячного распределения финансов
         либо для ежемесячного взносов на кредитку - бензин/коммуналка и т.п.

         либо для возврата задолженностей по длинным займам, оплаченным с кредитки
         либо для возврата задолженностей по длинным займам, оплаченным с собственных наличных

         коммуналка
         */

    }


    public void insertDepositType(SQLiteDatabase sqLiteDatabase) {

        String sql = "INSERT INTO " + TABLE_DEPOSIT_TYPE +
                " (" + DEPOSIT_TYPE_NAME + ") VALUES " +
                "('Погашение ежемесячных затрат'), " +              /** == "('Ежемесячные расходы'), " */
//                "('Ежемесячное планирование бюджета'), " +        /** Отсутсвует в чистом виде, частично заменяется следующей строкой */
                "('Ежемесячные взносы на кредитку'), " +            /** Вперед, для трат их позднее, Коммуналка и etc. */
                "('Погашение затрат по длинным займам с кредитных средств'), " +
                "('Погашение затрат по длинным займам с собственных средств') ";
//                "('Коммунальные платежи') ";

        sqLiteDatabase.execSQL(sql);


        /**
         ежемесячный возврат долга за предыдущий месяц, по кредитке, на месяц(число)
         либо для ежемесячного распределения финансов
         либо для ежемесячного взносов на кредитку - бензин/коммуналка и т.п.

         либо для возврата задолженностей по длинным займам, оплаченным с кредитки
         либо для возврата задолженностей по длинным займам, оплаченным с собственных наличных

         коммуналка
         */

    }


    public void createMonthTable(SQLiteDatabase sqLiteDatabase) {

        String sql = "CREATE TABLE " + TABLE_MONTH + " ( " +
                MONTH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MONTH_YEAR + " INTEGER NOT NULL, " +
                MONTH_MONTH + " INTEGER NOT NULL, " +
                "UNIQUE(" + MONTH_YEAR + ", " + MONTH_MONTH + "), " +
/*                "CHECK (" + MONTH_MONTH + " >= 1 AND " + MONTH_MONTH + " <= 12) " + */
                "CHECK (" + MONTH_MONTH + " BETWEEN 1 AND 12) " +
                ")";

        sqLiteDatabase.execSQL(sql);

    }


    public void createDepositTable(SQLiteDatabase sqLiteDatabase) {

        String createDepositTableSql = "CREATE TABLE " + TABLE_DEPOSIT + " ( " +
                DEPOSIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DEPOSIT_EXPENSE_ID + " INTEGER, " +
                DEPOSIT_DEPOSIT_TYPE_ID + " INTEGER NOT NULL, " +
                DEPOSIT_NAME + " TEXT NOT NULL, " +
                DEPOSIT_DESCRIPTION + " TEXT, " +
                DEPOSIT_DATETIME + " TEXT, " +
                DEPOSIT_IS_DELETED + " INTEGER DEFAULT 0, " +
                DEPOSIT_ROW_COLOR + " INTEGER, " +
                "FOREIGN KEY (" + DEPOSIT_EXPENSE_ID + ") REFERENCES " + TABLE_EXPENSE + "(" + EXPENSE_ID + ") ON DELETE SET NULL, " +
                "FOREIGN KEY (" + DEPOSIT_DEPOSIT_TYPE_ID + ") REFERENCES " + TABLE_DEPOSIT_TYPE + "(" + DEPOSIT_TYPE_ID + ") ON DELETE RESTRICT, " +
                "CHECK (" + DEPOSIT_IS_DELETED + " IN (0, 1)) " +
                ")";

        sqLiteDatabase.execSQL(createDepositTableSql);

    }


    public void createDepositPaymentTable(SQLiteDatabase sqLiteDatabase) {

        String createDepositTableSql = "CREATE TABLE " + TABLE_DEPOSIT_PAYMENT + " ( " +
                DEPOSIT_PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DEPOSIT_PAYMENT_DEPOSIT_ID + " INTEGER NOT NULL, " +
                DEPOSIT_PAYMENT + " REAL NOT NULL, " +
                "FOREIGN KEY (" + DEPOSIT_PAYMENT_DEPOSIT_ID + ") REFERENCES " + TABLE_DEPOSIT + "(" + DEPOSIT_ID + ") ON DELETE RESTRICT " +
                ")";

        sqLiteDatabase.execSQL(createDepositTableSql);

    }


    public void createDepositTypeTable(SQLiteDatabase sqLiteDatabase) {

        String sql = "CREATE TABLE " + TABLE_DEPOSIT_TYPE + " ( " +
                DEPOSIT_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DEPOSIT_TYPE_NAME + " TEXT NOT NULL " +
                ")";

        sqLiteDatabase.execSQL(sql);

    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // TODO: Реализовать миграции вместо затирания таблиц, в проде

        /** Дополнить выводом в файл либо миграцией : */
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_PAYMENT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_TYPE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTH);


        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT_PAYMENT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT_TYPE);


        onCreate(sqLiteDatabase);
        /** !Дополнить выводом в файл либо миграцией */


    }


    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        /** Включает поддержку внешних ключей */
        db.execSQL("PRAGMA foreign_keys=ON;");
    }


//    @Override
//    public void onConfigure(SQLiteDatabase db) {
//        db.setForeignKeyConstraintsEnabled(true);
//    }


}
