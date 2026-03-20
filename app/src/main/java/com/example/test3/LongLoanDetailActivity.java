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

import java.util.List;

import com.example.test3.deposit.Deposit;
import com.example.test3.service.DepositService;

import java.time.ZonedDateTime;


public class LongLoanDetailActivity extends ExpenseDetailActivity {

    private static final String TAG = "LongLoanDetailActivity";
    private Button buttonRepay;

//    private TextView textViewRemainingDebt;

    private DepositService depositService;


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_long_loan_detail;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        depositService = new DepositService(this);
    }


    @Override
    protected void onResume() {

        super.onResume();

        if (currentExpense != null && depositService != null) {

            refreshData();

//            if (listViewPayments != null) {
//                listViewPayments.postDelayed(() -> {
//                    Log.d(TAG, "Показываем обновлённый список погашений");
//                    showDepositsListDialog();
//                }, 5);
//            }

        }

    }


    @Override
    protected void initViews() {

        super.initViews();

        /** Находим кнопку погашения */
        buttonRepay = findViewById(R.id.buttonRepay);

        if (buttonRepay == null) {
            Log.e(TAG, "buttonRepay is NULL! Проверьте layout activity_long_loan_detail.xml");
        } else {
            Log.d(TAG, "buttonRepay found successfully");
        }

    }


    @Override
    protected void setupListeners() {
        super.setupListeners();
        /** Добавляет обработчик для кнопки погашения */
        if (buttonRepay != null) {
            buttonRepay.setOnClickListener(v -> showRepayDialog());
        }
    }

    private void showRepayDialog() {

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

                if (repaymentAmount <= 0) {
                    Toast.makeText(this, "Сумма должна быть больше 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (repaymentAmount > currentTotal) {
                    Toast.makeText(this, "Сумма погашения не может превышать сумму долга", Toast.LENGTH_SHORT).show();
                    return;
                }

                /** Создаёт Deposit для погашения займа */
                Deposit repaymentDeposit = new Deposit(
                        "Погашение: " + currentExpense.getName(),
                        DepositService.TYPE_CREDIT_LOAN_REPAYMENT                                                           /** Тип 3 - погашение кредитных займов */
                );

                repaymentDeposit.setDescription("Погашение займа \"" + currentExpense.getName() + "\" на сумму " + repaymentAmount + " руб.");
                repaymentDeposit.setDateTime(ZonedDateTime.now());
                repaymentDeposit.addPayment(repaymentAmount);
                repaymentDeposit.setExpenseId(currentExpense.getId());

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
    }


    private void showDepositsListDialog() {

        if (currentExpense == null) {
            Log.d(TAG, "showDepositsListDialog: currentExpense == null");
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "showDepositsListDialog для займа ID=" + currentExpense.getId() +
                ", " + currentExpense.getName());

        List<Deposit> repayments = depositService.getRepaymentsForExpense(currentExpense.getId());

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
//    private void showDepositsListDialog() {
//        if (depositService == null || currentExpense == null) {
//            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Получаем список погашений для текущего займа (тип 3)
//        List<Deposit> repayments = depositService.getRepaymentsForExpense(currentExpense.getId());
//
//        if (repayments == null || repayments.isEmpty()) {
//            Toast.makeText(this, "Нет погашений по этому займу", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Создаем адаптер для списка погашений
//        DepositListAdapter adapter = new DepositListAdapter(this, repayments,
//                new DepositListAdapter.OnDepositActionListener() {
//                    @Override
//                    public void onEditClick(Deposit deposit) {
//                        // При нажатии "Редактировать" открывается DepositDetailActivity
//                        Intent intent = new Intent(LongLoanDetailActivity.this, DepositDetailActivity.class);
//                        intent.putExtra("deposit_id", deposit.getId());
//                        startActivity(intent);
//                    }
//
//                    @Override
//                    public void onDeleteClick(Deposit deposit) {
//                        // Подтверждение удаления погашения
//                        confirmDeleteRepayment(deposit);
//                    }
//                });
//
//        // Создаем и показываем диалог со списком
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("История погашений займа: " + currentExpense.getName());
//        builder.setAdapter(adapter, null);
//        builder.setPositiveButton("Закрыть", null);
//        builder.show();
//    }


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
//    private void confirmDeleteRepayment(Deposit deposit) {
//        new AlertDialog.Builder(this)
//                .setTitle("Удаление погашения")
//                .setMessage("Удалить погашение \"" + deposit.getName() + "\" на сумму " +
//                        String.format("%.2f", deposit.getTotalAmount()) + " руб.?")
//                .setPositiveButton("Удалить", (dialog, which) -> {
//                    if (depositService.deleteDeposit(deposit.getId())) {
//                        Toast.makeText(this, "Погашение удалено", Toast.LENGTH_SHORT).show();
//                        refreshData(); // Обновляем данные
//                    } else {
//                        Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Отмена", null)
//                .show();
//    }


    private void showFullyRepaidDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Заём полностью погашен")
                .setMessage("Заём полностью погашен. Закрыть окно?")
                .setPositiveButton("Закрыть", (dialog, which) -> finish())
                .setNegativeButton("Остаться", null)
                .show();
    }


}