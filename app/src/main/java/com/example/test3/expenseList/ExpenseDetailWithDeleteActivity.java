package com.example.test3.expenseList;

import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.test3.ExpenseDetailActivity;
import com.example.test3.R;

public class ExpenseDetailWithDeleteActivity extends ExpenseDetailActivity {

    private Button buttonDeleteExpense;


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_expense_detail_with_delete;
    }


    @Override
    protected void initViews() {
        super.initViews();                      /** все стандартные View из родителя */
        buttonDeleteExpense = findViewById(R.id.buttonDeleteExpense);
    }

    @Override
    protected void setupListeners() {
        super.setupListeners();                 /** слушатели для кнопок родителя */
        if (buttonDeleteExpense != null) {
            buttonDeleteExpense.setOnClickListener(v -> showDeleteConfirmationDialog());
        }
    }


    private void showDeleteConfirmationDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Удаление расхода")
                .setMessage("Вы уверены, что хотите удалить расход \"" + currentExpense.getName() + "\" и все его платежи?")
                .setPositiveButton("Удалить", (dialog, which) -> {

                    // todo: по типу Expense подтянуть относящмеся к ней Deposit и, если они не удаляются в вызывающем сервмсе - удалить здесь
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