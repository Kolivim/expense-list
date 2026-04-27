package com.example.test3.service;

import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_PLANNING;
import static com.example.test3.util.Util.TYPE_EXPENSE_MONTH_PLANNING;
import static com.example.test3.util.Util.TYPE_MONTHLY_EXPENSE_PLANNING;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.test3.dao.ExpenseSQLite;
import com.example.test3.meter.Meter;
import com.example.test3.month.Month;
import com.example.test3.month.MonthlyDto;
import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.Expense;
import com.example.test3.monthly.expense.planning.MonthlyExpensePlanningDto;
import com.example.test3.monthly.expense.utility.service.MonthUtilityServiceDto;
import com.example.test3.util.Util;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MonthService {

    private static final String TAG = "MonthService";

    public static Long TYPE_MONTHLY_EXPENSES = 1L;
    /* Из Util берём TYPE_MONTHLY_EXPENSE_PLANNYNG == 3L */
    public static Long TYPE_MONTHLY_DEPOSIT = 1L;
    public static Long TYPE_MONTHLY_DEPOSIT_PLAN = 5L;


    private Context context;
    private ExpenseSQLite dbHelper;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbWrite;
    private ExpenseService expenseService;
    private DepositService depositService;
    private MeterService meterService;

//    private DepositService getDepositService() {
//        if (depositService == null) {
//            depositService = new DepositService(context);
//            /** В DepositService уже будет ленивая инициализация MonthService */
//        }
//        return depositService;
//    }


    public MonthService(Context context) {
        this.context = context;
        this.dbHelper = new ExpenseSQLite(context);
        this.dbRead = dbHelper.getReadableDatabase();
        this.dbWrite = dbHelper.getWritableDatabase();
        this.expenseService = new ExpenseService(context);
        this.depositService = new DepositService(context);
        this.meterService = new MeterService(context);
    }


    /**
     * Синхронизирует таблицу month с данными из expense и deposit
     * Создаёт отсутствующие записи в month и связывает с ними deposit
     */
    public void syncMonths(Long typeId) {
        Log.d(TAG, "Начало синхронизации месяцев");


        /** Получает все уникальные месяцы из expense и deposit */
        List<Month> monthsFromData = getAllMonthsWithData(typeId);


        for (Month month : monthsFromData) {

            /** Проверяем, есть ли такой месяц в таблице month */
            Month existingMonth = findMonthInDb(month.getYear(), month.getMonth(), month.getTypeId());

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


/** Реализация для MonthAdminActivity : */
    /**
     * Связывает новый созданный в БД deposit с месяцем (если месяца нет - создаёт его)
     * @param deposit объект Deposit для связки
     * @return успешно ли связана запись Deposit с месяцем
     */
    public boolean linkDeposit(Deposit deposit) {
        Log.d(TAG, "linkDeposit: " + deposit.getName() + ", typeId=" + deposit.getTypeId() +
                ", expenseId=" + deposit.getExpenseId());

        boolean isSuccess = true;

        long id = deposit.getId();

        /** Получает дату из deposit */
        String dateStr;                                                                             /** A */
        if (deposit.getDateTime() != null) {                                                        /** A */
            dateStr = deposit.getDateTime().format(Util.dateFormatterInsert);                       /** A */
        } else {                                                                                    /** A */
            dateStr = java.time.ZonedDateTime.now().format(Util.dateFormatterInsert);               /** A */
        }                                                                                           /** A */


        /** Извлекает месяц и год из даты (формат "dd.MM.yy") */
        try {

            int month = Integer.parseInt(dateStr.substring(3, 5));                                  /** A */
            int year = 2000 + Integer.parseInt(dateStr.substring(6, 8));                            /** A */

            Log.d(TAG, "Для deposit с type==1 определяем месяц: " + month + "/" + year);            /** A */

            /** Получает или создаём объект месяца */
            Month monthObj = getOrCreateMonth(year, month, TYPE_MONTHLY_EXPENSES);                  /** A */

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
                    isSuccess = false;
                }

            } else {
                Log.e(TAG, "Не удалось получить/создать месяц для " + month + "/" + year);
                isSuccess = false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении ссылки на месяц для Deposit: " + deposit, e);
        }

        return isSuccess;
    }
/** !Реализация для MonthAdminActivity */


    /** Проверяет наличие месяца в таблице month */
    private Month findMonthInDb(int year, int month, Long typeId) {

        Cursor cursor = dbRead.rawQuery(
                "SELECT * FROM " + ExpenseSQLite.TABLE_MONTH +
                        " WHERE " + ExpenseSQLite.MONTH_YEAR + " = " + year +
                        " AND " + ExpenseSQLite.MONTH_MONTH + " = " + month +
                " AND " + ExpenseSQLite.MONTH_MONTH_TYPE_ID + " = " + typeId,
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
        cv.put(ExpenseSQLite.MONTH_MONTH_TYPE_ID, month.getTypeId());

        Log.d(TAG, "insertMonth: year=" + month.getYear() +
                ", month=" + month.getMonth() +
                ", typeId=" + month.getTypeId());

        return dbWrite.insert(ExpenseSQLite.TABLE_MONTH, null, cv);
    }


    /** Преобразует курсор в объект Month */
    private Month cursorToMonth(Cursor cursor) {

        Month month = new Month();
        month.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseSQLite.MONTH_ID)));

        int year = cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseSQLite.MONTH_YEAR));
        int monthValue = cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseSQLite.MONTH_MONTH));
        long typeId = cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseSQLite.MONTH_MONTH_TYPE_ID));

        if (monthValue < 1 || monthValue > 12) {
            Log.e(TAG, "⚠️ В БД найден некорректный месяц: " + monthValue + " для ID=" + month.getId() +
                    ". Исправляем на текущий месяц.");
            monthValue = java.time.ZonedDateTime.now().getMonthValue();
        }

        month.setYear(year);
        month.setMonth(monthValue);
        month.setTypeId(typeId);

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
    public Month getOrCreateMonth(int year, int month, Long typeId) {

        /** Сначала ищет в БД */
        Month existingMonth = findMonthInDb(year, month, typeId);
        if (existingMonth != null) {
            return existingMonth;
        }


        /** Если нет - создаёт и сохраняет */
        Month newMonth = new Month(year, month, typeId);
        long id = insertMonth(newMonth);
        newMonth.setId(id);


        return newMonth;

    }


    /** Получает все месяцы, для которых есть расходы (typeId == 1) */
    public List<Month> getMonthsWithExpenses(Long typeId) {

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
            months.add(new Month(year, month, typeId));
        }

        cursor.close();


        return months;
    }


    /** Получает все месяцы, для которых есть взносы (typeId == 1) */
    public List<Month> getMonthsWithDeposits(Long typeId) {

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
            months.add(new Month(year, month, typeId));
        }

        cursor.close();


        return months;
    }


    /** Получает все месяцы, для которых есть данные (расходы или взносы) */
    public List<Month> getAllMonthsWithData(Long typeId) {

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
            Month dbMonth = findMonthInDb(year, month, typeId);

            if (dbMonth != null) {
                months.add(dbMonth); /** добавляет с ID из БД */
            } else {
                months.add(new Month(year, month, typeId)); /** добавляет без ID */
            }

        }

        cursor.close();

        return months;
    }


    /** Получает расходы за указанный месяц */
    private List<Expense> getExpensesForMonth(Month month) {                                        /** TODO : сделал копию, вынес expenseType */

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


        /** Получает планируемый возврат (отдельным запросом) */
        double plannedReturnAmount = 0.0;
        if (month.getId() != null) plannedReturnAmount = getPlannedReturnForMonth(month.getId());
        dto.setPlannedReturnAmount(plannedReturnAmount);

        Log.d(TAG, "Month ID=" + month.getId() + ", plannedReturn=" + plannedReturnAmount);


        /** Рассчитываем баланс */
        dto.setBalance(totalDepositAmount - totalExpenseAmount);

        return dto;
    }


    /** Получает все имеющиеся MonthlyDto, для всех месяцев, для последующего вывода в интерфейсе Активити */
    public List<MonthlyDto> getAllMonthlyDtos(Long typeId) {

        List<Month> months = getAllMonthsWithData(typeId);
        List<MonthlyDto> dtos = new ArrayList<>();

        for (Month month : months) dtos.add(getMonthlyDto(month));

        return dtos;
    }


/** Реализация для MonthlyExpensePlanning : */
    /** Получает все имеющиеся MonthlyExpensePlanningDto, для всех месяцев,
     * для последующего вывода в интерфейсе Активити */
    public List<MonthlyExpensePlanningDto> getAllMonthlyExpensePlanningDtos(Long typeId, Long expenseType, Long depositType) {
        Log.d("getAllMonthlyExpensePlannyngDtos", "startMethod, typeId = " + typeId +
                ", expenseType = " + expenseType + ", depositType = " + depositType);

        List<Month> monthList = getMonthList(typeId);

        return getMonthlyExpensePlanningDtoList(monthList, expenseType, depositType) ;
    }


    /** @param typeId Тип месяца, записи которого необходимо получить
     * @return Все месяцы указанного типа, записи которого сохранены в БД */
    public List<Month> getMonthList(Long typeId) {
        Log.d("getMonthList", "startMethod, typeId = " + typeId);

        List<Month> months = new ArrayList<>();

        String query =
                "SELECT " +
                    " * " +
                "FROM " + ExpenseSQLite.TABLE_MONTH + " " +
                "WHERE " + ExpenseSQLite.MONTH_MONTH_TYPE_ID + " = " + typeId + " " +
//                    "AND " + ExpenseSQLite.EXPENSE_IS_DELETED + " = 0 " +
                "ORDER BY year DESC, month DESC";

        Cursor cursor = dbRead.rawQuery(query, null);

        while (cursor.moveToNext()) {
            months.add(cursorToMonth(cursor));
        }

        cursor.close();

        return months;
    }


    // todo: переделать на вызов getMonthlyExpensePlanningDtoList(List<Month> monthList, Long expenseType, Long depositType)
    public List<MonthlyExpensePlanningDto> getMonthlyExpensePlanningDtoList(
            List<Month> monthList, Long expenseType, Long depositType) {
        Log.d("getMonthlyExpensePlanningDtoList", "startMethod, monthList: " + monthList +
                ", expenseType = " + expenseType + ", depositType = " + depositType);

        List<MonthlyExpensePlanningDto> dtos = new ArrayList<>();
        for (Month month : monthList) dtos.add(getMonthlyExpensePlanningDto(month, expenseType, depositType));

        return dtos;
    }


    /** Реализация для MonthlyExpensePlanning : */
    /** Получает все имеющиеся MonthlyExpensePlanningDto, для всех месяцев,
     * для последующего вывода в интерфейсе Активити */
    public List<MonthUtilityServiceDto> getAllMonthUtilityServiceDtos(Long typeId, Long expenseType, Long depositType) {
        Log.d("getAllMonthUtilityServiceDtos", "startMethod, typeId = " + typeId + ", expenseType: " + expenseType + ", depositType: " + depositType);

        List<Month> monthList = getMonthList(typeId);

        /*
        List<MonthlyExpensePlannyngDto> dtos = new ArrayList<>();
        for (Month month : months) dtos.add(getMonthlyExpensePlannyngDto(month));
        */

        return getMonthUtilityServiceDtoList(monthList, expenseType, depositType) ;
    }


    public List<MonthUtilityServiceDto> getMonthUtilityServiceDtoList(List<Month> monthList, Long expenseType, Long depositType) {
        Log.d("getMonthlyExpensePlanningDtoList", "startMethod, monthList: " + monthList + ", expenseType: " + expenseType + ", depositType: " + depositType);

        List<MonthUtilityServiceDto> dtos = new ArrayList<>();
        for (Month month : monthList) dtos.add(getMonthUtilityServiceDto(month, expenseType, depositType));

        return dtos;
    }

    /** Получает MonthlyExpensePlannyngDto для переданного в month месяца
     * @param month Месяц, для которого собираются его запланированные расходы */
    public MonthUtilityServiceDto getMonthUtilityServiceDto(Month month, Long expenseType, Long depositType) {
        Log.d("getMonthlyExpensePlannyngDto", "startMethod, month: " + month + ", expenseType: " + expenseType + ", depositType: " + depositType);

        MonthUtilityServiceDto dto = new MonthUtilityServiceDto(month);


        /** Получает расходы за месяц */
        List<Expense> expenses = getExpensesForMonth(month, expenseType);
        dto.setExpenseList(expenses);


/** Рассчитывает статистику по расходам : */
        double totalExpenseAmount = 0;
        int paymentsCount = 0;
//        double totalDepositAmount = 0;
//        int depositCount = 0;
//        int depositPaymentsCount = 0;

        for (Expense expense : expenses) {

//            /** Получает внесённые к каждой из имеющихся Expense взносы (typeId == 6) */
//            List<Deposit> deposits = depositService.getExpenseDeposits(
//                    expense.getId(), depositType);
//            expense.setDepositList(deposits);


            /** Рассчитываем статистику : */
            /** 1. По расходам : */
            totalExpenseAmount += expense.getExpenseListTotalAmount();
            if (expense.getExpenseList() != null) paymentsCount += expense.getExpenseList().size();

//            /** 2. Рассчитываем статистику по уже выполненным взносам */
//            for (Deposit deposit : deposits) {
//                totalDepositAmount += deposit.getTotalAmount();
//                depositCount++;
//            }
            /** !Рассчитываем статистику */


        }

        dto.setTotalExpenseAmount(totalExpenseAmount);
        dto.setExpensesCount(expenses.size());
        dto.setPaymentsCount(paymentsCount);
//        dto.setTotalDepositAmount(totalDepositAmount);
//        dto.setDepositsCount(depositCount);
//        dto.setBalance(totalDepositAmount - totalExpenseAmount);
/** !Рассчитывает статистику по расходам */


        /** Получает расходы за месяц */
        List<Meter> meterList = getMetersForMonth(month);
        dto.setMeterList(meterList);

        Log.d("getMonthlyExpensePlannyngDto",
                "endMethod, к возврату MonthlyExpensePlannyngDto: " + dto);
        return dto;
    }


    /** Получает все Показания за указанный в month месяц */
    private List<Meter> getMetersForMonth(Month month) {
        Log.d("getMetersForMonth", "startMethod, month: " + month);
        return meterService.getMeterList(month.getId());
    }


    // todo: Переделать на вызов getMonthlyExpensePlanningDto(Month month, Long expenseType, Long depositType)
    /** Получает MonthlyExpensePlannyngDto для переданного в month месяца
     * @param month Месяц, для которого собираются его запланированные расходы */
    public MonthlyExpensePlanningDto getMonthlyExpensePlanningDto(
            Month month, Long expenseType, Long depositType) {
        Log.d("getMonthlyExpensePlannyngDto", "startMethod, month: " + month +
                ", expenseType = " + expenseType + ", depositType = " + depositType);


        MonthlyExpensePlanningDto dto = new MonthlyExpensePlanningDto(month);


        /** Получает расходы за месяц */
        List<Expense> expenses = getExpensesForMonth(month, expenseType);
        dto.setExpenseList(expenses);


/** Рассчитывает статистику по расходам : */
        double totalExpenseAmount = 0;
        int paymentsCount = 0;
        double totalDepositAmount = 0;
        int depositCount = 0;
//        int depositPaymentsCount = 0;

        for (Expense expense : expenses) {

            /** Получает внесённые к каждой из имеющихся Expense взносы (typeId == 6) */
            List<Deposit> deposits = depositService.getExpenseDeposits(
                    expense.getId(), depositType);

            expense.setDepositList(deposits);


    /** Рассчитываем статистику : */
            /** 1. По планируемому бюджету : */
            totalExpenseAmount += expense.getExpenseListTotalAmount();
            if (expense.getExpenseList() != null) paymentsCount += expense.getExpenseList().size();

            /** 2. Рассчитываем статистику по уже выполненным взносам */
            for (Deposit deposit : deposits) {
                totalDepositAmount += deposit.getTotalAmount();
                depositCount++;
            }
    /** !Рассчитываем статистику */


        }

        dto.setTotalExpenseAmount(totalExpenseAmount);
        dto.setExpensesCount(expenses.size());
        dto.setPaymentsCount(paymentsCount);
        dto.setTotalDepositAmount(totalDepositAmount);
        dto.setDepositsCount(depositCount);

        /** ___. Рассчитываем текущий баланс */
        dto.setBalance(totalDepositAmount - totalExpenseAmount);
/** !Рассчитывает статистику по расходам */


        Log.d("getMonthlyExpensePlannyngDto",
                "endMethod, к возврату MonthlyExpensePlannyngDto: " + dto);
        return dto;
    }


    /** Получает расходы за указанный в month месяц, для указанного в expenseType типа расходоа */
    private List<Expense> getExpensesForMonth(Month month, Long expenseType) {
        Log.d("getExpensesForMonth", "startMethod, month: " + month +
                ", expenseType = " + expenseType);

        List<Expense> expenses = new ArrayList<>();

        String monthStr = String.format("%02d", month.getMonth());
        String yearStr = String.valueOf(month.getYear()).substring(2);                    /** 2026 -> "26" */


        /** Получает все расходы за месяц */
        ArrayList<Expense> allExpenses = expenseService.getExpenseList(expenseType);


        /** Фильтрует по указанному в month месяцу */
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


    @Deprecated
    public Month getOrCreatePlanningExpenseMonth(Expense expense) {                                 /***/
        Log.d(TAG, "getOrCreatePlanningExpenseMonth: Expense name = " + expense.getName() +
                ", typeId =" + expense.getTypeId() + ", expenseId =" + expense.getId());


        return getOrCreatePlanningExpenseMonth(expense.getDateTime());


//        /** Получает дату из expense */
//        String dateStr;                                                                             /** A */
//        if (expense.getDateTime() != null) {                                                        /** A */
//            dateStr = expense.getDateTime().format(Util.dateFormatterInsert);                       /** A */
//        } else {                                                                                    /** A */
//            dateStr = java.time.ZonedDateTime.now().format(Util.dateFormatterInsert);               /** A */
//        }                                                                                           /** A */
//
//
//        /** Извлекает месяц и год из даты (формат "dd.MM.yy") */
//        Integer monthInt = null;
//        Integer yearInt = null;
//        try {
//            monthInt = Integer.parseInt(dateStr.substring(3, 5));                                   /** A */
//            yearInt = 2000 + Integer.parseInt(dateStr.substring(6, 8));                             /** A */
//        } catch (Exception e) {
//            Log.e(TAG, "Ошибка парсинга даты dateStr: " + dateStr, e);
//        }
//
//
//        /** Получает или создаём объект месяца */
//        Month month = null;
//
//        if(monthInt != null && yearInt != null) {
//            month = getOrCreateMonth(yearInt, monthInt, TYPE_MONTHLY_EXPENSE_PLANNYNG);
//        } else {
//            Log.e(TAG, "Ошибка даты, месяц не был запрошен/создан");
//        }
//
//
//        return month;
    }



    public Month getOrCreateExpenseMonth(Expense expense, Long monthTypeId) {
        Log.d(TAG, "getOrCreatePlanningExpenseMonth: Expense name = " + expense.getName() +
                ", typeId =" + expense.getTypeId() + ", expenseId =" + expense.getId() +
                ", monthTypeId = " + monthTypeId);

        return getOrCreateExpenseMonth(expense.getDateTime(), monthTypeId);
    }


    public Month getOrCreateExpenseMonth(ZonedDateTime createDate, Long monthTypeId) {
        Log.d(TAG, "getOrCreatePlanningExpenseMonth: createDate = " + createDate + ", monthTypeId = " + monthTypeId);


// TODO: Переписать на прямое получение monthInt и yearInt из ZDT :
        /** Получает дату : */
        String dateStr;                                                                             /** A */
        if (createDate != null) {                                                                   /** A */
            dateStr = createDate.format(Util.dateFormatterInsert);                                  /** A */
        } else {                                                                                    /** A */
            dateStr = java.time.ZonedDateTime.now().format(Util.dateFormatterInsert);               /** A */
        }                                                                                           /** A */


        /** Извлекает месяц и год из даты (формат "dd.MM.yy") */
        Integer monthInt = null;
        Integer yearInt = null;
        try {
            monthInt = Integer.parseInt(dateStr.substring(3, 5));                                   /** A */
            yearInt = 2000 + Integer.parseInt(dateStr.substring(6, 8));                             /** A */
        } catch (Exception e) {
            Log.e(TAG, "Ошибка парсинга даты dateStr: " + dateStr, e);
        }
// TODO: !Переписать на прямое получение monthInt и yearInt из ZDT


        /** Получает или создаём объект месяца */
        Month month = null;

        if(monthInt != null && yearInt != null) {
            month = getOrCreateMonth(yearInt, monthInt, monthTypeId);
        } else {
            Log.e(TAG, "Ошибка даты, месяц не был запрошен/создан");
        }


        return month;
    }


    // TODO: перенести вызов на новый метод getOrCreatePlanningExpenseMonth(ZonedDateTime createDate, Long monthTypeId)
    @Deprecated
    public Month getOrCreatePlanningExpenseMonth(ZonedDateTime createDate) {
        Log.d(TAG, "getOrCreatePlanningExpenseMonth: createDate = " + createDate);


// TODO: Переписать на прямое получение monthInt и yearInt из ZDT :
        /** Получает дату : */
        String dateStr;                                                                             /** A */
        if (createDate != null) {                                                                   /** A */
            dateStr = createDate.format(Util.dateFormatterInsert);                                  /** A */
        } else {                                                                                    /** A */
            dateStr = java.time.ZonedDateTime.now().format(Util.dateFormatterInsert);               /** A */
        }                                                                                           /** A */


        /** Извлекает месяц и год из даты (формат "dd.MM.yy") */
        Integer monthInt = null;
        Integer yearInt = null;
        try {
            monthInt = Integer.parseInt(dateStr.substring(3, 5));                                   /** A */
            yearInt = 2000 + Integer.parseInt(dateStr.substring(6, 8));                             /** A */
        } catch (Exception e) {
            Log.e(TAG, "Ошибка парсинга даты dateStr: " + dateStr, e);
        }
// TODO: !Переписать на прямое получение monthInt и yearInt из ZDT


        /** Получает или создаём объект месяца */
        Month month = null;

        if(monthInt != null && yearInt != null) {
            month = getOrCreateMonth(yearInt, monthInt, TYPE_MONTHLY_EXPENSE_PLANNING);
        } else {
            Log.e(TAG, "Ошибка даты, месяц не был запрошен/создан");
        }


        return month;
    }


//    private List<Deposit> getExpenseDeposits(Long expenseId) {
//
//        List<Deposit> deposits = new ArrayList<>();
//
//
//        /** Ищет deposit с type=1, у которых expenseId = ID месяца */
//        Cursor cursor = dbRead.rawQuery(
//                "SELECT * " +
//                        "FROM " + ExpenseSQLite.TABLE_DEPOSIT +
//                        " WHERE " + ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID + " = " + TYPE_MONTHLY_DEPOSIT +
//                            " AND " + ExpenseSQLite.DEPOSIT_IS_DELETED + " = 0" +
//                            " AND " + ExpenseSQLite.DEPOSIT_EXPENSE_ID + " = " + expenseId +
//                            " AND " + ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID + " = " + TYPE_DEPOSIT_MONTH_PLANNING,
//                null);
//
//
//        while (cursor.moveToNext()) {
//            Deposit deposit = cursorToDeposit(cursor);
//            loadPaymentsForDeposit(deposit);
//            deposits.add(deposit);
//        }
//        cursor.close();
//
//
//        return deposits;
//    }


    /**
     * Удаляет месяц типа 3L, со всеми его дочерними расходами и взносами.
     * @param monthlyExpensePlanningDto DTO удаляемого месяца
     * @return true, если удаление успешно, false в противном случае
     */
    public boolean removeMonth(MonthlyExpensePlanningDto monthlyExpensePlanningDto) {
        Log.d("removeMonth",
                "startMethod, MonthlyExpensePlanningDto: " + monthlyExpensePlanningDto);

        if(!checkBeforeRemove(monthlyExpensePlanningDto)) return false;


//        dbWrite.beginTransaction();

        Long monthId = monthlyExpensePlanningDto.getMonth().getId();

        try {

            /** 1. Удаляет все Expense и их Deposit's */
            List<Expense> expenses = monthlyExpensePlanningDto.getExpenseList();

            if (expenses != null && !expenses.isEmpty()) {

                for (Expense expense : expenses) {

                    if (expense.getId() != null) {

                        /** Удаляет депозиты, связанные с этой Expense */
                        List<Deposit> deposits = expense.getDepositList();
                        if (deposits != null && !deposits.isEmpty()) {
                            for (Deposit deposit : deposits) {
                                if (!depositService.deleteDeposit(deposit.getId())) {
                                    Log.e(TAG, "removeMonth: Не удалось удалить депозит ID=" + deposit.getId());
                                    dbWrite.endTransaction();
                                    return false;
                                }
                            }
                        }

                        /** Удаляет сам Expense (и её Payment's) */
                        if (!expenseService.removeExpense(expense)) {
                            Log.e(TAG, "removeMonth: Не удалось удалить расход ID=" + expense.getId());
                            dbWrite.endTransaction();
                            return false;
                        }

                    }

                }

            }


            /** 2. Удаляет запись месяца, соответствие типа проверено на входе в метод, не дублируем */
            int deleted = dbWrite.delete(ExpenseSQLite.TABLE_MONTH,
                    ExpenseSQLite.MONTH_ID + " = ?",
                    new String[]{String.valueOf(monthId)});

            if (deleted == 0) Log.w(TAG, "removeMonth: Месяц с ID " + monthId + " не найден в БД");


//            dbWrite.setTransactionSuccessful();
            Log.d(TAG, "removeMonth: Месяц ID=" + monthId + " успешно удалён");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "removeMonth: Ошибка при удалении месяца", e);
            return false;
        } finally {
//            dbWrite.endTransaction();
        }


    }


    public boolean checkBeforeRemove(MonthlyExpensePlanningDto monthlyExpensePlanningDto) {
        Log.d("checkBeforeRemove",
                "startMethod, MonthlyExpensePlanningDto: " + monthlyExpensePlanningDto);

        if (monthlyExpensePlanningDto == null || monthlyExpensePlanningDto.getMonth() == null) {
            Log.e(TAG, "removeMonth: DTO или месяц равен null");
            return false;
        }

        Month month = monthlyExpensePlanningDto.getMonth();
        Long monthId = month.getId();
        if (monthId == null) {
            Log.e(TAG, "removeMonth: ID месяца равен null");
            return false;
        }

        /** Проверяем тип месяца — удаляем только для планирования (typeId == 3) или TYPE_MONTHLY_CONTRIBUTIONS */
        if (month.getTypeId() != null && !(month.getTypeId() == Util.TYPE_MONTHLY_EXPENSE_PLANNING || month.getTypeId() == Util.TYPE_MONTHLY_CONTRIBUTIONS)) {
            Log.w(TAG, "removeMonth: Месяц имеет тип " + month.getTypeId() +
                    ", не поддерживаемый для удаления через этот метод");
            return false;
        }


        Log.d("checkBeforeRemove",
                "endMethod, к возврату TRUE для MonthlyExpensePlanningDto: " +
                        monthlyExpensePlanningDto);
        return true;
    }
/** !Реализация для MonthlyExpensePlanning */


/** Реализация для MonthUtilityServiceDto : */
    public boolean removeMonth(MonthUtilityServiceDto monthUtilityServiceDto) {
        Log.d("removeMonth",
                "startMethod, monthUtilityServiceDto: " + monthUtilityServiceDto);

        if(!checkBeforeRemove(monthUtilityServiceDto)) return false;


    //        dbWrite.beginTransaction();

        Long monthId = monthUtilityServiceDto.getMonth().getId();

        try {

            /** 1.1 Удаляет все Expense и их Deposit's */
            List<Expense> expenses = monthUtilityServiceDto.getExpenseList();

            if (expenses != null && !expenses.isEmpty()) {

                for (Expense expense : expenses) {

                    if (expense.getId() != null) {

                        /** Удаляет депозиты, связанные с этой Expense */
                        List<Deposit> deposits = expense.getDepositList();
                        if (deposits != null && !deposits.isEmpty()) {
                            for (Deposit deposit : deposits) {
                                if (!depositService.deleteDeposit(deposit.getId())) {
                                    Log.e(TAG, "removeMonth: Не удалось удалить депозит ID=" + deposit.getId());
                                    dbWrite.endTransaction();
                                    return false;
                                }
                            }
                        }

                        /** Удаляет сам Expense (и её Payment's) */
                        if (!expenseService.removeExpense(expense)) {
                            Log.e(TAG, "removeMonth: Не удалось удалить расход ID=" + expense.getId());
                            dbWrite.endTransaction();
                            return false;
                        }

                    }

                }

            }


            /** 1.2 Удаляет все Meter : */
            List<Meter> meterList = monthUtilityServiceDto.getMeterList();

            if (meterList != null && !meterList.isEmpty()) {

                for (Meter meter : meterList) {

                    if (meter.getId() != null) {

                        if (!meterService.removeMeter(meter.getId())) {
                            Log.e(TAG, "removeMonth: Не удалось удалить запись Показаний с Id=" + meter.getId());
                            dbWrite.endTransaction();
                            return false;
                        }

                    }

                }

            }


            /** 2. Удаляет запись месяца, соответствие типа проверено на входе в метод, не дублируем */
            int deleted = dbWrite.delete(ExpenseSQLite.TABLE_MONTH,
                    ExpenseSQLite.MONTH_ID + " = ?",
                    new String[]{String.valueOf(monthId)});

            if (deleted == 0) Log.w(TAG, "removeMonth: Месяц с ID " + monthId + " не найден в БД");


    //            dbWrite.setTransactionSuccessful();
            Log.d(TAG, "removeMonth: Месяц ID=" + monthId + " успешно удалён");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "removeMonth: Ошибка при удалении месяца", e);
            return false;
        } finally {
    //            dbWrite.endTransaction();
        }


    }


    public boolean checkBeforeRemove(MonthUtilityServiceDto monthUtilityServiceDto) {
        Log.d("checkBeforeRemove",
                "startMethod, MonthUtilityServiceDto: " + monthUtilityServiceDto);

        if (monthUtilityServiceDto == null || monthUtilityServiceDto.getMonth() == null) {
            Log.e(TAG, "removeMonth: DTO или месяц равен null");
            return false;
        }

        Month month = monthUtilityServiceDto.getMonth();
        Long monthId = month.getId();
        if (monthId == null) {
            Log.e(TAG, "removeMonth: ID месяца равен null");
            return false;
        }

        /** Проверяем тип месяца — удаляем только для КоммунальныхУслуг (typeId == 2) */
        if (month.getTypeId() != null && month.getTypeId() != Util.TYPE_MONTHLY_UTILITY_BILLS) {
            Log.w(TAG, "removeMonth: Месяц имеет тип " + month.getTypeId() +
                    ", не поддерживаемый для удаления через этот метод");
            return false;
        }


        Log.d("checkBeforeRemove",
                "endMethod, к возврату TRUE для MonthUtilityServiceDto: " +
                        monthUtilityServiceDto);
        return true;
    }
/** !Реализация для MonthUtilityServiceDto */


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


    /**
     * Получает планируемый возврат для указанного месяца (typeId == 5)
     * @param monthId ID месяца
     * @return сумма планируемого возврата
     */
    public double getPlannedReturnForMonth(long monthId) {

        double plannedReturn = 0.0;

        Cursor cursor = dbRead.rawQuery(
                "SELECT " + ExpenseSQLite.DEPOSIT_PAYMENT +
                        " FROM " + ExpenseSQLite.TABLE_DEPOSIT_PAYMENT + " dp" +
                        " JOIN " + ExpenseSQLite.TABLE_DEPOSIT + " d ON dp." + ExpenseSQLite.DEPOSIT_PAYMENT_DEPOSIT_ID +
                        " = d." + ExpenseSQLite.DEPOSIT_ID +
                        " WHERE d." + ExpenseSQLite.DEPOSIT_EXPENSE_ID + " = " + monthId +
                        " AND d." + ExpenseSQLite.DEPOSIT_DEPOSIT_TYPE_ID + " = " + TYPE_MONTHLY_DEPOSIT_PLAN + /** тип 5 = Планируемый возврат */
                        " AND d." + ExpenseSQLite.DEPOSIT_IS_DELETED + " = 0",
                null);


        while (cursor.moveToNext()) {
            plannedReturn += cursor.getDouble(0);
        }

        cursor.close();

        return plannedReturn;
    }


}