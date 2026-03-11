package com.example.test3.expenseList;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.test3.R;
import com.example.test3.service.ExpenseService;

import java.util.ArrayList;

public class ExpenseAdapter extends ArrayAdapter<Expense> {

    private Context context;
    private ArrayList<Expense> expenseList;
    private ExpenseService expenseService;

    private OnItemClickListener listener;

    /** Выделение выбранной строки */
    private int selectedPosition = -1;


    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }


    public interface OnItemClickListener {
        void onItemClick(Expense expense, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ExpenseAdapter(Context context, ArrayList<Expense> expenseList, ExpenseService expenseService) {
        super(context, R.layout.list_item_expense, expenseList);
        this.context = context;
        this.expenseList = expenseList;
        this.expenseService = expenseService;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_expense, parent, false);
        }

        Expense expense = expenseList.get(position);

        TextView textViewInfo = convertView.findViewById(R.id.textViewExpenseInfo);
        Button buttonAdd = convertView.findViewById(R.id.buttonAddPayment);

        // Показываем информацию о расходе
        String expenseText = expense.getName();
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            expenseText += " (" + expense.getDescription() + ")";
        }

        // Добавляем информацию о платежах
        if (expense.getExpenseList() != null && !expense.getExpenseList().isEmpty()) {
            expenseText += "\nСумма: " + String.format("%.2f", expense.getExpenseListTotalAmount()) +
                    " руб. | Платежей: " + expense.getExpenseList().size();
        } else {
            expenseText += "\nНет платежей";
        }

        textViewInfo.setText(expenseText);

//        // Обработчик нажатия на кнопку +
//        buttonAdd.setOnClickListener(v -> showAddPaymentDialog(position, expense));

        // Обработчик нажатия на кнопку +
        buttonAdd.setOnClickListener(v -> {
            v.setFocusable(true);
            v.requestFocus();
            showAddPaymentDialog(position, expense);
        });

        // Обработчик нажатия на всю строку (кроме кнопки)
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(expense, position);
            }
        });


        /** Выделяет выбранную строку */
        if (position == selectedPosition) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_color));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }


        return convertView;
    }

    private void showAddPaymentDialog(int position, Expense expense) {
        // Создаём диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Добавить платёж");
        builder.setMessage("Расход: " + expense.getName());

        // Поле для ввода суммы
        final EditText input = new EditText(context);
        input.setHint("Введите сумму");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_SIGNED |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Кнопки
        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String valueStr = input.getText().toString().trim();
            if (valueStr.isEmpty()) {
                Toast.makeText(context, "Введите сумму", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double payment = Double.parseDouble(valueStr);

                // Добавляем платёж в БД
                if (expenseService.addPaymentToExpense(expense, payment)) {
                    Toast.makeText(context, "Платёж добавлен: " + String.format("%.2f", payment),
                            Toast.LENGTH_SHORT).show();
                    // Обновляем только этот элемент в списке
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Ошибка при добавлении платежа",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Введите корректное число",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}