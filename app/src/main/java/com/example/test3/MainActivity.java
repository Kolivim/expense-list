package com.example.test3;

import static com.example.test3.util.Util.EXTRA_EXPENSE_TYPE;
import static com.example.test3.util.Util.TYPE_EXPENSE_UTILITY_BILLS;
import static com.example.test3.util.Util.TYPE_MONTHLY_CONTRIBUTIONS;
import static com.example.test3.util.Util.TYPE_MONTHLY_EXPENSE_PLANNING;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.expenseList.Expense;
import com.example.test3.expenseList.ExpenseAdapter;
import com.example.test3.monthly.expense.planning.MonthExpensePlanningActivity;
import com.example.test3.monthly.expense.refund.planning.MonthRefundPlannedServiceActivity;
import com.example.test3.monthly.expense.utility.service.MonthUtilityServiceActivity;
import com.example.test3.service.DepositService;
import com.example.test3.service.ExpenseService;
import com.example.test3.util.FileExportUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

//    ArrayAdapter<Expense> arrayAdapter;

    private ExpenseAdapter expenseAdapter;

    private ListView listView;

    private Expense selectedExpense = null;

//    ArrayList<Expense> selectedUsers = new ArrayList<Expense>();

    private ExpenseService expenseService;

    public static Long TYPE_MONTHLY_EXPENSES = 1L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* getScreenSize(); */


        /** Настройка Toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Мои финансы");
        }


        expenseService = new ExpenseService(getBaseContext());
        listView = findViewById(R.id.expenseList);

        loadExpenses();

    }

    private void loadExpenses() {
        ArrayList<Expense> allExpenseListDb = expenseService.getExpenseList();
        expenseAdapter = new ExpenseAdapter(this, allExpenseListDb, expenseService);

        /** Устанавливаем слушатель */
        expenseAdapter.setOnItemClickListener(new ExpenseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Expense expense, int position) {

                selectedExpense = expense;
                expenseAdapter.setSelectedPosition(position);

//                listView.setItemChecked(position, true);

                Toast.makeText(MainActivity.this, "Выбран: " + expense.getName(),
                        Toast.LENGTH_SHORT).show();

            }
        });


        listView.setAdapter(expenseAdapter);

        clearSelection();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        View rootView = getWindow().getDecorView().getRootView();

        if (id == R.id.action_month_admin) {
            openMonthAdmin(null);
            return true;
        } else if (id == R.id.action_long_loan) {
            openLongLoans(null);
            return true;
        } else if (id == R.id.action_long_loan_own_funds) {
            openLongLoansOwnFunds(null);
            return true;
        } else if (id == R.id.action_export_txt) {
            exportToTxt(null);
            return true;
        } else if (id == R.id.action_export_json) {
            exportToJson(null);
            return true;
        } else if (id == R.id.action_month_expense_plannyng) {
            openMonthExpensePlanning(null);
            return true;
        } else if (id == R.id.action_month_utility_service) {
            openMonthUtilityService(null);
            return true;
        } else if (id == R.id.action_month_refund_planning) {
            openMonthRefundPlanning(null);
            return true;
        } else if (id == R.id.action_month_contributions) {
            openMonthContributions(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void clearSelection() {

        selectedExpense = null;

        if (listView != null) {
            listView.clearChoices();
            expenseAdapter.notifyDataSetChanged();
        }

    }


    public void add(View view){

        /** Вычитываем введённые пользователем данные: */
        EditText expenseNameEditText = findViewById(R.id.editTextNameExpense);
        String expenseName = expenseNameEditText.getText().toString();

        EditText expenseEditText = findViewById(R.id.editTextNumberDecimal);

        Double expense = null;
        if(expenseEditText.getText() != null && !expenseEditText.getText().toString().isEmpty())
            expense = Double.parseDouble(expenseEditText.getText().toString());

        EditText expenseDateEditText = findViewById(R.id.editTextDate);
        String expenseDateTimeString = expenseDateEditText.getText().toString();

        String expenseDescription = null;


        /** Создаём новую запись: */
        if(!expenseName.isEmpty()){

            Expense newExpense = getNewExpense(expenseName, expense, expenseDateTimeString, expenseDescription);

            expenseService.insertExpense(newExpense);

            updateAdapter();

            cleanUserInput(expenseNameEditText, expenseEditText, expenseDateEditText);
            loadExpenses();

//            expenseAdapter.notifyDataSetChanged();

        }

    }


    public void remove(View view){

        if (selectedExpense == null) {
            Toast.makeText(this, "Выберите расход для удаления", Toast.LENGTH_SHORT).show();
            return;
        }


        boolean success = expenseService.removeExpense(selectedExpense);


        if (success) {
            Toast.makeText(this, "Расход удалён", Toast.LENGTH_SHORT).show();
            loadExpenses();
        } else {
            Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
        }


        /*
        updateAdapter();
        expenseAdapter.notifyDataSetChanged();
        */

    }


    public void updateAdapter() {

        ArrayList<Expense> allExpenseList = expenseService.getExpenseList();
//        arrayAdapter = new ArrayAdapter<Expense>(this, android.R.layout.simple_list_item_1, allExpenseList);
//        listView.setAdapter(arrayAdapter);

        expenseAdapter = new ExpenseAdapter(this, allExpenseList, expenseService);
        listView.setAdapter(expenseAdapter);

    }


    public Expense getNewExpense(String expenseName, Double expense, String expenseDateTimeString, String expenseDescription) {

        if(expenseName != null && !expenseName.isEmpty()) {

            Expense newExpense = new Expense(expenseName, TYPE_MONTHLY_EXPENSES);

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
        loadExpenses();
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


    public void exportToTxt(View view) {

        Toast.makeText(this, "Экспорт в TXT запущен", Toast.LENGTH_SHORT).show();

        /** Получаем актуальный список расходов */
        ArrayList<Expense> expenseList = expenseService.getExpenseList();

        if (expenseList.isEmpty()) {
            Toast.makeText(this, "Нет данных для экспорта", Toast.LENGTH_SHORT).show();
            return;
        }

        /** Используем наш утилитный класс для экспорта */
        /** Вариант 1: Сохранить в общедоступную папку Downloads
         * Требует разрешения в манифесте */
        FileExportUtil.exportExpensesToTxt(this, expenseList);


        // Вариант 2: Сохранить в приватную директорию приложения
        // Не требует разрешений
        // FileExportUtil.exportToPrivateStorage(this, expenseList);

    }


    public void exportToJson(View view) {

        Toast.makeText(this, "Экспорт в JSON запущен", Toast.LENGTH_SHORT).show();

        /** Получаем актуальный список расходов */
        ArrayList<Expense> expenseList = expenseService.getExpenseList();

        if (expenseList.isEmpty()) {
            Toast.makeText(this, "Нет данных для экспорта", Toast.LENGTH_SHORT).show();
            return;
        }

        /** Используем наш утилитный класс для экспорта в JSON */
        /** Вариант 1: Сохранить в общедоступную папку Downloads */
        FileExportUtil.exportExpensesToJson(this, expenseList);

        // Вариант 2: Сохранить в приватную директорию приложения (не требует разрешений)
        // FileExportUtil.exportJsonToPrivateStorage(this, expenseList);

    }


    /** Метод для обработки результата: */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение получено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Нет разрешения на запись файлов", Toast.LENGTH_LONG).show();
            }

        }

    }


    public void updateExpense(View view) {

        if (selectedExpense == null) {
            Toast.makeText(this, "Выберите расход для изменения", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ExpenseDetailActivity.class);
        intent.putExtra("expense_id", selectedExpense.getId());
        startActivity(intent);

        /** Обновляет список, после изменения одног из элементов списка */
        loadExpenses();
    }


    public void openMonthAdmin(View view) {
        Intent intent = new Intent(this, MonthAdminActivity.class);
        startActivity(intent);
    }


    /** Длинные займы с кредитных средств */
    public void openLongLoans(View view) {
        Intent intent = new Intent(this, LongLoansActivity.class);
        startActivity(intent);
    }


    /** Длинные займы с собственнных средств */
    public void openLongLoansOwnFunds(View view) {
        LongLoansUniversalActivity.start(this, 4L, DepositService.TYPE_MYSELF_LOAN_REPAYMENT, "Длинные займы (собственные средства)");
//        Intent intent = new Intent(this, LongLoansActivity.class);
//        startActivity(intent);
    }


    /** Ежемесячное планирование расходов */
    public void openMonthExpensePlanning(View view) {
        Log.d("openMonthExpensePlannyng", "startMethod");

//        MonthExpensePlannyngActivity.start(this, 4L, DepositService.TYPE_MYSELF_LOAN_REPAYMENT, "Длинные займы (собственные средства)");

//        /*
        Intent intent = new Intent(this, MonthExpensePlanningActivity.class);          /** Универсальная Активити */
        intent.putExtra(EXTRA_EXPENSE_TYPE, TYPE_MONTHLY_EXPENSE_PLANNING);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
//        */

    }


    /** Ежемесячное планирование расходов */
    public void openMonthUtilityService(View view) {
        Log.d(TAG, "openMonthUtilityService startMethod");

        Intent intent = new Intent(this, MonthUtilityServiceActivity.class);
//        intent.putExtra(EXTRA_EXPENSE_TYPE, TYPE_EXPENSE_UTILITY_BILLS);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


    /** Ежемесячное планирование возвратов длинных займов */
    public void openMonthRefundPlanning(View view) {
        Log.d(TAG, "openMonthRefundPlanning startMethod");

        Intent intent = new Intent(this, MonthRefundPlannedServiceActivity.class);
        startActivity(intent);

        Log.d(TAG, "openMonthRefundPlanning endMethod");
    }


    /** Ежемесячное планирование возвратов длинных займов */
    public void openMonthContributions(View view) {
        Log.d(TAG, "openMonthContributions startMethod");

        Intent intent = new Intent(this, MonthExpensePlanningActivity.class);
        intent.putExtra(EXTRA_EXPENSE_TYPE, TYPE_MONTHLY_CONTRIBUTIONS);
        startActivity(intent);

        Log.d(TAG, "openMonthContributions endMethod");
    }


    private void getScreenSize() {

        android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;


        float density = displayMetrics.density;


        int widthDp = (int) (widthPixels / density);
        int heightDp = (int) (heightPixels / density);


        android.util.Log.d("SCREEN_SIZE", "Ширина в пикселях: " + widthPixels);
        android.util.Log.d("SCREEN_SIZE", "Высота в пикселях: " + heightPixels);
        android.util.Log.d("SCREEN_SIZE", "Плотность экрана: " + density);
        android.util.Log.d("SCREEN_SIZE", "Ширина в dp: " + widthDp);
        android.util.Log.d("SCREEN_SIZE", "Высота в dp: " + heightDp);


        Toast.makeText(this,
                "Экран: " + widthDp + "dp x " + heightDp + "dp",
                Toast.LENGTH_LONG).show();
    }


}