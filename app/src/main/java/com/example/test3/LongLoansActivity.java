package com.example.test3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.Expense;
import com.example.test3.service.DepositService;
import com.example.test3.service.ExpenseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LongLoansActivity extends AppCompatActivity {

    private static final String TAG = "LongLoansActivity";

    /** Тип "Длинные займы с кредитных средств" - ID из таблицы type_expense */
    public static final Long TYPE_CREDIT_LOANS = 3L;

    private ListView listViewLoans;
    private TextView textViewTotalAmount;
    private Button buttonSave, buttonBack, buttonUpdate, buttonDelete;
    private EditText editTextDate, editTextName, editTextAmount;

    private ExpenseService expenseService;
    private DepositService depositService;
    private LongLoanAdapter loanAdapter;
    private ArrayList<Expense> loansList;
    private Expense selectedLoan = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_loans);

        expenseService = new ExpenseService(this);
        depositService = new DepositService(this);

        initViews();
        setupListeners();
        loadLoans();
    }

    private void initViews() {
        listViewLoans = findViewById(R.id.listViewLongLoans);
        textViewTotalAmount = findViewById(R.id.textViewLongLoansTotal);

        buttonSave = findViewById(R.id.buttonSaveLoan);
        buttonBack = findViewById(R.id.buttonBackLongLoans);

        buttonUpdate = findViewById(R.id.buttonUpdateLoan);
        buttonDelete = findViewById(R.id.buttonDeleteLoan);

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
        loansList = expenseService.getExpenseList(TYPE_CREDIT_LOANS);

        if (loansList.isEmpty()) {
            textViewTotalAmount.setText("Общая сумма: 0.00 руб.");
        } else {
            double total = 0.0;
            for (Expense loan : loansList) {
                total += loan.getExpenseListTotalAmount();
            }
            textViewTotalAmount.setText(String.format("Общая сумма: %.2f руб.", total));
        }

        loanAdapter = new LongLoanAdapter(this, loansList, expenseService, depositService);
        loanAdapter.setOnItemClickListener((expense, position) -> {
            selectedLoan = expense;
            loanAdapter.setSelectedPosition(position);
            Toast.makeText(LongLoansActivity.this,
                    "Выбран: " + expense.getName(), Toast.LENGTH_SHORT).show();
        });

        loanAdapter.setOnRepayClickListener(loan -> showRepayDialog(loan));

        listViewLoans.setAdapter(loanAdapter);
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

            Expense newLoan = new Expense(name, TYPE_CREDIT_LOANS);
            newLoan.addPayment(amount);

            if (!dateStr.isEmpty()) {
                ZonedDateTime dateTime = parseDate(dateStr);
                if (dateTime != null) {
                    newLoan.setDateTime(dateTime);
                }
            }

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


    private void showRepayDialog(Expense loan) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Погашение займа");

        double totalLoan = loan.getExpenseListTotalAmount();
        double totalRepaid = getTotalRepaid(loan);
        double remainingDebt = totalLoan - totalRepaid;

        String message = String.format("Заём: %s\nИсходная сумма: %.2f руб.\nУже погашено: %.2f руб.\nОстаток долга: %.2f руб.\n\nВведите сумму для погашения:",
                loan.getName(), totalLoan, totalRepaid, remainingDebt);

        builder.setMessage(message);

        final EditText input = new EditText(this);
        input.setHint("Сумма погашения");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_SIGNED |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Погасить", (dialog, which) -> {

            String amountStr = input.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
                return;
            }


            try {

                double repaymentAmount = Double.parseDouble(amountStr);

                if (repaymentAmount <= 0) {
                    Toast.makeText(this, "Сумма должна быть больше 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (repaymentAmount > remainingDebt) {
                    Toast.makeText(this, "Сумма погашения не может превышать остаток долга", Toast.LENGTH_SHORT).show();
                    return;
                }


                Deposit repaymentDeposit = new Deposit(
                        "Погашение: " + loan.getName(),
                        DepositService.TYPE_CREDIT_LOAN_REPAYMENT
                );

                repaymentDeposit.setDescription("Погашение займа \"" + loan.getName() + "\" на сумму " + repaymentAmount + " руб.");
                repaymentDeposit.setDateTime(ZonedDateTime.now());
                repaymentDeposit.addPayment(repaymentAmount);
                repaymentDeposit.setExpenseId(loan.getId());

                long id = depositService.insertDeposit(repaymentDeposit);

                if (id != -1) {
                    Toast.makeText(this, String.format("Погашено %.2f руб.", repaymentAmount), Toast.LENGTH_SHORT).show();
                    loadLoans();
                } else {
                    Toast.makeText(this, "Ошибка при погашении", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNeutralButton("Список погашений", (dialog, which) -> {
            showDepositsListDialog(loan);
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }


    private void showDepositsListDialog(Expense loan) {

        if (loan == null) {
            Log.d(TAG, "showDepositsListDialog: loan == null");
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "showDepositsListDialog для займа ID=" + loan.getId() +
                ", " + loan.getName());

        List<Deposit> repayments = depositService.getRepaymentsForExpense(loan.getId());

        Log.d(TAG, "Получено погашений из БД: " + (repayments != null ? repayments.size() : 0));

        if (repayments == null || repayments.isEmpty()) {
            Log.d(TAG, "Нет погашений по этому займу");
            Toast.makeText(this, "Нет погашений по этому займу", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Deposit d : repayments) {
            Log.d(TAG, "  Погашение ID=" + d.getId() + ", название=" + d.getName() +
                    ", сумма=" + d.getTotalAmount() + ", платежей=" +
                    (d.getPayments() != null ? d.getPayments().size() : 0));
        }

        /** Создаёт диалог */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("История погашений займа: " + loan.getName());

        /** Инфлейтим layout */
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_deposit_list, null);
        ListView listView = dialogView.findViewById(R.id.listViewDeposits);
        Button buttonClose = dialogView.findViewById(R.id.buttonClose);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        /** Создаём адаптер для списка погашений с кнопками */
        DepositListAdapter adapter = new DepositListAdapter(this, repayments,
                new DepositListAdapter.OnDepositActionListener() {

                    @Override
                    public void onEditClick(Deposit deposit) {
                        Log.d(TAG, "onEditClick: deposit ID=" + deposit.getId());
                        dialog.dismiss();

                        /** Открывает детали для редактирования */
                        Intent intent = new Intent(LongLoansActivity.this, DepositDetailActivity.class);
                        intent.putExtra("deposit_id", deposit.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(Deposit deposit) {
                        Log.d(TAG, "onDeleteClick: deposit ID=" + deposit.getId());
                        confirmDeleteRepayment(deposit, loan, dialog);
                    }

                });

        listView.setAdapter(adapter);

        buttonClose.setOnClickListener(v -> {
            Log.d(TAG, "Диалог закрыт по кнопке");
            dialog.dismiss();
        });

        dialog.show();
    }


    private void confirmDeleteRepayment(Deposit deposit, Expense loan, AlertDialog parentDialog) {
        Log.d(TAG, "confirmDeleteRepayment: пытаемся удалить deposit ID=" + deposit.getId() +
                ", название=" + deposit.getName());

        new AlertDialog.Builder(this)
                .setTitle("Удаление погашения")
                .setMessage("Удалить погашение \"" + deposit.getName() + "\" на сумму " +
                        String.format("%.2f", deposit.getTotalAmount()) + " руб.?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    Log.d(TAG, "Пользователь подтвердил удаление");

                    boolean success = depositService.deleteDeposit(deposit.getId());
                    Log.d(TAG, "Результат удаления: " + success);

                    if (success) {
                        Toast.makeText(this, "Погашение удалено", Toast.LENGTH_SHORT).show();
                        parentDialog.dismiss();
                        loadLoans();
                    } else {
                        Log.e(TAG, "Ошибка при удалении deposit ID=" + deposit.getId());
                        Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Отмена", null)
                .show();
    }


    private void openLoanDetail(Expense loan) {
        Intent intent = new Intent(this, /* LongLoanDetailActivity.class */ ExpenseDetailActivity.class);
        intent.putExtra("expense_id", loan.getId());
        startActivity(intent);
    }


    private void confirmDeleteLoan(Expense loan) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление займа")
                .setMessage("Удалить заём \"" + loan.getName() + "\" вместе со всеми платежами и погашениями?")
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


    private double getTotalRepaid(Expense loan) {
        double total = 0.0;
        List<Deposit> repayments = depositService.getRepaymentsForExpense(loan.getId());
        for (Deposit deposit : repayments) {
            total += deposit.getTotalAmount();
        }
        return total;
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