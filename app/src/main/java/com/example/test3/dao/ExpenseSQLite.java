package com.example.test3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.ZonedDateTime;

public class ExpenseSQLite extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

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


    /** Таблица хранит сами записи ПлановогоВозвратаРасходов : */
    public static final String TABLE_EXPENSE_REFUND = "expense_refund";
    public static final String EXPENSE_REFUND_ID = "id";
    public static final String EXPENSE_REFUND_EXPENSE_ID = "expense_id";                            /** id Expense, к которой относится запись в таблице */
    public static final String EXPENSE_REFUND_START_DATE = "start_date";
    public static final String EXPENSE_REFUND_MONTH_COUNT = "month_count";
    public static final String EXPENSE_REFUND_IS_REFUNDED = "is_refunded";
    /** !Таблица хранит сами записи ПлановогоВозвратаРасходов */


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
    public static final String MONTH_MONTH_TYPE_ID = "type_id";
    public static final String MONTH_YEAR = "year";
    public static final String MONTH_MONTH = "month";
    /** !Таблица месяцев */


    /** Таблица типов месяцев */
    public static final String TABLE_MONTH_TYPE = "month_type";
    public static final String MONTH_TYPE_ID = "id";
    public static final String MONTH_TYPE_NAME = "name";
    /** !Таблица типов месяцев */


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


    /** Таблица показаний счетчиков */
    public static final String TABLE_METER = "meter";
    public static final String METER_ID = "id";

    public static final String METER_MONTH_ID = "month_id";
    public static final String METER_NAME = "name";
    public static final String METER_VALUE = "value";
    /** !Таблица показаний счетчиков */


    /** Таблица номеров счетов */
    public static final String TABLE_ACCOUNT_NUMBER = "account_number";
    public static final String ACCOUNT_NUMBER_ID = "id";
    /** expenseId либо depositId, в зависимости от типа */
    public static final String ACCOUNT_NUMBER_NUMBER_PARENT_ID = "parent_id";
    /** 0 либо 1 - 0 это expense, 1 это deposit */
    public static final String ACCOUNT_NUMBER_TYPE = "type";
    /** Может быть как наименование счёта, так и наименование банка-эмитента карты */
    public static final String ACCOUNT_NUMBER_NAME = "name";
    public static final String ACCOUNT_NUMBER_NUMBER = "number";
    /** !Таблица номеров счетов */


    public ExpenseSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        createExpenseTable(sqLiteDatabase);
        createExpensePaymentTable(sqLiteDatabase);
        createExpenseTypeTable(sqLiteDatabase);
        insertExpenseType(sqLiteDatabase);
        createExpenseRefundTable(sqLiteDatabase);
        createAccountNumberTable(sqLiteDatabase);

        createDepositTable(sqLiteDatabase);
        createDepositPaymentTable(sqLiteDatabase);
        createDepositTypeTable(sqLiteDatabase);
        insertDepositType(sqLiteDatabase);
        insertDepositTypePart2(sqLiteDatabase);                                                     /** добавлено после начала ОЭ */

        createMonthTypeTable(sqLiteDatabase);
        insertMonthTypes(sqLiteDatabase);
        insertMonthTypes2(sqLiteDatabase);                                                          /** добавлено после начала ОЭ */
        createMonthTable(sqLiteDatabase);

        createMeterTable(sqLiteDatabase);
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


    public void createExpenseRefundTable(SQLiteDatabase sqLiteDatabase) {

        String createExpenseTableSql = "CREATE TABLE " + TABLE_EXPENSE_REFUND + " ( " +
                EXPENSE_REFUND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXPENSE_REFUND_EXPENSE_ID + " INTEGER NOT NULL, " +
                EXPENSE_REFUND_START_DATE + " TEXT, " +
                EXPENSE_REFUND_MONTH_COUNT + " INTEGER, " +
                EXPENSE_REFUND_IS_REFUNDED + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY (" + EXPENSE_REFUND_EXPENSE_ID + ") REFERENCES " + TABLE_EXPENSE + "(" + EXPENSE_ID + ") ON DELETE RESTRICT ," +
                "CHECK (" + EXPENSE_REFUND_IS_REFUNDED + " IN (0, 1)) " +
                " )";

        sqLiteDatabase.execSQL(createExpenseTableSql);
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
                "('Ежемесячные расходы'), " +                                                       /** 1 */
                "('Ежемесячное планирование бюджета'), " +                                          /** 2 */
                "('Длинные займы с кредитных средств'), " +                                         /** 3 */
                "('Длинные займы с собственных средств'), " +                                       /** 4 */
                "('Коммунальные платежи'), " +                                                      /** 5 */
                "('Ежемесячное планирование возвратов длинных займов'), " +                         /** 6 Планирование и реализация погашения пп.3 и 4*/
                "('Ежемесячные затраты с взносов собственных средств на кредитку')";                /** 7 Коммуналка и etc. (соответствует Deposit с type == 2) */
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
                "('Погашение ежемесячных затрат'), " +                                              /** 1. == "('Ежемесячные расходы'), " */
                "('Ежемесячные взносы на кредитку'), " +                                            /** 2. Вперед, для трат их позднее, Коммуналка и etc. */
                "('Погашение затрат по длинным займам с кредитных средств'), " +                    /** 3. */
                "('Погашение затрат по длинным займам с собственных средств'), " +                  /** 4. */
                "('Планируемый возврат ежемесячных трат') ";                                        /** 5. Для подсчёта остатка ДС, выделенных на месяц */
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

    public void insertDepositTypePart2(SQLiteDatabase sqLiteDatabase) {

        String sql = "INSERT INTO " + TABLE_DEPOSIT_TYPE +
                " (" + DEPOSIT_TYPE_NAME + ") VALUES " +
                "('Ежемесячное планирование бюджета'), " +                                          /** 6 */
                "('Взносы согласно ежемесячного планирования возвратов длинных займов'), " +        /** 7 Реально выполненные взносы согласно запланированных возвратов по длинным займам, соответствует п.6 Expense */
                "('Планируемые взносы для ежемесячного планирования возвратов длинных займов') ";   /** 8 Запланированная сумма взноса для погашения запланированных возвратов по длинным займам, соответствует п.6 Expense */

        sqLiteDatabase.execSQL(sql);

    }


    public void createMonthTable(SQLiteDatabase sqLiteDatabase) {

        String sql = "CREATE TABLE " + TABLE_MONTH + " ( " +
                MONTH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MONTH_YEAR + " INTEGER NOT NULL, " +
                MONTH_MONTH + " INTEGER NOT NULL, " +
                MONTH_MONTH_TYPE_ID + " INTEGER NOT NULL, " +
                "UNIQUE(" + MONTH_YEAR + ", " + MONTH_MONTH + ", "+ MONTH_MONTH_TYPE_ID + "), " +
/*                "CHECK (" + MONTH_MONTH + " >= 1 AND " + MONTH_MONTH + " <= 12) " + */
                "CHECK (" + MONTH_MONTH + " BETWEEN 1 AND 12) " +
                ")";

        sqLiteDatabase.execSQL(sql);

    }


    /** Создание таблицы типов месяцев */
    public void createMonthTypeTable(SQLiteDatabase sqLiteDatabase) {

        String sql = "CREATE TABLE " + TABLE_MONTH_TYPE + " ( " +
                MONTH_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MONTH_TYPE_NAME + " TEXT NOT NULL UNIQUE" +
                ")";

        sqLiteDatabase.execSQL(sql);
    }


    /** Заполнение таблицы типов месяцев */
    public void insertMonthTypes(SQLiteDatabase sqLiteDatabase) {

        String sql = "INSERT INTO " + TABLE_MONTH_TYPE +
                " (" + MONTH_TYPE_NAME + ") VALUES " +
                "('Ежемесячные расходы'), " +                                                       /** typeId = 1 */
                "('Передача показаний')"; /** Коммунальные услуги - по факту, и передача показаний, и оплата КУ */     /** typeId = 2 */    //  TODO: Исправить имя

        sqLiteDatabase.execSQL(sql);
    }


    /** Заполнение таблицы типов месяцев2 */
    public void insertMonthTypes2(SQLiteDatabase sqLiteDatabase) {

        String sql = "INSERT INTO " + TABLE_MONTH_TYPE +
                " (" + MONTH_TYPE_NAME + ") VALUES " +
                "('Ежемесячное планирование расходов'), " +                                         /** typeId = 3 */
                "('Ежемесячное планирование возвратов по длинным займам'), " +                      /** 4 */
                "('Ежемесячные взносы на кредитку')";                                               /** 5 ДС, внемённые на кредитку вперёд */
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


    public void createMeterTable(SQLiteDatabase sqLiteDatabase) {

        String sql = "CREATE TABLE " + TABLE_METER + " ( " +
                METER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                METER_MONTH_ID + " INTEGER NOT NULL, " +
                METER_NAME + " TEXT NOT NULL, " +
                METER_VALUE + " REAL NOT NULL " +
                ")";

        sqLiteDatabase.execSQL(sql);

    }


    public void createAccountNumberTable(SQLiteDatabase sqLiteDatabase) {

        String sql = "CREATE TABLE " + TABLE_ACCOUNT_NUMBER + " ( " +
                ACCOUNT_NUMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ACCOUNT_NUMBER_NUMBER_PARENT_ID + " INTEGER NOT NULL, " +
                ACCOUNT_NUMBER_TYPE + " INTEGER NOT NULL, " +
                ACCOUNT_NUMBER_NAME + " TEXT, " +
                ACCOUNT_NUMBER_NUMBER + " TEXT NOT NULL, " +
                "CHECK (" + ACCOUNT_NUMBER_TYPE + " IN (0, 1)) " +
                ")";

        sqLiteDatabase.execSQL(sql);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        // TODO: Реализовать миграции вместо затирания таблиц, в проде

        /** Дополнить выводом в файл либо миграцией : */
//        /*
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_PAYMENT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_TYPE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_REFUND);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT_NUMBER);


        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT_PAYMENT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT_TYPE);


        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_METER);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTH);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTH_TYPE);


        onCreate(sqLiteDatabase);
//        */


        /*
        if(oldVersion <= 7 && newVersion >= 8) {
            insertDepositTypePart2(sqLiteDatabase);
            insertMonthTypes2(sqLiteDatabase);
        }
        */
        /** !Дополнить выводом в файл либо миграцией */


    }


    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

//        onUpgrade(db, oldVersion, newVersion);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_PAYMENT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_TYPE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_REFUND);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT_NUMBER);


        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT_PAYMENT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT_TYPE);


        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_METER);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTH);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTH_TYPE);


        onCreate(sqLiteDatabase);
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
