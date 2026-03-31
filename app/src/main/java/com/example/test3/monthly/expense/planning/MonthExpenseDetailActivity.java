package com.example.test3.monthly.expense.planning;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.ExpenseDetailActivity;
import com.example.test3.R;
import com.example.test3.expenseList.Expense;
import com.example.test3.service.ExpenseService;

import java.util.List;

public class MonthExpenseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MONTH_DTO = "month_dto";

    private MonthlyExpensePlanningDto monthDto;
    private ListView listViewExpenses;
    private TextView textViewMonthName;
    private Button buttonBack;

    private ExpenseService expenseService;
    private MonthlyExpensePlanningAdapterForExpenses adapter;                                       /** адаптер для списка запланированных месячных расходов */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_expense_detail);

        monthDto = (MonthlyExpensePlanningDto) getIntent().getSerializableExtra(EXTRA_MONTH_DTO);
        if (monthDto == null || monthDto.getMonth() == null) {
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        expenseService = new ExpenseService(this);

        textViewMonthName = findViewById(R.id.textViewMonthName);
        listViewExpenses = findViewById(R.id.listViewExpenses);
        buttonBack = findViewById(R.id.buttonBack);

        textViewMonthName.setText(monthDto.getMonth().getMonthYear());

        List<Expense> expenses = monthDto.getExpenseList();
        if (expenses == null || expenses.isEmpty()) {
            Toast.makeText(this, "Нет расходов за этот месяц", Toast.LENGTH_SHORT).show();
        }

        adapter = new MonthlyExpensePlanningAdapterForExpenses(this, expenses, expenseService);
        adapter.setOnItemClickListener((expense, position) -> {
            Intent intent = new Intent(MonthExpenseDetailActivity.this, ExpenseDetailActivity.class);
            intent.putExtra("expense_id", expense.getId());
            startActivity(intent);
        });

        listViewExpenses.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());
    }


    @Override
    protected void onResume() {

        super.onResume();

        /** Обновляет данные, после возможного редактирования расходов */
        List<Expense> updatedExpenses = monthDto.getExpenseList();
        adapter.clear();
        adapter.addAll(updatedExpenses);
        adapter.notifyDataSetChanged();

    }


}