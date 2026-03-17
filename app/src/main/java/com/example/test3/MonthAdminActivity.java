package com.example.test3;

import static com.example.test3.month.Month.TYPE_MONTHLY_EXPENSES;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.dao.ExpenseSQLite;
import com.example.test3.deposit.Deposit;
import com.example.test3.month.Month;
import com.example.test3.month.MonthAdminAdapter;
import com.example.test3.month.MonthlyDto;
import com.example.test3.service.DepositService;
import com.example.test3.service.MonthService;
import com.example.test3.util.Util;

import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MonthAdminActivity extends AppCompatActivity {

    private static final String TAG = "MonthAdminActivity";

    private ListView listViewMonths;
    private TextView textViewTotalExpenses, textViewTotalDeposits, textViewTotalBalance;
    private Button buttonAddDeposit, buttonBack;

    private MonthService monthService;
    private DepositService depositService;
    private List<MonthlyDto> monthlyDtos;
    private MonthAdminAdapter adapter;
    private MonthlyDto selectedMonthDto = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_admin);

        monthService = new MonthService(this);
        depositService = new DepositService(this);

        initViews();
        setupListeners();
        loadData();
    }


    private void initViews() {
        listViewMonths = findViewById(R.id.listViewMonths);
        textViewTotalExpenses = findViewById(R.id.textViewTotalExpenses);
        textViewTotalDeposits = findViewById(R.id.textViewTotalDeposits);
        textViewTotalBalance = findViewById(R.id.textViewTotalBalance);
        buttonAddDeposit = findViewById(R.id.buttonAddDeposit);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void setupListeners() {

        buttonAddDeposit.setOnClickListener(v -> {

            if (selectedMonthDto == null) {
                Toast.makeText(this, "Выберите месяц", Toast.LENGTH_SHORT).show();
                return;
            }

            showAddDepositDialog();

        });

        buttonBack.setOnClickListener(v -> finish());
    }


    @Override
    protected void onResume() {

        super.onResume();
        Log.d(TAG, "onResume - обновляем данные после редактирования");


        loadData();


        if (selectedMonthDto != null && monthlyDtos != null) {

            long selectedMonthId = selectedMonthDto.getMonth().getId();
            boolean found = false;

            for (int i = 0; i < monthlyDtos.size(); i++) {

                if (monthlyDtos.get(i).getMonth() != null &&
                        monthlyDtos.get(i).getMonth().getId() == selectedMonthId) {
                    selectedMonthDto = monthlyDtos.get(i);
                    adapter.setSelectedPosition(i);
                    buttonAddDeposit.setEnabled(true);
                    Log.d(TAG, "Восстановлен выбранный месяц: " + selectedMonthDto.getMonth().getMonthYear());
                    found = true;
                    break;
                }

            }


            if (found) {

                listViewMonths.postDelayed(() -> {
                    Log.d(TAG, "Показываем обновлённый список взносов");
                    showDepositsListDialog();
                }, 5);

            }

        }

    }


    private void loadData() {

        monthlyDtos = monthService.getAllMonthlyDtos(TYPE_MONTHLY_EXPENSES);

        if (monthlyDtos.isEmpty()) {
            Toast.makeText(this, "Нет данных за месяцы", Toast.LENGTH_LONG).show();
        }


        adapter = new MonthAdminAdapter(this, monthlyDtos);
        adapter.setOnItemClickListener((dto, position) -> {

            selectedMonthDto = dto;
            adapter.setSelectedPosition(position);
            buttonAddDeposit.setEnabled(true);
            Toast.makeText(MonthAdminActivity.this,
                    "Выбран: " + dto.getMonth().getMonthYear(),
                    Toast.LENGTH_SHORT).show();

        });


        listViewMonths.setAdapter(adapter);
        updateTotalStats();
    }


    private void updateTotalStats() {

        double totalExpenses = 0;
        double totalDeposits = 0;
        double totalBalance = 0;

        for (MonthlyDto dto : monthlyDtos) {
            totalExpenses += dto.getTotalExpenseAmount();
            totalDeposits += dto.getTotalDepositAmount();
            totalBalance += dto.getBalance();
        }

        DecimalFormat df = new DecimalFormat("#,##0.00");
        textViewTotalExpenses.setText("Расходы: " + df.format(totalExpenses) + " руб.");
        textViewTotalDeposits.setText("Взносы: " + df.format(totalDeposits) + " руб.");

        String balanceStr = "Баланс: " + df.format(totalBalance) + " руб.";
        textViewTotalBalance.setText(balanceStr);

        if (totalBalance > 0) {
            textViewTotalBalance.setTextColor(getColor(android.R.color.holo_green_dark));
        } else if (totalBalance < 0) {
            textViewTotalBalance.setTextColor(getColor(android.R.color.holo_red_dark));
        } else {
            textViewTotalBalance.setTextColor(getColor(android.R.color.darker_gray));
        }

    }


    private void showAddDepositDialog() {

        if (selectedMonthDto == null) {
            Toast.makeText(this, "Выберите месяц", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить взнос за " + selectedMonthDto.getMonth().getMonthYear());

        View view = getLayoutInflater().inflate(R.layout.dialog_add_deposit, null);
        EditText editTextName = view.findViewById(R.id.editTextDepositName);
        EditText editTextAmount = view.findViewById(R.id.editTextDepositAmount);
        EditText editTextDescription = view.findViewById(R.id.editTextDepositDescription);

        /** Предлагает название по умолчанию */
        editTextName.setText("Взнос за " + selectedMonthDto.getMonth().getMonthYear());

        builder.setView(view);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String name = editTextName.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
                return;
            }


            try {

                double amount = Double.parseDouble(amountStr);

                /** Создаёт новый deposit */
                Deposit deposit = new Deposit(name, DepositService.TYPE_MONTHLY_DEPOSIT);
                if(!description.isEmpty()) deposit.setDescription(description);


                /** Создаёт дату с первым числом выбранного месяца */
                java.time.ZonedDateTime monthDate = java.time.ZonedDateTime.now()
                        .withYear(selectedMonthDto.getMonth().getYear())
                        .withMonth(selectedMonthDto.getMonth().getMonth())
                        .withDayOfMonth(1);
                deposit.setDateTime(monthDate);

                deposit.addPayment(amount);

                /** Устанавливает expenseId = ID месяца (для type=1) */
                deposit.setExpenseId(selectedMonthDto.getMonth().getId());


                /** Сохраняет в БД */
                long id = depositService.insertDeposit(deposit);

                if (id != -1) {
                    Toast.makeText(this, "Взнос добавлен", Toast.LENGTH_SHORT).show();
                    loadData();                                                                     /** Перезагружает данные */
                } else {
                    Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректная сумма", Toast.LENGTH_SHORT).show();
            }

        });


        builder.setNegativeButton("Отмена", null);
        builder.setNeutralButton("Список взносов", (dialog, which) -> {
            showDepositsListDialog();
        });


        builder.show();
    }


    private void showDepositsListDialog() {

        if (selectedMonthDto == null) {
            Log.d(TAG, "showDepositsListDialog: selectedMonthDto == null");
            Toast.makeText(this, "Выберите месяц", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "showDepositsListDialog для месяца ID=" + selectedMonthDto.getMonth().getId() +
                ", " + selectedMonthDto.getMonth().getMonthYear());


        List<Deposit> deposits = depositService.getDepositsForMonth(selectedMonthDto.getMonth().getId());

        Log.d(TAG, "Получено взносов из БД: " + (deposits != null ? deposits.size() : 0));

        if (deposits == null || deposits.isEmpty()) {
            Log.d(TAG, "Нет взносов за этот месяц");
            Toast.makeText(this, "Нет взносов за этот месяц", Toast.LENGTH_SHORT).show();
            return;
        }


        for (Deposit d : deposits) {
            Log.d(TAG, "  Взнос ID=" + d.getId() + ", название=" + d.getName() +
                    ", сумма=" + d.getTotalAmount() + ", платежей=" +
                    (d.getPayments() != null ? d.getPayments().size() : 0));
        }


        /** Создаёт диалог */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Взносы за " + selectedMonthDto.getMonth().getMonthYear());

        /** Инфлейтим layout */
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_deposit_list, null);
        ListView listView = dialogView.findViewById(R.id.listViewDeposits);
        Button buttonClose = dialogView.findViewById(R.id.buttonClose);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        /** Создаём адаптер для списка взносов с кнопками */
        DepositListAdapter adapter = new DepositListAdapter(this, deposits,
                new DepositListAdapter.OnDepositActionListener() {

                    @Override
                    public void onEditClick(Deposit deposit) {

                        Log.d(TAG, "onEditClick: deposit ID=" + deposit.getId());

                        dialog.dismiss();

                        /** Открывает детали для редактирования */
                        Intent intent = new Intent(MonthAdminActivity.this, DepositDetailActivity.class);
                        intent.putExtra("deposit_id", deposit.getId());
                        startActivity(intent);

                    }

                    @Override
                    public void onDeleteClick(Deposit deposit) {

                        Log.d(TAG, "onDeleteClick: deposit ID=" + deposit.getId());
                        confirmDeleteDeposit(deposit);

                    }

                });

        listView.setAdapter(adapter);


        buttonClose.setOnClickListener(v -> {
            Log.d(TAG, "Диалог закрыт по кнопке");
            dialog.dismiss();
        });

        dialog.show();
    }


    private void confirmDeleteDeposit(Deposit deposit) {

        Log.d(TAG, "confirmDeleteDeposit: пытаемся удалить deposit ID=" + deposit.getId() +
                ", название=" + deposit.getName());

        new AlertDialog.Builder(this)
                .setTitle("Удаление взноса")
                .setMessage("Удалить взнос \"" + deposit.getName() + "\" вместе со всеми платежами?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    Log.d(TAG, "Пользователь подтвердил удаление");

                    boolean success = depositService.deleteDeposit(deposit.getId());
                    Log.d(TAG, "Результат удаления: " + success);


                    if (success) {

                        Toast.makeText(this, "Взнос удалён", Toast.LENGTH_SHORT).show();


                        loadData();
                        Log.d(TAG, "loadData() выполнен");

                        long monthId = deposit.getExpenseId();

                        if (monthId > 0 && monthlyDtos != null) {

                            for (MonthlyDto dto : monthlyDtos) {

                                if (dto.getMonth() != null && dto.getMonth().getId() == monthId) {
                                    selectedMonthDto = dto;
                                    buttonAddDeposit.setEnabled(true);
                                    Log.d(TAG, "Восстановлен выбранный месяц: " + dto.getMonth().getMonthYear());
                                    break;
                                }

                            }

                        }


                        dialog.dismiss();


                        listViewMonths.postDelayed(() -> {
                            Log.d(TAG, "Показываем обновлённый список взносов");
                            showDepositsListDialog();
                        }, 300);

                    } else {
                        Log.e(TAG, "Ошибка при удалении deposit ID=" + deposit.getId());
                        Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Отмена", null)
                .show();

    }


}