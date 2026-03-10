package com.example.test3;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.expenseList.Expense;
import com.example.test3.payment.PaymentAdapter;
import com.example.test3.service.ExpenseService;

import java.util.ArrayList;

public class ExpenseDetailActivity extends AppCompatActivity {

    private static final String TAG = "ExpenseDetailActivity";

    private TextView textViewName, textViewDescription, textViewDate, textViewTotal;
    private ListView listViewPayments;
    private EditText editTextNewPayment;
    private Button buttonAdd, buttonBack;

    private ExpenseService expenseService;
    private Expense currentExpense;
    private PaymentAdapter paymentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        long expenseId = getIntent().getLongExtra("expense_id", -1);
        Log.d(TAG, "Получен ID: " + expenseId);

        expenseService = new ExpenseService(this);

        initViews();
        setupListeners();

        // Загружаем данные
        loadExpense(expenseId);
    }

    private void initViews() {
        textViewName = findViewById(R.id.textViewExpenseName);
        textViewDescription = findViewById(R.id.textViewExpenseDescription);
        textViewDate = findViewById(R.id.textViewExpenseDate);
        textViewTotal = findViewById(R.id.textViewTotalAmount);
        listViewPayments = findViewById(R.id.listViewPayments);
        editTextNewPayment = findViewById(R.id.editTextNewPayment);
        buttonAdd = findViewById(R.id.buttonAddPayment);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void setupListeners() {
        buttonAdd.setOnClickListener(v -> addNewPayment());
        buttonBack.setOnClickListener(v -> finish());
    }

    private void loadExpense(long expenseId) {
        // Получаем свежие данные из БД
        ArrayList<Expense> expenses = expenseService.getExpenseList();

        for (Expense expense : expenses) {
            if (expense.getId() == expenseId) {
                currentExpense = expense;
                break;
            }
        }

        if (currentExpense == null) {
            Toast.makeText(this, "Расход не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        updateDisplay();
    }

    private void updateDisplay() {
        textViewName.setText(currentExpense.getName());

        if (currentExpense.getDescription() != null && !currentExpense.getDescription().isEmpty()) {
            textViewDescription.setText("Описание: " + currentExpense.getDescription());
            textViewDescription.setVisibility(View.VISIBLE);
        } else {
            textViewDescription.setVisibility(View.GONE);
        }

        textViewDate.setText("Дата: " + currentExpense.getDateTimeString());
        textViewTotal.setText(String.format("Общая сумма: %.2f руб.",
                currentExpense.getExpenseListTotalAmount()));

        ArrayList<Double> payments = currentExpense.getExpenseList();
        if (payments == null) {
            payments = new ArrayList<>();
            currentExpense.setExpenseList(payments);
        }

        paymentAdapter = new PaymentAdapter(this, payments, currentExpense, expenseService,
                this::refreshData); // Перезагружаем данные при любом изменении

        listViewPayments.setAdapter(paymentAdapter);
    }

    private void refreshData() {
        Log.d(TAG, "Обновление данных...");
        // Просто перезагружаем расход заново***
        loadExpense(currentExpense.getId());
    }

    private void addNewPayment() {
        String paymentStr = editTextNewPayment.getText().toString().trim();

        if (paymentStr.isEmpty()) {
            Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double payment = Double.parseDouble(paymentStr);

            if (expenseService.addPaymentToExpense(currentExpense, payment)) {
                editTextNewPayment.setText("");
                Toast.makeText(this, "Платёж добавлен", Toast.LENGTH_SHORT).show();
                // Перезагружаем данные
                refreshData();
            } else {
                Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
        }
    }
}

//package com.example.test3;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.test3.expenseList.Expense;
//import com.example.test3.payment.PaymentAdapter;
//import com.example.test3.service.ExpenseService;
//
//import java.util.ArrayList;
//
//public class ExpenseDetailActivity extends AppCompatActivity {
//
//    private TextView textViewName, textViewDescription, textViewDate, textViewTotal;
//    private ListView listViewPayments;
//    private EditText editTextNewPayment;
//    private Button buttonAdd, buttonBack;
//
//    private ExpenseService expenseService;
//    private Expense currentExpense;
//    private PaymentAdapter paymentAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_expense_detail);
//
//        // Получаем переданный расход
//        long expenseId = getIntent().getLongExtra("expense_id", -1);
//
//        expenseService = new ExpenseService(this);
//
//        // Загружаем расход из БД
//        loadExpense(expenseId);
//
//        initViews();
//        setupListeners();
//        updateDisplay();
//    }
//
//    private void initViews() {
//        textViewName = findViewById(R.id.textViewExpenseName);
//        textViewDescription = findViewById(R.id.textViewExpenseDescription);
//        textViewDate = findViewById(R.id.textViewExpenseDate);
//        textViewTotal = findViewById(R.id.textViewTotalAmount);
//        listViewPayments = findViewById(R.id.listViewPayments);
//        editTextNewPayment = findViewById(R.id.editTextNewPayment);
//        buttonAdd = findViewById(R.id.buttonAddPayment);
//        buttonBack = findViewById(R.id.buttonBack);
//    }
//
//    private void setupListeners() {
//        buttonAdd.setOnClickListener(v -> addNewPayment());
//        buttonBack.setOnClickListener(v -> finish());
//    }
//
//    private void loadExpense(long expenseId) {
//        // Получаем все расходы и ищем нужный
//        ArrayList<Expense> expenses = expenseService.getExpenseList();
//        for (Expense expense : expenses) {
//            if (expense.getId() == expenseId) {
//                currentExpense = expense;
//                break;
//            }
//        }
//
//        if (currentExpense == null) {
//            Toast.makeText(this, "Расход не найден", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//    }
//
//    private void updateDisplay() {
//        if (currentExpense == null) return;
//
//        // Обновляем информацию о расходе
//        textViewName.setText(currentExpense.getName());
//
//        if (currentExpense.getDescription() != null && !currentExpense.getDescription().isEmpty()) {
//            textViewDescription.setText("Описание: " + currentExpense.getDescription());
//            textViewDescription.setVisibility(View.VISIBLE);
//        } else {
//            textViewDescription.setVisibility(View.GONE);
//        }
//
//        textViewDate.setText("Дата: " + currentExpense.getDateTimeString());
//        textViewTotal.setText(String.format("Общая сумма: %.2f руб.",
//                currentExpense.getExpenseListTotalAmount()));
//
//        // Настраиваем адаптер для платежей
//        ArrayList<Double> payments = currentExpense.getExpenseList();
//        if (payments == null) {
//            payments = new ArrayList<>();
//            currentExpense.setExpenseList(payments);
//        }
//
//        paymentAdapter = new PaymentAdapter(this, payments, currentExpense, expenseService,
//                () -> {
//                    // Обновляем общую сумму при изменении платежей
//                    textViewTotal.setText(String.format("Общая сумма: %.2f руб.",
//                            currentExpense.getExpenseListTotalAmount()));
//                });
//
//        listViewPayments.setAdapter(paymentAdapter);
//    }
//
//    private void addNewPayment() {
//        String paymentStr = editTextNewPayment.getText().toString().trim();
//
//        if (paymentStr.isEmpty()) {
//            Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        try {
//            double payment = Double.parseDouble(paymentStr);
//
//            // Добавляем платёж
//            if (expenseService.addPaymentToExpense(currentExpense, payment)) {
//                editTextNewPayment.setText("");
//                paymentAdapter.notifyDataSetChanged();
//                textViewTotal.setText(String.format("Общая сумма: %.2f руб.",
//                        currentExpense.getExpenseListTotalAmount()));
//                Toast.makeText(this, "Платёж добавлен", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
//            }
//        } catch (NumberFormatException e) {
//            Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
//        }
//    }
//}