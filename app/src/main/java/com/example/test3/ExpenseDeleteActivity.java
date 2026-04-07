package com.example.test3;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.test3.expenseList.Expense;
import com.example.test3.service.ExpenseService;

public class ExpenseDeleteActivity extends AppCompatActivity {

    private TextView textViewName, textViewDescription, textViewDate, textViewTotal;
    private Button buttonDelete, buttonCancel;
    private ExpenseService expenseService;
    private Expense currentExpense;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_delete);

        long expenseId = getIntent().getLongExtra("expense_id", -1);
        expenseService = new ExpenseService(this);
        currentExpense = expenseService.getExpenseById(expenseId);

        if (currentExpense == null) {
            Toast.makeText(this, "Расход не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayExpenseInfo();
        setupListeners();
    }


    private void initViews() {
        textViewName = findViewById(R.id.textViewExpenseName);
        textViewDescription = findViewById(R.id.textViewExpenseDescription);
        textViewDate = findViewById(R.id.textViewExpenseDate);
        textViewTotal = findViewById(R.id.textViewTotalAmount);
        buttonDelete = findViewById(R.id.buttonDeleteExpense);
        buttonCancel = findViewById(R.id.buttonCancelDelete);
    }


    private void displayExpenseInfo() {
        textViewName.setText(currentExpense.getName());
        if (currentExpense.getDescription() != null && !currentExpense.getDescription().isEmpty()) {
            textViewDescription.setText("Описание: " + currentExpense.getDescription());
            textViewDescription.setVisibility(android.view.View.VISIBLE);
        } else {
            textViewDescription.setVisibility(android.view.View.GONE);
        }
        textViewDate.setText("Дата: " + currentExpense.getDateTimeString());
        textViewTotal.setText(String.format("Общая сумма: %.2f руб.",
                currentExpense.getExpenseListTotalAmount()));
    }


    private void setupListeners() {
        buttonDelete.setOnClickListener(v -> confirmDelete());
        buttonCancel.setOnClickListener(v -> finish());
    }


    private void confirmDelete() {

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