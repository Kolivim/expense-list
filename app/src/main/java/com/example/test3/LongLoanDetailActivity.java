package com.example.test3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.example.test3.account.Account;
import com.example.test3.deposit.Deposit;
import com.example.test3.payment.PaymentAdapter;
import com.example.test3.service.DepositService;

import java.time.ZonedDateTime;


public class LongLoanDetailActivity extends ExpenseDetailActivity {

    private static final String TAG = "LongLoanDetailActivity";

    public static final String EXTRA_LOAN_EXPENSE_TYPE = "expense_type";
    public static final String EXTRA_LOAN_DEPOSIT_TYPE = "deposit_type";

    private Account currentExpenseAccount;

    Long depositType;
    Long expenseType;   /** repaymentType */

    private Button buttonRepay, buttonUpdateAccount;
    private View accountInfo;
    private TextView textViewAccountNumber, textViewAccountName;

    private DepositService depositService;


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_long_loan_detail;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        depositService = new DepositService(this);

        expenseType = getIntent().getLongExtra(EXTRA_LOAN_EXPENSE_TYPE, -1);
        Log.d(TAG, "Получен expenseType: " + expenseType);

        depositType = getIntent().getLongExtra(EXTRA_LOAN_DEPOSIT_TYPE, -1);
        Log.d(TAG, "Получен depositType: " + depositType);

    }


    @Override
    protected void onResume() {

        super.onResume();
        if (currentExpense != null && depositService != null) refreshData();

    }


    @Override
    protected void initViews() {

        super.initViews();

        /** Находим кнопку погашения */
        buttonRepay = findViewById(R.id.buttonRepay);
        buttonUpdateAccount = findViewById(R.id.buttonUpdateAccount);

        accountInfo = findViewById(R.id.accountInfo);
        textViewAccountNumber = findViewById(R.id.textViewAccountNumber);
        textViewAccountName = findViewById(R.id.textViewAccountName);

    }


    @Override
    protected void setupListeners() {
        Log.d(TAG, "setupListeners() startMethod");

        super.setupListeners();

        if (buttonRepay != null) {
            buttonRepay.setOnClickListener(v -> showRepayDialog());
        }

        if (buttonUpdateAccount != null) {
            buttonUpdateAccount.setOnClickListener(v -> showUpdateAccountDialog());
        }

        Log.d(TAG, "setupListeners() endMethod");
    }


    @Override
    protected void updateDisplay() {
        Log.d(TAG, "updateDisplay() startMethod");

        super.updateDisplay();

        currentExpenseAccount = getCurrentExpenseAccount();

        if(currentExpenseAccount != null) {

            accountInfo.setVisibility(View.VISIBLE);

            textViewAccountNumber.setText("Номер счёта: ".concat(currentExpenseAccount.getNumber()));

            if(currentExpenseAccount.getName() != null) {
                textViewAccountName.setText("| ".concat(currentExpenseAccount.getName()));
            } else {
                textViewAccountName.setText("");
            }

        } else {
            accountInfo.setVisibility(View.GONE);
        }

        Log.d(TAG, "updateDisplay() endMethod");
    }


    private Account getCurrentExpenseAccount() {
        return expenseService.getExpenseAccount(currentExpense.getId());
    }


    private void showUpdateAccountDialog() {
        Log.d(TAG, "showUpdateAccountDialog() startMethod");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_info, null);
        EditText editAccountNumber = dialogView.findViewById(R.id.editAccountNumber);
        EditText editAccountName = dialogView.findViewById(R.id.editAccountName);

        /** Если счёт уже существует - предзаполняет поля */
        if (currentExpenseAccount != null) {
            editAccountNumber.setText(currentExpenseAccount.getNumber());
            if (currentExpenseAccount.getName() != null) {
                editAccountName.setText(currentExpenseAccount.getName());
            }
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Информация о счёте/банке")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String number = editAccountNumber.getText().toString().trim();
                    String name = editAccountName.getText().toString().trim();

                    if (number.isEmpty()) {
                        Toast.makeText(this, "Введите номер счёта", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success;
                    if (currentExpenseAccount == null) {

                        /** Создаёт новую запись : */
                        Account newAccount = new Account();
                        newAccount.setNumber(number);
                        newAccount.setName(name.isEmpty() ? null : name);
                        newAccount.setType(0L);                                                     /** 0 = Expense */
                        /** !Создаёт новую запись */

                        success = expenseService.insertAccountNumber(currentExpense.getId(), newAccount);

                    } else {

                        /** Обновляет существующую запись Account : */
                        currentExpenseAccount.setNumber(number);
                        currentExpenseAccount.setName(name.isEmpty() ? null : name);
                        /** !Обновляет существующую запись Account */

                        success = expenseService.updateAccountNumber(currentExpenseAccount);

                    }

                    if (success) {
                        Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Отмена", null);


        /** Если currentExpenseAccount уже есть счёт, добавляет кнопку "Удалить" */
        if (currentExpenseAccount != null) {

            builder.setNeutralButton("Удалить", (dialog, which) -> {
                boolean deleted = expenseService.deleteAccountNumber(currentExpense.getId());
                if (deleted) {
                    Toast.makeText(this, "Счёт удалён", Toast.LENGTH_SHORT).show();
                    refreshData();
                } else {
                    Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                }
            });

        }

        builder.show();

        Log.d(TAG, "showUpdateAccountDialog() endMethod");
    }


    private void showRepayDialog() {
        Log.d(TAG, "showRepayDialog() startMethod");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Погашение займа");

        double currentTotal = currentExpense.getExpenseListTotalAmount();
        String message = String.format("Займ: %s\nТекущая сумма долга: %.2f руб.\n\nВведите сумму для погашения:",
                currentExpense.getName(),
                currentTotal);

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


                /** Проверка введённых пользователем значений : */
                if (repaymentAmount <= 0) {
                    Toast.makeText(this, "Сумма должна быть больше 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (repaymentAmount > currentTotal) {
                    Toast.makeText(this, "Сумма погашения не может превышать сумму долга", Toast.LENGTH_SHORT).show();
                    return;
                }
                /** !Проверка введённых пользователем значений */


                /** Создаёт Deposit для погашения займа : */
                Deposit repaymentDeposit = new Deposit(
                        "Погашение: " + currentExpense.getName(), depositType   /**  DepositService.TYPE_CREDIT_LOAN_REPAYMENT  */                                        /** Тип 3 - погашение кредитных займов */
                );

                repaymentDeposit.setDescription("Погашение займа \"" + currentExpense.getName() + "\" на сумму " + repaymentAmount + " руб.");
                repaymentDeposit.setDateTime(ZonedDateTime.now());
                repaymentDeposit.addPayment(repaymentAmount);
                repaymentDeposit.setExpenseId(currentExpense.getId());
                /** !Создаёт Deposit для погашения займа */


                /** Сохраняет в БД */
                long id = depositService.insertDeposit(repaymentDeposit);

                if (id != -1) {
                    Toast.makeText(this, String.format("Погашено %.2f руб.", repaymentAmount), Toast.LENGTH_SHORT).show();
                    refreshData();
                } else {
                    Toast.makeText(this, "Ошибка при погашении", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show();
            }

        });


        builder.setNeutralButton("Список взносов", (dialog, which) -> {
            showDepositsListDialog();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();


        Log.d(TAG, "showRepayDialog() endMethod");
    }


    private void showDepositsListDialog() {

        if (currentExpense == null) {
            Log.d(TAG, "showDepositsListDialog: currentExpense == null");
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "showDepositsListDialog для займа ID=" + currentExpense.getId() +
                ", " + currentExpense.getName());

        List<Deposit> repayments = depositService.getRepaymentsForExpenseByType(currentExpense.getId(), depositType);

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
        builder.setTitle("История погашений займа: " + currentExpense.getName());

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
                        Intent intent = new Intent(LongLoanDetailActivity.this, DepositDetailActivity.class);
                        intent.putExtra("deposit_id", deposit.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(Deposit deposit) {
                        Log.d(TAG, "onDeleteClick: deposit ID=" + deposit.getId());
                        confirmDeleteRepayment(deposit, dialog);
                    }

                });

        listView.setAdapter(adapter);

        buttonClose.setOnClickListener(v -> {
            Log.d(TAG, "Диалог закрыт по кнопке");
            dialog.dismiss();
        });

        dialog.show();
    }


    /** Подтверждение удаления погашения */
    private void confirmDeleteRepayment(Deposit deposit, AlertDialog parentDialog) {
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
                        refreshData();

                        // Используем listViewPayments из родительского класса
                        if (listViewPayments != null) {
                            listViewPayments.postDelayed(() -> {
                                Log.d(TAG, "Показываем обновлённый список погашений");
                                showDepositsListDialog();
                            }, 300);
                        }
                    } else {
                        Log.e(TAG, "Ошибка при удалении deposit ID=" + deposit.getId());
                        Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }


    private void showFullyRepaidDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Заём полностью погашен")
                .setMessage("Заём полностью погашен. Закрыть окно?")
                .setPositiveButton("Закрыть", (dialog, which) -> finish())
                .setNegativeButton("Остаться", null)
                .show();
    }


}