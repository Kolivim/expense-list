package com.example.test3.expenseList;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.test3.ExpenseDetailActivity;
import com.example.test3.R;
import com.example.test3.monthly.expense.refund.planning.ExpenseRefund;
import com.example.test3.payment.PaymentAdapter;
import com.example.test3.service.ExpenseService;
import com.example.test3.util.Util;

import java.util.ArrayList;

public class ExpenseRefundDetailActivity extends AppCompatActivity {

    private static final String TAG = "ExpenseRefundDetailActivity";

    protected TextView textViewName, textViewDescription, textViewDate, textViewTotal;
    protected ListView listViewPayments;
    protected EditText editTextNewPayment;
    protected Button buttonAdd, buttonBack, buttonChooseTextColor, buttonEditDescription;

    protected ExpenseService expenseService;
    protected ExpenseRefund currentExpense;
    protected PaymentAdapter paymentAdapter;

    private TextView textViewStartDateRefund, textViewMonthCountRefund;
    private Button buttonDeleteExpense;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expense_refund_detail);

        currentExpense = (ExpenseRefund) getIntent().getSerializableExtra("expense_object");
        Log.d(TAG, "Получена ExpenseRefund: " + currentExpense);

        expenseService = new ExpenseService(this);

        initViews();
        setupListeners();

        loadExpense();
    }


    protected void initViews() {
        textViewName = findViewById(R.id.textViewExpenseName);
        textViewDescription = findViewById(R.id.textViewExpenseDescription);
        textViewDate = findViewById(R.id.textViewExpenseDate);
        textViewTotal = findViewById(R.id.textViewTotalAmount);
        listViewPayments = findViewById(R.id.listViewPayments);
        editTextNewPayment = findViewById(R.id.editTextNewPayment);
        buttonAdd = findViewById(R.id.buttonAddPayment);
        buttonBack = findViewById(R.id.buttonBack);
        buttonChooseTextColor = findViewById(R.id.buttonChooseTextColor);
        buttonEditDescription = findViewById(R.id.buttonEditDescription);

        textViewStartDateRefund = findViewById(R.id.textViewStartDateRefund);
        textViewMonthCountRefund = findViewById(R.id.textViewMonthCountRefund);
        buttonDeleteExpense = findViewById(R.id.buttonDeleteExpense);
    }


    protected void setupListeners() {
        buttonAdd.setOnClickListener(v -> addNewPayment());
        buttonBack.setOnClickListener(v -> finish());
        buttonChooseTextColor.setOnClickListener(v -> showTextColorPickerDialog());
        buttonEditDescription.setOnClickListener(v -> showEditDescriptionDialog());

        if (buttonDeleteExpense != null) {
            buttonDeleteExpense.setOnClickListener(v -> showDeleteConfirmationDialog());
        }
    }


    protected void showTextColorPickerDialog() {

        /** Массив доступных цветов */
        final Integer[] textColors = {
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.holo_red_dark),
                ContextCompat.getColor(this, android.R.color.holo_blue_dark),
                ContextCompat.getColor(this, android.R.color.holo_green_dark),
                ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                ContextCompat.getColor(this, android.R.color.holo_purple),
                ContextCompat.getColor(this, android.R.color.darker_gray)
        };

        final String[] colorNames = {
                "Чёрный", "Белый", "Красный", "Синий", "Зелёный", "Оранжевый", "Фиолетовый", "Серый"
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle("Выберите цвет текста")
                .setItems(colorNames, (dialog, which) -> {

                    currentExpense.setRowColor(textColors[which]);

                    if (expenseService.updateExpenseRowColor(currentExpense)) {
                        Toast.makeText(this, "Цвет текста обновлён", Toast.LENGTH_SHORT).show();
                        refreshData(); /** перезагружает данные для отображения нового цвета */
                    } else {
                        Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();

    }


    protected void loadExpense() {

//        currentExpense = expenseService.getExpenseById(expenseId);
//
//        if (currentExpense == null) {
//            Toast.makeText(this, "Расход не найден", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

        updateDisplay();
    }


    protected void updateDisplay() {

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

        String startDate = this.getString(R.string.month_expense_refund_start_date,
                currentExpense.getStartDate().format(Util.dateFormatterMonthYearSee));
        textViewStartDateRefund.setText(startDate);

        String monthCount = this.getString(R.string.month_expense_refund_month_count, currentExpense.getMonthCount());
        textViewMonthCountRefund.setText(monthCount);


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
//        ArrayList<Double> payments = currentExpense.getExpenseList();
//        if (payments == null) {
//            payments = new ArrayList<>();
//            currentExpense.setExpenseList(payments);
//        }
//
//        paymentAdapter = new PaymentAdapter(this, payments, currentExpense, expenseService,
//                this::refreshData); // Перезагружаем данные при любом изменении
//
//        listViewPayments.setAdapter(paymentAdapter)
    }


    protected void refreshData() {
        Log.d(TAG, "Обновление данных для currentExpense с id = ".concat(currentExpense.getId().toString()));
        /** Просто закрываем */
        finish();
//        loadExpense(currentExpense.getId());
    }


    protected void addNewPayment() {
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

                refreshData();
            } else {
                Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
        }
    }


    /** Диалог редактирования описания */
    protected void showEditDescriptionDialog() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Редактировать описание");

        /** Создаёт поле ввода */
        final EditText input = new EditText(this);
        input.setHint("Введите описание");


        /** Заполняет текущим описанием, если оно есть */
        if (currentExpense.getDescription() != null && !currentExpense.getDescription().isEmpty()) {
            input.setText(currentExpense.getDescription());
        }

        input.setSelection(input.getText().length()); /** Перемещает курсор в конец текста */
        builder.setView(input);


        /** Кнопки */
        builder.setPositiveButton("Сохранить", (dialog, which) -> {

            String newDescription = input.getText().toString().trim();

            currentExpense.setDescription(newDescription);

            /** Сохраняет в БД */
            if (expenseService.updateExpenseDescription(currentExpense)) {
                Toast.makeText(this, "Описание обновлено", Toast.LENGTH_SHORT).show();
                updateDisplay();                                                                    /** Обновляет отображение */
            } else {
                Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Отмена", null);

        /** Кнопка Очистить */
        builder.setNeutralButton("Очистить", (dialog, which) -> {

            currentExpense.setDescription("");

            if (expenseService.updateExpenseDescription(currentExpense)) {
                Toast.makeText(this, "Описание очищено", Toast.LENGTH_SHORT).show();
                updateDisplay();
            }

        });


        builder.show();
    }


    private void showDeleteConfirmationDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Удаление расхода")
                .setMessage("Вы уверены, что хотите удалить расход \"" + currentExpense.getName() + "\" и все его платежи?")
                .setPositiveButton("Удалить", (dialog, which) -> {

                    boolean deleted = expenseService.removeExpense(currentExpense);
                    if (deleted) {
                        Toast.makeText(this, "Расход удалён", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Отмена", null)
                .show();

    }


}