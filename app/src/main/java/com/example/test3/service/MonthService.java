package com.example.test3.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.test3.dao.ExpenseSQLite;
import com.example.test3.month.Month;
import com.example.test3.month.MonthlyDto;
import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.Expense;

import java.util.ArrayList;
import java.util.List;

public class MonthService {

    private static final String TAG = "MonthService";

    public static Long TYPE_MONTHLY_EXPENSES = 1L;
    public static Long TYPE_MONTHLY_DEPOSIT = 1L;


    private Context context;
    private ExpenseSQLite dbHelper;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbWrite;
    private ExpenseService expenseService;


    public MonthService(Context context) {
        this.context = context;
        this.dbHelper = new ExpenseSQLite(context);
        this.dbRead = dbHelper.getReadableDatabase();
        this.dbWrite = dbHelper.getWritableDatabase();
        this.expenseService = new ExpenseService(context);
    }


    /**
     * Синхронизирует таблицу month с данными из expense и deposit
     * Создаёт отсутствующие записи в month и связывает с ними deposit
     */
    public void syncMonths() {
        Log.d(TAG, "Начало синхронизации месяцев");


        /** Получает все уникальные месяцы из expense и deposit */
        List<Month> monthsFromData = getAllMonthsWithData();


        for (Month month : monthsFromData) {

            /** Проверяем, есть ли такой месяц в таблице month */
            Month existingMonth = findMonthInDb(month.getYear(), month.getMonth());

            if (existingMonth == null) {

                /** Месяца нет - создаём */
                long monthId = insertMonth(month);
                month.setId(monthId);
                Log.d(TAG, "Создан новый месяц: " + month.getMonthYear() + " с ID: " + monthId);

            } else {

                /** Месяц уже есть - используем его ID */
                month.setId(existingMonth.getId());

            }


            /** Связываем deposit с месяцем (связь через expenseId) */
            linkDepositsToMonth(month);

        }

        Log.d(TAG, "Синхронизация месяцев завершена");
    }


    /** Проверяет наличие месяца в таблице month */
    private Month findMonthInDb(int year, int month) {

        Cursor cursor = dbRead.rawQuery(
                "SELECT * FROM " + ExpenseSQLite.TABLE_MONTH +
                        " WHERE " + ExpenseSQLite.MONTH_YEAR + " = " + year +
                        " AND " + ExpenseSQLite.MONTH_MONTH + " = " + month,
                null);

        Month monthObj = null;
        if (cursor.moveToFirst()) {
            monthObj = cursorToMonth(cursor);
        }
        cursor.close();
        return monthObj;
    }


    /** Вставляет месяц в БД */
    private long insertMonth(Month month) {

        ContentValues cv = new ContentValues();
        cv.put(ExpenseSQLite.MONTH_YEAR, month.getYear());
        cv.put(ExpenseSQLite.MONTH_MONTH, month.getMonth());

        return dbWrite.insert(ExpenseSQLite.TABLE_MONTH, null, cv);
    }


    /** Преобразует курсор в объект Month */
    private Month cursorToMonth(Cursor cursor) {

        Month month = new Month();
        month.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseSQLite.MONTH_ID)));

        int year = cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseSQLite.MONTH_YEAR));
        int monthValue = cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseSQLite.MONTH_MONTH));

        if (monthValue < 1 || monthValue > 12) {
            Log.e(TAG, "⚠️ В БД найден некорректный месяц: " + monthValue + " для ID=" + month.getId() +
                    ". Исправляем на текущий месяц.");
            monthValue = java.time.ZonedDateTime.now().getMonthValue();
        }

        month.setYear(year);
        month.setMonth(monthValue);

        return month;
    }


    /**
     * Связывает записи deposit с месяцем
     * Для deposit с type = 1 устанавливаем expenseId = ID месяца
     */
    private void linkDepositsToMonth(Month month) {

        String monthStr = String.format("%02d", month.getMonth());
        String yearStr = String.valueOf(month.getYear()).substring(2);


        /** Находит все deposit за этот месяц (type=1), у которых expenseId не указывает на этот месяц
            (т.е. либо NULL, либо отличное от переданного в month значение) */
        Cursor depositCursor = dbRead.rawQuery(
                "SELECT " + ExpenseSQLite.DEPOSIT_ID +
                        " FROM " + ExpenseSQLite.TABLE_DEPOSIT +
                        " WHERE " + ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID + " = " + TYPE_MONTHLY_DEPOSIT +
                        " AND " + ExpenseSQLite.DEPOSIT_IS_DELETED + " = 0" +
                        " AND substr(" + ExpenseSQLite.DEPOSIT_DATETIME + ", 4, 2) = ?" +
                        " AND substr(" + ExpenseSQLite.DEPOSIT_DATETIME + ", 7, 2) = ?" +
                        " AND (" + ExpenseSQLite.DEPOSIT_EXPENSE_ID + " IS NULL OR " +
                        ExpenseSQLite.DEPOSIT_EXPENSE_ID + " != " + month.getId() + ")",
                new String[]{monthStr, yearStr});

        List<Long> depositIds = new ArrayList<>();

        while (depositCursor.moveToNext()) {depositIds.add(depositCursor.getLong(0));}

        depositCursor.close();


        /** Обновляет найденные deposit, устанавливая expenseId = month.getId() */
        if (!depositIds.isEmpty()) {

            ContentValues cv = new ContentValues();
            cv.put(ExpenseSQLite.DEPOSIT_EXPENSE_ID, month.getId()); // expenseId хранит ID месяца

            for (Long depositId : depositIds) {

                dbWrite.update(
                        ExpenseSQLite.TABLE_DEPOSIT,
                        cv,
                        ExpenseSQLite.DEPOSIT_ID + " = ?",
                        new String[]{String.valueOf(depositId)}
                );

                Log.d(TAG, "Для deposit ID=" + depositId + " установлен expenseId = " + month.getId() +
                        " (ID месяца " + month.getMonthYear() + ")");
            }

        }


    }


    /** Получает или создаёт месяц по году и номеру */
    public Month getOrCreateMonth(int year, int month) {

        /** Сначала ищет в БД */
        Month existingMonth = findMonthInDb(year, month);
        if (existingMonth != null) {
            return existingMonth;
        }


        /** Если нет - создаёт и сохраняет */
        Month newMonth = new Month(year, month);
        long id = insertMonth(newMonth);
        newMonth.setId(id);


        return newMonth;

    }


    /** Получает все месяцы, для которых есть расходы (typeId == 1) */
    public List<Month> getMonthsWithExpenses() {

        List<Month> months = new ArrayList<>();

        String query =
                "SELECT DISTINCT " +
                        "substr(" + ExpenseSQLite.EXPENSE_DATETIME + ", 7, 2) as year, " +
                        "substr(" + ExpenseSQLite.EXPENSE_DATETIME + ", 4, 2) as month " +
                        "FROM " + ExpenseSQLite.TABLE_EXPENSE + " " +
                        "WHERE " + ExpenseSQLite.EXPENSE_EXPENSE_TYPE_ID + " = " + TYPE_MONTHLY_EXPENSES + " " +
                        "AND " + ExpenseSQLite.EXPENSE_IS_DELETED + " = 0 " +
                        "ORDER BY year DESC, month DESC";

        Cursor cursor = dbRead.rawQuery(query, null);


        while (cursor.moveToNext()) {
            int year = 2000 + Integer.parseInt(cursor.getString(0));
            int month = Integer.parseInt(cursor.getString(1));
            months.add(new Month(year, month));
        }

        cursor.close();


        return months;
    }


    /** Получает все месяцы, для которых есть взносы (typeId == 1) */
    public List<Month> getMonthsWithDeposits() {

        List<Month> months = new ArrayList<>();

        String query =
                "SELECT DISTINCT " +
                        "substr(" + ExpenseSQLite.DEPOSIT_DATETIME + ", 7, 2) as year, " +
                        "substr(" + ExpenseSQLite.DEPOSIT_DATETIME + ", 4, 2) as month " +
                        "FROM " + ExpenseSQLite.TABLE_DEPOSIT + " " +
                        "WHERE " + ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID + " = " + TYPE_MONTHLY_DEPOSIT + " " +
                        "AND " + ExpenseSQLite.DEPOSIT_IS_DELETED + " = 0 " +
                        "ORDER BY year DESC, month DESC";

        Cursor cursor = dbRead.rawQuery(query, null);


        while (cursor.moveToNext()) {
            int year = 2000 + Integer.parseInt(cursor.getString(0));
            int month = Integer.parseInt(cursor.getString(1));
            months.add(new Month(year, month));
        }

        cursor.close();


        return months;
    }


    /** Получает все месяцы, для которых есть данные (расходы или взносы) */
    public List<Month> getAllMonthsWithData() {
        List<Month> months = new ArrayList<>();

        String query =
                "SELECT DISTINCT " +
                        "substr(" + ExpenseSQLite.EXPENSE_DATETIME + ", 7, 2) as year, " +
                        "substr(" + ExpenseSQLite.EXPENSE_DATETIME + ", 4, 2) as month " +
                        "FROM " + ExpenseSQLite.TABLE_EXPENSE + " " +
                        "WHERE " + ExpenseSQLite.EXPENSE_EXPENSE_TYPE_ID + " = " + TYPE_MONTHLY_EXPENSES + " " +
                        "AND " + ExpenseSQLite.EXPENSE_IS_DELETED + " = 0 " +
                        "UNION " +
                        "SELECT DISTINCT " +
                        "substr(" + ExpenseSQLite.DEPOSIT_DATETIME + ", 7, 2) as year, " +
                        "substr(" + ExpenseSQLite.DEPOSIT_DATETIME + ", 4, 2) as month " +
                        "FROM " + ExpenseSQLite.TABLE_DEPOSIT + " " +
                        "WHERE " + ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID + " = " + TYPE_MONTHLY_DEPOSIT + " " +
                        "AND " + ExpenseSQLite.DEPOSIT_IS_DELETED + " = 0 " +
                        "ORDER BY year DESC, month DESC";

        Cursor cursor = dbRead.rawQuery(query, null);


        while (cursor.moveToNext()) {

            int year = 2000 + Integer.parseInt(cursor.getString(0));
            int month = Integer.parseInt(cursor.getString(1));

            /** Проверяеn, есть ли месяц в БД */
            Month dbMonth = findMonthInDb(year, month);

            if (dbMonth != null) {
                months.add(dbMonth); /** добавляет с ID из БД */
            } else {
                months.add(new Month(year, month)); /** добавляет без ID */
            }

        }

        cursor.close();

        return months;
    }


    /** Получает расходы за указанный месяц */
    private List<Expense> getExpensesForMonth(Month month) {

        List<Expense> expenses = new ArrayList<>();

        String monthStr = String.format("%02d", month.getMonth());
        String yearStr = String.valueOf(month.getYear()).substring(2);                    /** 2026 -> "26" */


        /** Получает все расходы за месяц */
        ArrayList<Expense> allExpenses = expenseService.getExpenseList(1L);


        /** Фильтрует по месяцу */
        for (Expense expense : allExpenses) {

            String expenseDate = expense.getDateTime().format(com.example.test3.util.Util.dateFormatterInsert);
            String expenseMonth = expenseDate.substring(3, 5);
            String expenseYear = expenseDate.substring(6, 8);

            if (expenseMonth.equals(monthStr) && expenseYear.equals(yearStr)) {
                expenses.add(expense);
            }

        }

        return expenses;
    }


    /** Получает взносы за указанный месяц (typeId == 1) */
    private List<Deposit> getDepositsForMonth(Month month) {

        List<Deposit> deposits = new ArrayList<>();


        /** Ищет deposit с type=1, у которых expenseId = ID месяца */
        Cursor cursor = dbRead.rawQuery(
                "SELECT * " +
                    "FROM " + ExpenseSQLite.TABLE_DEPOSIT +
                    " WHERE " + ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID + " = " + TYPE_MONTHLY_DEPOSIT +
                        " AND " + ExpenseSQLite.DEPOSIT_IS_DELETED + " = 0" +
                        " AND " + ExpenseSQLite.DEPOSIT_EXPENSE_ID + " = " + month.getId(),
                null);


        while (cursor.moveToNext()) {
            Deposit deposit = cursorToDeposit(cursor);
            loadPaymentsForDeposit(deposit);
            deposits.add(deposit);
        }
        cursor.close();


        return deposits;

    }


    /** Загружает платежи для конкретного Deposit */
    private void loadPaymentsForDeposit(Deposit deposit) {

        Cursor cursor = dbRead.rawQuery(
                "SELECT " + ExpenseSQLite.DEPOSIT_PAYMENT + " FROM " + ExpenseSQLite.TABLE_DEPOSIT_PAYMENT +
                        " WHERE " + ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID + " = " + deposit.getId() +
                        " ORDER BY " + ExpenseSQLite.DEPOSIT_PAYMENT_ID + " ASC",
                null);


        while (cursor.moveToNext()) {
            deposit.addPayment(cursor.getDouble(0));
        }

        cursor.close();
    }


    /** Получает MonthlyDto для указанного месяца */
    public MonthlyDto getMonthlyDto(Month month) {

        MonthlyDto dto = new MonthlyDto();
        dto.setMonth(month);


        /** Получает расходы за месяц */
        List<Expense> expenses = getExpensesForMonth(month);
        dto.setExpenseList(expenses);

        /** Рассчитывает статистику по расходам */
        double totalExpenseAmount = 0;
        int paymentsCount = 0;

        for (Expense expense : expenses) {

            totalExpenseAmount += expense.getExpenseListTotalAmount();

            if (expense.getExpenseList() != null) {
                paymentsCount += expense.getExpenseList().size();
            }

        }

        dto.setTotalExpenseAmount(totalExpenseAmount);
        dto.setExpensesCount(expenses.size());
        dto.setPaymentsCount(paymentsCount);


        /** Получает взносы за месяц (typeId == 1) */
        List<Deposit> deposits = getDepositsForMonth(month);
        dto.setDepositList(deposits);


        /** Рассчитываем статистику по взносам */
        double totalDepositAmount = 0;
        for (Deposit deposit : deposits) {
            totalDepositAmount += deposit.getTotalAmount();
        }
        dto.setTotalDepositAmount(totalDepositAmount);
        dto.setDepositsCount(deposits.size());


        /** Рассчитываем баланс */
        dto.setBalance(totalDepositAmount - totalExpenseAmount);

        return dto;
    }


    /** Получает все имеющиеся MonthlyDto, для всех месяцев, для последующего вывода в интерфейсе Активити */
    public List<MonthlyDto> getAllMonthlyDtos() {

        List<Month> months = getAllMonthsWithData();
        List<MonthlyDto> dtos = new ArrayList<>();

        for (Month month : months) dtos.add(getMonthlyDto(month));

        return dtos;
    }


    /** Преобразует курсор в объект Deposit */
    private Deposit cursorToDeposit(Cursor cursor) {

        Deposit deposit = new Deposit();
        deposit.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_ID)));
        deposit.setExpenseId(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_EXPENSE_ID)));
        deposit.setTypeId(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID)));
        deposit.setName(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_NAME)));
        deposit.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_DESCRIPTION)));

        String dateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_DATETIME));
        deposit.setDateTime(java.time.ZonedDateTime.parse(dateTimeStr, com.example.test3.util.Util.dateFormatterInsert));

        deposit.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_IS_DELETED)) == 1);
        deposit.setRowColor(cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseSQLite.DEPOSIT_ROW_COLOR)));

        return deposit;
    }


    /**
     * Получает месяц по его ID
     * @param monthId ID месяца
     * @return объект Month или null, если не найден
     */
    public Month getMonthById(long monthId) {

        Cursor cursor = dbRead.rawQuery(
                "SELECT * FROM " + ExpenseSQLite.TABLE_MONTH +
                        " WHERE " + ExpenseSQLite.MONTH_ID + " = " + monthId,
                null);

        Month month = null;
        if (cursor.moveToFirst()) {
            month = cursorToMonth(cursor);
        }

        cursor.close();

        return month;
    }


}