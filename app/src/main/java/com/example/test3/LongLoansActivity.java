package com.example.test3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.expenseList.Expense;
import com.example.test3.expenseList.ExpenseAdapter;
import com.example.test3.service.ExpenseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;

public class LongLoansActivity extends AppCompatActivity {

    private static final String TAG = "LongLoansActivity";

    /** Тип "Длинные займы с кредитных средств" - ID из таблицы type_expense */
    public static final Long TYPE_CREDIT_LOANS = 3L;

    private ListView listViewLoans;
    private TextView textViewTotalAmount;
    private Button buttonUpdate, buttonDelete, buttonSave, buttonBack;
    private EditText editTextDate, editTextName, editTextAmount;

    private ExpenseService expenseService;
    private ExpenseAdapter expenseAdapter;
    private ArrayList<Expense> loansList;
    private Expense selectedLoan = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_loans);

        expenseService = new ExpenseService(this);

        initViews();
        setupListeners();
        loadLoans();
    }

    private void initViews() {
        listViewLoans = findViewById(R.id.listViewLongLoans);
        textViewTotalAmount = findViewById(R.id.textViewLongLoansTotal);

        buttonUpdate = findViewById(R.id.buttonUpdateLoan);
        buttonDelete = findViewById(R.id.buttonDeleteLoan);
        buttonSave = findViewById(R.id.buttonSaveLoan);
        buttonBack = findViewById(R.id.buttonBackLongLoans);

        editTextDate = findViewById(R.id.editTextLoanDate);
        editTextName = findViewById(R.id.editTextLoanName);
        editTextAmount = findViewById(R.id.editTextLoanAmount);
    }

    private void setupListeners() {
        buttonBack.setOnClickListener(v -> finish());

        buttonSave.setOnClickListener(v -> addNewLoan());

        buttonUpdate.setOnClickListener(v -> {
            if (selectedLoan == null) {
                Toast.makeText(this, "Выберите заём для изменения", Toast.LENGTH_SHORT).show();
                return;
            }
            openLoanDetail(selectedLoan);
        });

        buttonDelete.setOnClickListener(v -> {
            if (selectedLoan == null) {
                Toast.makeText(this, "Выберите заём для удаления", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmDeleteLoan(selectedLoan);
        });
    }

    private void loadLoans() {
        // Получаем расходы только с типом "Длинные займы с кредитных средств"
        loansList = expenseService.getExpenseList(TYPE_CREDIT_LOANS);

        if (loansList.isEmpty()) {
            textViewTotalAmount.setText("Общая сумма: 0.00 руб.");
        } else {
            // Рассчитываем общую сумму
            double total = 0.0;
            for (Expense loan : loansList) {
                total += loan.getExpenseListTotalAmount();
            }
            textViewTotalAmount.setText(String.format("Общая сумма: %.2f руб.", total));
        }

        // Создаем адаптер
        expenseAdapter = new ExpenseAdapter(this, loansList, expenseService);
        expenseAdapter.setOnItemClickListener(new ExpenseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Expense expense, int position) {
                selectedLoan = expense;
                expenseAdapter.setSelectedPosition(position);

                Toast.makeText(LongLoansActivity.this,
                        "Выбран: " + expense.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        listViewLoans.setAdapter(expenseAdapter);
    }

    private void addNewLoan() {
        String name = editTextName.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();
        String dateStr = editTextDate.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название займа", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            // Создаём новый расход с типом "Длинные займы с кредитных средств"
            Expense newLoan = new Expense(name, TYPE_CREDIT_LOANS);
            newLoan.addPayment(amount);

            // Устанавливаем дату, если указана
            if (!dateStr.isEmpty()) {
                ZonedDateTime dateTime = parseDate(dateStr);
                if (dateTime != null) {
                    newLoan.setDateTime(dateTime);
                }
            }

            // Сохраняем в БД
            if (expenseService.insertExpense(newLoan)) {
                Toast.makeText(this, "Заём добавлен", Toast.LENGTH_SHORT).show();
                clearInputs();
                loadLoans();
            } else {
                Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show();
        }
    }

    private ZonedDateTime parseDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
        try {
            Date date = formatter.parse(dateString);
            return date.toInstant().atZone(ZonedDateTime.now().getZone());
        } catch (ParseException e) {
            Toast.makeText(this, "Некорректный формат даты (используйте дд.ММ.гг)", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void openLoanDetail(Expense loan) {
        Intent intent = new Intent(this, ExpenseDetailActivity.class);
        intent.putExtra("expense_id", loan.getId());
        startActivity(intent);
    }

    private void confirmDeleteLoan(Expense loan) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление займа")
                .setMessage("Удалить заём \"" + loan.getName() + "\" вместе со всеми платежами?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    if (expenseService.removeExpense(loan)) {
                        Toast.makeText(this, "Заём удалён", Toast.LENGTH_SHORT).show();
                        selectedLoan = null;
                        loadLoans();
                    } else {
                        Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void clearInputs() {
        editTextName.setText("");
        editTextAmount.setText("");
        editTextDate.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLoans();
    }
}