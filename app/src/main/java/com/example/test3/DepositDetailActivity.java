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

import com.example.test3.deposit.Deposit;
import com.example.test3.month.Month;
import com.example.test3.payment.DepositPaymentAdapter;
import com.example.test3.service.DepositService;
import com.example.test3.service.MonthService;

import java.util.List;

public class DepositDetailActivity extends AppCompatActivity {

    private static final String TAG = "DepositDetailActivity";

    private TextView textViewName, textViewDescription, textViewDate, textViewMonth, textViewTotal;
    private ListView listViewPayments;
    private EditText editTextNewPayment;
    private Button buttonAdd, buttonBack;

    private DepositService depositService;
    private MonthService monthService;
    private Deposit currentDeposit;
    private DepositPaymentAdapter paymentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_detail);

        long depositId = getIntent().getLongExtra("deposit_id", -1);
        Log.d(TAG, "Получен ID взноса: " + depositId);

        depositService = new DepositService(this);
        monthService = new MonthService(this);

        initViews();
        setupListeners();
        loadDeposit(depositId);
    }


    private void initViews() {
        textViewName = findViewById(R.id.textViewDepositName);
        textViewDescription = findViewById(R.id.textViewDepositDescription);
        textViewDate = findViewById(R.id.textViewDepositDate);
        textViewMonth = findViewById(R.id.textViewDepositMonth);
        textViewTotal = findViewById(R.id.textViewDepositTotal);
        listViewPayments = findViewById(R.id.listViewDepositPayments);
        editTextNewPayment = findViewById(R.id.editTextNewDepositPayment);
        buttonAdd = findViewById(R.id.buttonAddDepositPayment);
        buttonBack = findViewById(R.id.buttonBack);
    }


    private void setupListeners() {
        buttonAdd.setOnClickListener(v -> addNewPayment());
        buttonBack.setOnClickListener(v -> finish());
    }


    private void loadDeposit(long depositId) {

        currentDeposit = depositService.getDepositById(depositId);

        if (currentDeposit == null) {
            Toast.makeText(this, "Взнос не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        updateDisplay();
    }


    private void updateDisplay() {

        textViewName.setText(currentDeposit.getName());

        if (currentDeposit.getDescription() != null && !currentDeposit.getDescription().isEmpty()) {
            textViewDescription.setText("Описание: " + currentDeposit.getDescription());
            textViewDescription.setVisibility(View.VISIBLE);
        } else {
            textViewDescription.setVisibility(View.GONE);
        }

        textViewDate.setText("Дата: " + currentDeposit.getDateTimeString());


        /** Информация о месяце */
        if (currentDeposit.getExpenseId() != null) {

            /** Ищет месяц по ID */
            Month month = monthService.getMonthById(currentDeposit.getExpenseId());
            if (month != null) {
                textViewMonth.setText("Месяц: " + month.getMonthYear());
                textViewMonth.setVisibility(View.VISIBLE);
            } else {
                textViewMonth.setText("Месяц: ID=" + currentDeposit.getExpenseId() + " (не найден в БД)");
                textViewMonth.setVisibility(View.VISIBLE);
            }

        } else {
            textViewMonth.setVisibility(View.GONE);
        }


        /** Общая сумма (автоматически высчитывается из всех платежей) */
        double total = currentDeposit.getTotalAmount();
        textViewTotal.setText(String.format("Общая сумма: %.2f руб.", total));


        /** Настраивает адаптер для списка платежей */
        List<Double> payments = currentDeposit.getPayments();
        if (payments == null || payments.isEmpty()) {
            listViewPayments.setVisibility(View.GONE);
        } else {
            listViewPayments.setVisibility(View.VISIBLE);
            paymentAdapter = new DepositPaymentAdapter(this, payments, currentDeposit, depositService,
                    this::refreshData);
            listViewPayments.setAdapter(paymentAdapter);
        }

    }


    private void refreshData() {
        Log.d(TAG, "Обновление данных...");
        loadDeposit(currentDeposit.getId());                                                        /** Перезагружает взнос заново */
    }


    private void addNewPayment() {

        String paymentStr = editTextNewPayment.getText().toString().trim();

        if (paymentStr.isEmpty()) {
            Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            double payment = Double.parseDouble(paymentStr);

            if (depositService.addPaymentToDeposit(currentDeposit, payment)) {
                editTextNewPayment.setText("");
                Toast.makeText(this, "Платёж добавлен", Toast.LENGTH_SHORT).show();
                refreshData();                                                                      /** Перезагружает данные */
            } else {
                Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
        }

    }


}