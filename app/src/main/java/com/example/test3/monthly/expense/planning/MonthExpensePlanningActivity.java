package com.example.test3.monthly.expense.planning;

import static com.example.test3.util.Util.EXTRA_EXPENSE_TYPE;
import static com.example.test3.util.Util.TYPE_EXPENSE_MONTH_PLANNING;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.ExpenseDetailActivity;
import com.example.test3.LongLoansActivity;
import com.example.test3.LongLoansUniversalActivity;
import com.example.test3.MonthAdminActivity;
import com.example.test3.R;
import com.example.test3.expenseList.Expense;
import com.example.test3.service.DepositService;
import com.example.test3.service.ExpenseService;
import com.example.test3.service.MonthService;
import com.example.test3.util.FileExportUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MonthExpensePlanningActivity extends AppCompatActivity {

    private static final String TAG = "MonthExpensePlanningActivity";

    private MonthService monthService;

    private ExpenseService expenseService;

    private DepositService depositService;

    private Long monthType;                                                                         /** == TYPE_MONTHLY_EXPENSE_PLANNYNG = 3L */


    private List<MonthlyExpensePlanningDto> monthDtoList;
    private ExpandableListView expandableListView;
    /* private ListView listView; */
    private /* MonthExpensePlanningAdapter */ MonthExpenseExpandableAdapter adapter;


    private MonthlyExpensePlanningDto selectedMonthDto = null;
    private int selectedMonthPosition = -1;

//    private Button buttonBack /* , buttonSave, buttonUpdate, buttonDelete */ ;


//    public static void start(Context context, long loanType, Long repaymentType, String title) {
//        Intent intent = new Intent(context, LongLoansUniversalActivity.class);
//        intent.putExtra(EXTRA_LOAN_TYPE, loanType);
//        intent.putExtra(EXTRA_TITLE, title);
//        intent.putExtra("repayment_type", repaymentType);
//        context.startActivity(intent);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("loadExpenses", "startMethod");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_expense_planning);

        monthType = getIntent().getLongExtra(EXTRA_EXPENSE_TYPE, 3L);

        monthService = new MonthService(this);
        expenseService = new ExpenseService(this);
        depositService = new DepositService(this);

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

        Log.d("loadExpenses", "endMethod");
    }


    private void loadData() {
        Log.d(TAG, "loadData startMethod");

        monthDtoList = monthService.getAllMonthlyExpensePlannyngDtos(monthType);

        if (monthDtoList.isEmpty()) {
            Toast.makeText(this, "Нет ни одного месяца с данными", Toast.LENGTH_LONG).show();
        }


        adapter = new MonthExpenseExpandableAdapter /* MonthExpensePlanningAdapter */ (this, monthDtoList);

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


        /** Раскрывает все группы (для наглядности) */
        int groupCount = adapter.getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            expandableListView.expandGroup(i);
        }


        adapter.setSelectedGroupPosition(-1);
        selectedMonthPosition = -1;                                                                 /** сбрасывает выбор месяца */


//        updateTotalStats();

        Log.d(TAG, "loadData endMethod");
    }


    private void updateTotalStats() {

        double totalExpenses = 0;
        /*
        double totalDeposits = 0;
        double totalBalance = 0;
        */


        for (MonthlyExpensePlanningDto dto : monthDtoList) {
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
        Log.d(TAG, "startMethod");

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

        if (newExpense != null) monthService.getOrCreatePlanningExpenseMonth(newExpense);

        loadData();
        cleanUserInput(expenseNameEditText, expenseEditText, expenseDateEditText);
        Log.d(TAG, "endMethod");
    }
//    public void add(View view){
//        Log.d(TAG, "startMethod");
//
//        /** Вычитываем введённые пользователем данные: */
//        EditText expenseNameEditText = findViewById(R.id.editTextNameExpense);
//        String expenseName = expenseNameEditText.getText().toString();
//
//        EditText expenseEditText = findViewById(R.id.editTextNumberDecimal);
//
//        Double expense = null;
//        if(expenseEditText.getText() != null && !expenseEditText.getText().toString().isEmpty())
//            expense = Double.parseDouble(expenseEditText.getText().toString());
//
//        EditText expenseDateEditText = findViewById(R.id.editTextDate);
//        String expenseDateTimeString = expenseDateEditText.getText().toString();
//
//        String expenseDescription = null;
//
//
//        /** Создаёт новую запись Expense : */
//        Expense newExpense = null;
//        if(!expenseName.isEmpty()){
//
//            newExpense = getNewExpense(expenseName, expense, expenseDateTimeString, expenseDescription);
//
//            expenseService.insertExpense(newExpense);
//
////            updateAdapter();
//        }
//        /** !Создаёт новую запись Expense */
//
//
//        /** Создаёт новую запись Month : */
//        if(newExpense != null) monthService.getOrCreatePlanningExpenseMonth(newExpense);
//        /** !Создаёт новую запись Month */
//
//
//        /** Перезагружает Activity : */
//        loadData();
//        cleanUserInput(expenseNameEditText, expenseEditText, expenseDateEditText);
//        /** !Перезагружает Activity */
//
//
//        Log.d(TAG, "endMethod");
//    }


    public void remove(View view){
        Log.d(TAG, "startMethod");

        /*
        if (selectedMonthDto == null) {
            Toast.makeText(this, "Выберите расход для удаления", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Получен к удалению selectedMonthDto: ".concat(selectedMonthDto.toString()));

        boolean success = monthService.removeMonth(selectedMonthDto);
        */

        if (selectedMonthPosition == -1) {
            Toast.makeText(this, "Выберите месяц для удаления", Toast.LENGTH_SHORT).show();
            return;
        }

        MonthlyExpensePlanningDto dto = monthDtoList.get(selectedMonthPosition);

        Log.d(TAG, "Получен к удалению MonthlyExpensePlanningDto: ".concat(dto.toString()));

        boolean success = monthService.removeMonth(dto);


        if (success) {
            Toast.makeText(this, "Расход удалён", Toast.LENGTH_SHORT).show();
            loadData();
        } else {
            Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
        }


        /*
        updateAdapter();
        expenseAdapter.notifyDataSetChanged();
        */

        Log.d(TAG, "endMethod");
    }


    /*
    public void updateAdapter() {

        List<MonthlyExpensePlanningDto> monthList = monthService.getAllMonthlyExpensePlannyngDtos(monthType);

        adapter = new MonthExpensePlanningAdapter(this, monthList);
        listView.setAdapter(adapter);

    }
    */


    public Expense getNewExpense(String expenseName, Double expense, String expenseDateTimeString, String expenseDescription) {

        if(expenseName != null && !expenseName.isEmpty()) {

            Expense newExpense = new Expense(expenseName, TYPE_EXPENSE_MONTH_PLANNING);

            if(expense != null && !expense.isNaN()) newExpense.addPayment(expense);

            if(expenseDateTimeString != null && !expenseDateTimeString.isEmpty()) {
                ZonedDateTime expenseZonedDateTime = getZoneDateTime(expenseDateTimeString);
                if(expenseZonedDateTime != null) newExpense.setDateTime(expenseZonedDateTime);
            }

            if(expenseDescription != null && !expenseDescription.isEmpty()) newExpense.setDescription(expenseDescription);

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

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");

        Date date = null;
        ZonedDateTime zonedDateTime = null;

        try {

            date = formatter.parse(dateString);

            zonedDateTime = date.toInstant().atZone(ZonedDateTime.now().getZone());

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return zonedDateTime;
    }


    public void cleanUserInput(EditText expenseNameEditText, EditText expenseEditText, EditText expenseDateEditText) {

        expenseNameEditText.setText("");
        expenseEditText.setText("");
        expenseDateEditText.setText("");

    }


    public void back(View view) {
        finish();
    }


    public void updateMonthExpensePlanning(View view) {
        Log.d(TAG, "startMethod");


        /*
        long selectedGroupId = expandableListView.getSelectedPosition();
        if (selectedGroupId < 0 || selectedGroupId >= adapter.getGroupCount()) {
            Toast.makeText(this, "Выберите месяц", Toast.LENGTH_SHORT).show();
            return;
        }

        MonthlyExpensePlanningDto selectedMonthDto = (MonthlyExpensePlanningDto) adapter.getGroup((int) selectedGroupId);
        Intent intent = new Intent(this, ExpenseDetailActivity.class);
        intent.putExtra("expense_id", selectedMonthDto.getMonth().getId());
        startActivity(intent);
        */


        if (selectedMonthPosition == -1) {
            Toast.makeText(this, "Выберите месяц для изменения", Toast.LENGTH_SHORT).show();
            return;
        }

        MonthlyExpensePlanningDto dto = monthDtoList.get(selectedMonthPosition);
        Toast.makeText(this, "Для изменения выбран месяц " + dto.getMonth().getMonthYear(), Toast.LENGTH_LONG).show();


        // todo: изменить активити на правильную новую, для изменения строки с месяцем :
        Intent intent = new Intent(this, ExpenseDetailActivity.class);
        intent.putExtra("expense_id", dto.getMonth().getId());
        startActivity(intent);
        // todo: !изменить активити на правильную новую, для изменения строки с месяцем


        Log.d(TAG, "endMethod");
    }
//    public void updateMonthExpensePlanning(View view) {
//
//        if (selectedMonthDto == null) {
//            Toast.makeText(this, "Выберите расход для изменения", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Intent intent = new Intent(this, ExpenseDetailActivity.class);
//        intent.putExtra("expense_id", selectedMonthDto.getMonth().getId());
//        startActivity(intent);
//
//        /** Обновляет список, после изменения одног из элементов списка */
//        loadData();
//    }


}