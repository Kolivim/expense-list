package com.example.test3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import com.example.test3.expenseList.Expense;
import com.example.test3.service.DepositService;
import com.example.test3.service.ExpenseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LongLoansUniversalActivity extends AppCompatActivity {

    private static final String TAG = "LongLoansUniversalActivity";
    private static final String EXTRA_LOAN_TYPE = "loan_type";
    private static final String EXTRA_TITLE = "title";

    private static final String EXTRA_REPAYMENT_TYPE = "repayment_type";

    private ListView listViewLoans;
    private TextView textViewTotalAmount;
    private Button buttonSave, buttonBack, buttonUpdate, buttonDelete;
    private EditText editTextDate, editTextName, editTextAmount;

    private ExpenseService expenseService;
    private DepositService depositService;
    private LongLoanUniversalAdapter loanAdapter;
    private ArrayList<Expense> loansList;
    private Expense selectedLoan = null;

    private Long loanType;
    private String activityTitle;
    private Long repaymentType;


    public static void start(Context context, long loanType, Long repaymentType, String title) {
        Intent intent = new Intent(context, LongLoansUniversalActivity.class);
        intent.putExtra(EXTRA_LOAN_TYPE, loanType);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra("repayment_type", repaymentType);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_loans_own_funds); //  activity_long_loans

        loanType = getIntent().getLongExtra(EXTRA_LOAN_TYPE, 3L);
        activityTitle = getIntent().getStringExtra(EXTRA_TITLE);
        repaymentType = getIntent().getLongExtra(EXTRA_REPAYMENT_TYPE, DepositService.TYPE_CREDIT_LOAN_REPAYMENT);

//        if (activityTitle == null) {
//            activityTitle = loanType == 3L ?
//                    "Длинные займы (кредитные средства)" : "Длинные займы (собственные средства)";
//        }
//
//        setTitle(activityTitle);


        /** Устанавливает заголовок */
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        if (textViewTitle != null) {
            if (activityTitle != null && !activityTitle.isEmpty()) {
                textViewTitle.setText(activityTitle);
            } else if (loanType == 3L) {
                textViewTitle.setText("Длинные займы (кредитные средства)");
            } else {
                textViewTitle.setText("Длинные займы (собственные средства)");
            }
        }


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
                Toast.makeText(this, "Выберите займ для изменения", Toast.LENGTH_SHORT).show();
                return;
            }
            openLoanDetail(selectedLoan);
        });

        buttonDelete.setOnClickListener(v -> {
            if (selectedLoan == null) {
                Toast.makeText(this, "Выберите займ для удаления", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmDeleteLoan(selectedLoan);
        });

    }


    private void loadLoans() {

        loansList = expenseService.getExpenseList(loanType);

        if (loansList.isEmpty()) {
            textViewTotalAmount.setText("Общая сумма: 0.00 руб.");
        } else {
            double total = 0.0;
            for (Expense loan : loansList) {
                total += loan.getExpenseListTotalAmount();
            }
            textViewTotalAmount.setText(String.format("Общая сумма: %.2f руб.", total));
        }

        loanAdapter = new LongLoanUniversalAdapter(this, loansList, expenseService, depositService, repaymentType);
        loanAdapter.setOnItemClickListener((expense, position) -> {

            selectedLoan = expense;
            loanAdapter.setSelectedPosition(position);
            Toast.makeText(this, "Выбран: " + expense.getName(), Toast.LENGTH_SHORT).show();

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

            Expense newLoan = new Expense(name, loanType);
            newLoan.addPayment(amount);

            if (!dateStr.isEmpty()) {
                ZonedDateTime dateTime = parseDate(dateStr);
                if (dateTime != null) {
                    newLoan.setDateTime(dateTime);
                }
            }

            if (expenseService.insertExpense(newLoan)) {
                Toast.makeText(this, "Займ добавлен", Toast.LENGTH_SHORT).show();
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

        String message = String.format("Займ: %s\nCумма займа: %.2f руб.\nПогашено: %.2f руб.\nОстаток долга: %.2f руб.\n\nВведите сумму для погашения:",
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
                        repaymentType
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
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Deposit> repayments = depositService.getRepaymentsForExpenseByType(loan.getId(), repaymentType);

        if (repayments == null || repayments.isEmpty()) {
            Toast.makeText(this, "Нет погашений по этому займу", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("История погашений займа: " + loan.getName());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_deposit_list, null);
        ListView listView = dialogView.findViewById(R.id.listViewDeposits);
        Button buttonClose = dialogView.findViewById(R.id.buttonClose);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        DepositListAdapter adapter = new DepositListAdapter(this, repayments,
                new DepositListAdapter.OnDepositActionListener() {

                    @Override
                    public void onEditClick(Deposit deposit) {
                        dialog.dismiss();
                        Intent intent = new Intent(LongLoansUniversalActivity.this, DepositDetailActivity.class);
                        intent.putExtra("deposit_id", deposit.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(Deposit deposit) {
                        confirmDeleteRepayment(deposit, loan, dialog);
                    }

                });

        listView.setAdapter(adapter);
        buttonClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void confirmDeleteRepayment(Deposit deposit, Expense loan, AlertDialog parentDialog) {

        new AlertDialog.Builder(this)
                .setTitle("Удаление погашения")
                .setMessage("Удалить погашение \"" + deposit.getName() + "\" на сумму " +
                        String.format("%.2f", deposit.getTotalAmount()) + " руб.?")
                .setPositiveButton("Удалить", (dialog, which) -> {

                    if (depositService.deleteDeposit(deposit.getId())) {
                        Toast.makeText(this, "Погашение удалено", Toast.LENGTH_SHORT).show();
                        parentDialog.dismiss();
                        loadLoans();
                    } else {
                        Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Отмена", null)
                .show();
    }


    private void openLoanDetail(Expense loan) {
        Intent intent = new Intent(this, LongLoanDetailActivity.class);
        intent.putExtra("expense_id", loan.getId());
        startActivity(intent);
    }


    private void confirmDeleteLoan(Expense loan) {

        new AlertDialog.Builder(this)
                .setTitle("Удаление займа")
                .setMessage("Удалить займ \"" + loan.getName() + "\" вместе со всеми платежами и погашениями?")
                .setPositiveButton("Удалить", (dialog, which) -> {

                    if (expenseService.removeExpense(loan)) {
                        Toast.makeText(this, "Займ удалён", Toast.LENGTH_SHORT).show();
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
        List<Deposit> repayments = depositService.getRepaymentsForExpenseByType(loan.getId(), repaymentType);
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