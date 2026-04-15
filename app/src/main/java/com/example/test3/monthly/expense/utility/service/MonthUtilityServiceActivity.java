package com.example.test3.monthly.expense.utility.service;

import static com.example.test3.util.Util.EXTRA_EXPENSE_TYPE;
import static com.example.test3.util.Util.TYPE_EXPENSE_MONTH_PLANNING;
import static com.example.test3.util.Util.TYPE_EXPENSE_UTILITY_BILLS;
import static com.example.test3.util.Util.TYPE_MONTHLY_UTILITY_BILLS;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.ExpenseDetailActivity;
import com.example.test3.R;
import com.example.test3.expenseList.Expense;
import com.example.test3.month.Month;
import com.example.test3.monthly.expense.planning.MonthExpenseExpandableAdapter;
import com.example.test3.monthly.expense.planning.MonthlyExpensePlanningDto;
import com.example.test3.service.ExpenseService;
import com.example.test3.service.MeterService;
import com.example.test3.service.MonthService;
import com.example.test3.util.UtilService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class MonthUtilityServiceActivity extends AppCompatActivity {

    private static final String TAG = "MonthUtilityBillsActivity";

    private MonthService monthService;

    private ExpenseService expenseService;

    private MeterService meterService;

//    private DepositService depositService;

    private Long monthType = TYPE_MONTHLY_UTILITY_BILLS;
    private Long expenseType = TYPE_EXPENSE_UTILITY_BILLS;
    private Long depositType = -1L; /** Для КоммунальныхУслуг Deposit не предусмотрены */


    private List<MonthUtilityServiceDto> monthDtoList;
//    private List<MonthlyExpensePlanningDto> monthDtoList;
    private ExpandableListView expandableListView;
    /* private ListView listView; */
    private /* MonthExpensePlanningAdapter */ MonthUtilityServiceAdapter adapter;


    private MonthlyExpensePlanningDto selectedMonthDto = null;
    private int selectedMonthPosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MonthUtilityServiceActivity " + getCurrentMethodName(), " startMethod");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_utility_service);

//        monthType = getIntent().getLongExtra(EXTRA_EXPENSE_TYPE, 5L);

        monthService = new MonthService(this);
        expenseService = new ExpenseService(this);
        meterService = new MeterService(this);
//        depositService = new DepositService(this);

        expandableListView = findViewById(R.id.expandableListView);
        expandableListView
                .setOnGroupClickListener((parent, v, groupPosition, id) -> {

                    selectedMonthPosition = groupPosition;

                    adapter.setSelectedGroupPosition(groupPosition);                                /** Обновляет подсветку выбранной строки */

                    Toast.makeText(this,
                            "Выбран месяц: " + monthDtoList.get(groupPosition).getMonth().getMonthYear(),
                            Toast.LENGTH_SHORT).show();



                    return true; /** Для да/нет у сворачивания/разворачивания группы */

                });
        /* listView = findViewById(R.id.monthList); */

        loadData();

        Log.d("loadExpenses" + getCurrentMethodName(), "endMethod");
    }


    private void loadData() {
        Log.d(TAG, "loadData startMethod");

        monthDtoList = monthService.getAllMonthUtilityServiceDtos(monthType, expenseType, depositType);

        if (monthDtoList.isEmpty()) {
            Toast.makeText(this, "Нет ни одного месяца с данными", Toast.LENGTH_LONG).show();
        }


        adapter = new MonthUtilityServiceAdapter /* MonthExpensePlanningAdapter */ (this, monthDtoList);

        /*
        adapter.setOnItemClickListener((dto, position) -> {

            Intent intent = new Intent(MonthExpensePlanningActivity.this, MonthExpenseDetailActivity.class);
            intent.putExtra(MonthExpenseDetailActivity.EXTRA_MONTH_DTO, dto);
            startActivity(intent);


//            selectedMonthDto = dto;
//            adapter.setSelectedPosition(position);


            Toast.makeText(MonthExpensePlanningActivity.this,
                    "Выбран: " + dto.getMonth().getMonthYear(),
                    Toast.LENGTH_SHORT).show();

        });
        */


        expandableListView.setAdapter(adapter);
        /* listView.setAdapter(adapter); */


        if (monthDtoList != null && !monthDtoList.isEmpty()) {

            /** Раскрывает все группы (для наглядности) */
            int groupCount = adapter.getGroupCount();
            for (int i = 0; i < groupCount; i++) {
                expandableListView.expandGroup(i);
            }

        }


        adapter.setSelectedGroupPosition(-1);
        selectedMonthPosition = -1;                                                                 /** сбрасывает выбор месяца */


        /** Обработчик нажатия кнопки (+) на заголовке месяца */
        adapter.setOnAddExpenseClickListener(this::showAddExpenseDialogForMonth);                   //  adapter.setOnAddExpenseClickListener(dto -> { showAddExpenseDialogForMonth(dto); });


        /** Обработчик нажатия самого заголовка месяца */
        adapter.setOnGroupClickListener(groupPosition -> {

            Log.d("loadExpenses" + getCurrentMethodName(), "click setOnGroupClickListener() start");

            selectedMonthPosition = groupPosition;
            adapter.setSelectedGroupPosition(groupPosition);
            Toast.makeText(this,
                    "Выбран месяц: " + monthDtoList.get(groupPosition).getMonth().getMonthYear(),
                    Toast.LENGTH_SHORT).show();

        });


        /** Здесь пробрасывается из Адаптера из строки addMeterReadingListener.onAddMeterReading(dto); */
        /** 13.04 Устанавливает слушателя для кнопки добавить (+) для ПередачиПоказаний */
        adapter.setOnAddMeterReadingListener(this::showAddMeterReadingDialog);


        //
         /*
        expandableListView.invalidateViews();
        expandableListView.requestLayout();
        */
        //


//        updateTotalStats();

        Log.d(TAG, "loadData endMethod");
    }


    private void updateTotalStats() {

        double totalExpenses = 0;
        /*
        double totalDeposits = 0;
        double totalBalance = 0;
        */


        for (MonthUtilityServiceDto dto : monthDtoList) {
            totalExpenses += dto.getTotalExpenseAmount();
//            totalDeposits += dto.getTotalDepositAmount();
//            totalBalance += dto.getBalance();
        }

        /*
        DecimalFormat df = new DecimalFormat("#,##0.00");
        textViewTotalExpenses.setText("Расходы: " + df.format(totalExpenses) + " руб.");
        textViewTotalDeposits.setText("Взносы: " + df.format(totalDeposits) + " руб.");

        String balanceStr = "Баланс: " + df.format(totalBalance) + " руб.";
        textViewTotalBalance.setText(balanceStr);

        if (totalBalance > 0) {
            textViewTotalBalance.setTextColor(getColor(android.R.color.holo_green_dark));
        } else if (totalBalance < 0) {
            textViewTotalBalance.setTextColor(getColor(android.R.color.holo_red_dark));
        } else {
            textViewTotalBalance.setTextColor(getColor(android.R.color.darker_gray));
        }
        */

    }


    /*
    private void clearSelection() {

        selectedMonthDto = null;

        if (listView != null) {
            listView.clearChoices();
            adapter.notifyDataSetChanged();
        }

    }
    */


    public void add(View view) {
        Log.d(TAG + getCurrentMethodName(), "startMethod");

        EditText expenseNameEditText = findViewById(R.id.editTextNameExpense);
        String expenseName = expenseNameEditText.getText().toString();

        EditText expenseEditText = findViewById(R.id.editTextNumberDecimal);
        Double expense = null;
        if (expenseEditText.getText() != null && !expenseEditText.getText().toString().isEmpty())
            expense = Double.parseDouble(expenseEditText.getText().toString());

        EditText expenseDateEditText = findViewById(R.id.editTextDate);
        String expenseDateTimeString = expenseDateEditText.getText().toString();

        String expenseDescription = null;

        Expense newExpense = null;
        if (!expenseName.isEmpty()) {
            newExpense = getNewExpense(expenseName, expense, expenseDateTimeString, expenseDescription);
            expenseService.insertExpense(newExpense);
        }

        if (newExpense != null) monthService.getOrCreateExpenseMonth(newExpense, monthType);

        loadData();
        cleanUserInput(expenseNameEditText, expenseEditText, expenseDateEditText);
        Log.d(TAG + getCurrentMethodName(), "endMethod");
    }


    public void remove(View view){
        Log.d(TAG + getCurrentMethodName(), "startMethod");

        if (selectedMonthPosition == -1) {
            Toast.makeText(this, "Выберите месяц для удаления", Toast.LENGTH_SHORT).show();
            return;
        }

        MonthUtilityServiceDto dto = monthDtoList.get(selectedMonthPosition);

        Log.d(TAG + getCurrentMethodName(),
                "Получен к удалению MonthlyExpensePlanningDto: ".concat(dto.toString()));

        boolean success = monthService.removeMonth(dto);


        if (success) {
            Toast.makeText(this, "Расход удалён", Toast.LENGTH_SHORT).show();
            loadData();
        } else {
            Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
        }


        Log.d(TAG + getCurrentMethodName(), "endMethod");
    }


    public Expense getNewExpense(String expenseName, Double expense,
                                 String expenseDateTimeString, String expenseDescription) {
        Log.d(TAG + getCurrentMethodName(), "startMethod");

        if(expenseName != null && !expenseName.isEmpty()) {

            Expense newExpense = new Expense(expenseName, expenseType);

            if(expense != null && !expense.isNaN()) newExpense.addPayment(expense);

            if(expenseDateTimeString != null && !expenseDateTimeString.isEmpty()) {
                ZonedDateTime expenseZonedDateTime = getZoneDateTime(expenseDateTimeString);
                if(expenseZonedDateTime != null) newExpense.setDateTime(expenseZonedDateTime);
            }

            if(expenseDescription != null && !expenseDescription.isEmpty()) newExpense.setDescription(expenseDescription);

            Log.d(TAG + getCurrentMethodName(),
                    "endMethod к возврату newExpense: " + newExpense);
            return newExpense;
        }


        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        /** Обновляет список при каждом возврате на главную активити */
        loadData();
    }


    public ZonedDateTime getZoneDateTime(String dateString) {
        Log.d(TAG + getCurrentMethodName(), "startMethod");

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");

        Date date = null;
        ZonedDateTime zonedDateTime = null;

        try {

            date = formatter.parse(dateString);

            zonedDateTime = date.toInstant().atZone(ZonedDateTime.now().getZone());

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Log.d(TAG + getCurrentMethodName(),
                "endMethod, к возврату zonedDateTime: " + zonedDateTime);
        return zonedDateTime;
    }


    public void cleanUserInput(EditText expenseNameEditText, EditText expenseEditText, EditText expenseDateEditText) {
        Log.d(TAG + getCurrentMethodName(), "startMethod");

        expenseNameEditText.setText("");
        expenseEditText.setText("");
        expenseDateEditText.setText("");

        Log.d(TAG + getCurrentMethodName(), "endMethod");
    }


    public void back(View view) {
        finish();
    }


    private void showAddExpenseDialogForMonth(MonthUtilityServiceDto dto) {
        Log.d(TAG + getCurrentMethodName(),"startMethod, MonthUtilityServiceDto: " + dto);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить расход в " + dto.getMonth().getMonthYear());

        View view = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        EditText editTextName = view.findViewById(R.id.editTextExpenseName);
        EditText editTextAmount = view.findViewById(R.id.editTextExpenseAmount);
        EditText editTextDescription = view.findViewById(R.id.editTextExpenseDescription);

        builder.setView(view);
        builder.setPositiveButton("Добавить", (dialog, which) -> {

            String name = editTextName.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();


            /** Проверяем введённые в поля значения : */
            if (name.isEmpty()) {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
                return;
            }
            /***/


            try {

                double amount = Double.parseDouble(amountStr);
                Expense expense = new Expense(name, expenseType);
                expense.addPayment(amount);
                if (!description.isEmpty()) expense.setDescription(description);

                /** Устанавливает дату на первое число выбранного месяца */
                ZonedDateTime firstDayOfMonth = getFirstDayOfMonth(dto.getMonth());
                /*
                ZonedDateTime firstDayOfMonth = ZonedDateTime.now()
                        .withYear(dto.getMonth().getYear())
                        .withMonth(dto.getMonth().getMonth())
                        .withDayOfMonth(1)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
                */

                expense.setDateTime(firstDayOfMonth);


                expenseService.insertExpense(expense);
                loadData();
                Toast.makeText(this, "Расход добавлен", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректная сумма", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Отмена", null);
        builder.show();

        Log.d(TAG + getCurrentMethodName(),"endMethod lkz MonthlyExpensePlanningDto: " + dto);
    }


    private ZonedDateTime getFirstDayOfMonth(Month month) {
        Log.d(TAG + getCurrentMethodName(), getCurrentMethodName() + "startMethod, Month: " + month);

        /** Дата на первое число полученного месяца */
        ZonedDateTime firstDayOfMonth = ZonedDateTime.now()
                .withYear(month.getYear())
                .withMonth(month.getMonth())
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        Log.d(TAG + getCurrentMethodName(),
                "endMethod, firstDayOfMonth: " + firstDayOfMonth + " для Month: " + month);
        return firstDayOfMonth;
    }


    public static String getCurrentMethodName() {
        return new Throwable().getStackTrace()[1].getMethodName();
    }


//    /*
    public void updateMonthExpensePlanning(View view) {
        Log.d(TAG, "startMethod");

        if (selectedMonthPosition == -1) {
            Toast.makeText(this, "Выберите месяц для изменения", Toast.LENGTH_SHORT).show();
            return;
        }

        MonthUtilityServiceDto dto = monthDtoList.get(selectedMonthPosition);
        Toast.makeText(this, "Для изменения выбран месяц " + dto.getMonth().getMonthYear(), Toast.LENGTH_LONG).show();


        // изменить активити на правильную новую, для изменения строки с месяцем :
        /*
        Intent intent = new Intent(this, ExpenseDetailActivity.class);
        intent.putExtra("expense_id", dto.getMonth().getId());
        startActivity(intent);
        */
        // !изменить активити на правильную новую, для изменения строки с месяцем


        Log.d(TAG, "endMethod");
    }
//    */


    /** Для передачи показаний : */
    /** В т.ч. вызывается из Адаптера из строки addMeterReadingListener.onAddMeterReading(dto); */
    private void showAddMeterReadingDialog(MonthUtilityServiceDto dto) {
        Log.d(TAG + "showAddMeterReadingDialog() ", "startMethod, dto: " + dto);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Добавить показания для " + dto.getMonth().getMonthYear());

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_meter_reading, null);
        EditText editName = view.findViewById(R.id.editName);
        EditText editCurrent = view.findViewById(R.id.editCurrentValue);

        builder.setView(view);
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            Log.d(TAG + "showAddMeterReadingDialog() ", "push buttonSave start");

            /** Валидация : */
            if (!isValidMeter(editName, editCurrent)) return;

            String nameStr = editName.getText().toString().trim();
            double currentValue = Double.parseDouble(editCurrent.getText().toString().trim());

            /** Cохранение : */
            meterService.insertMeter(nameStr, currentValue, dto.getMonth().getId());

            /** Обновляет отображение, после изменения */
            loadData();

            Log.d(TAG + "showAddMeterReadingDialog() ", "push buttonSave end");
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();

        Log.d(TAG + "endMethod() ", "startMethod, dto: " + dto);
    }


    public boolean isValidMeter(EditText editName, EditText editCurrent) {
        Log.d(TAG + "startMethod() ",
                "startMethod, editName: " + editName + ", editCurrent: " + editCurrent);

        boolean isValid = true;

        if (editName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Введите текущее показание", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        try {
            double currentValue = Double.parseDouble(editCurrent.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректное текущее показание", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }
    /** !Для передачи показаний */


}