package com.example.test3.payment;

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

import com.example.test3.R;
import com.example.test3.expenseList.Expense;
import com.example.test3.service.ExpenseService;

import java.util.ArrayList;

public class PaymentAdapter extends ArrayAdapter<Double> {

    private Context context;
    private ArrayList<Double> payments;
    private Expense expense;
    private ExpenseService expenseService;
    private OnPaymentChangedListener listener;

    public interface OnPaymentChangedListener {
        void onPaymentChanged();
    }

    public PaymentAdapter(Context context, ArrayList<Double> payments,
                          Expense expense, ExpenseService expenseService,
                          OnPaymentChangedListener listener) {
        super(context, R.layout.list_item_payment, payments);
        this.context = context;
        this.payments = payments;
        this.expense = expense;
        this.expenseService = expenseService;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_payment, parent, false);
        }

        Double payment = payments.get(position);
        int paymentId = position + 1;

        TextView textViewPayment = convertView.findViewById(R.id.textViewPayment);
        Button buttonEdit = convertView.findViewById(R.id.buttonEditPayment);
        Button buttonDelete = convertView.findViewById(R.id.buttonDeletePayment);

        String paymentText = String.format("%d. %.2f руб.", paymentId, payment);
        textViewPayment.setText(paymentText);

        final int currentPosition = position;

        buttonEdit.setOnClickListener(v -> showEditPaymentDialog(currentPosition, payment));
        buttonDelete.setOnClickListener(v -> showDeleteConfirmationDialog(currentPosition, payment));

        return convertView;
    }

    private void showEditPaymentDialog(int position, Double oldPayment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Изменить платёж");
        builder.setMessage("Введите новую сумму:");

        final EditText input = new EditText(context);
        input.setHint("Сумма");
        input.setText(String.valueOf(oldPayment));
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_SIGNED |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String valueStr = input.getText().toString().trim();
            if (!valueStr.isEmpty()) {
                try {
                    double newPayment = Double.parseDouble(valueStr);

                    if (expenseService.updatePayment(expense, position, newPayment)) {
                        Toast.makeText(context, "Платёж изменён", Toast.LENGTH_SHORT).show();
                        listener.onPaymentChanged();
                    } else {
                        Toast.makeText(context, "Ошибка при изменении", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Введите корректное число", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog(int position, Double payment) {
        new AlertDialog.Builder(context)
                .setTitle("Удаление платежа")
                .setMessage(String.format("Удалить платёж %.2f руб.?", payment))
                .setPositiveButton("Удалить", (dialog, which) -> {
                    if (expenseService.deletePayment(expense, position)) {
                        Toast.makeText(context, "Платёж удалён", Toast.LENGTH_SHORT).show();
                        listener.onPaymentChanged();
                    } else {
                        Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}

//package com.example.test3.payment;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.test3.R;
//import com.example.test3.expenseList.Expense;
//import com.example.test3.service.ExpenseService;
//
//import java.util.ArrayList;
//
//public class PaymentAdapter extends ArrayAdapter<Double> {
//
//    private Context context;
//    private ArrayList<Double> payments;
//    private Expense expense;
//    private ExpenseService expenseService;
//    private OnPaymentChangedListener listener;
//
//    public interface OnPaymentChangedListener {
//        void onPaymentChanged();
//    }
//
//    public PaymentAdapter(Context context, ArrayList<Double> payments,
//                          Expense expense, ExpenseService expenseService,
//                          OnPaymentChangedListener listener) {
//        super(context, R.layout.list_item_payment, payments);
//        this.context = context;
//        this.payments = payments;
//        this.expense = expense;
//        this.expenseService = expenseService;
//        this.listener = listener;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
//            LayoutInflater inflater = LayoutInflater.from(context);
//            convertView = inflater.inflate(R.layout.list_item_payment, parent, false);
//        }
//
//        Double payment = payments.get(position);
//        int paymentId = position + 1; // В реальном приложении нужно хранить ID платежа
//
//        TextView textViewPayment = convertView.findViewById(R.id.textViewPayment);
//        Button buttonEdit = convertView.findViewById(R.id.buttonEditPayment);
//        Button buttonDelete = convertView.findViewById(R.id.buttonDeletePayment);
//
//        // Отображаем информацию о платеже
//        String paymentText = String.format("%d. %.2f руб.", paymentId, payment);
//        textViewPayment.setText(paymentText);
//
//        // Кнопка изменения
//        buttonEdit.setOnClickListener(v -> showEditPaymentDialog(position, payment));
//
//        // Кнопка удаления
//        buttonDelete.setOnClickListener(v -> showDeleteConfirmationDialog(position, payment));
//
//        return convertView;
//    }
//
//    private void showEditPaymentDialog(int position, Double oldPayment) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Изменить платёж");
//        builder.setMessage("Введите новую сумму:");
//
//        final EditText input = new EditText(context);
//        input.setHint("Сумма");
//        input.setText(String.valueOf(oldPayment));
//        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
//                android.text.InputType.TYPE_NUMBER_FLAG_SIGNED |
//                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
//        builder.setView(input);
//
//        builder.setPositiveButton("Сохранить", (dialog, which) -> {
//            String valueStr = input.getText().toString().trim();
//            if (!valueStr.isEmpty()) {
//                try {
//                    double newPayment = Double.parseDouble(valueStr);
//
//                    // Здесь нужно обновить платеж в БД
//                    // Для этого нужно хранить ID платежа
//                    // Пока просто обновляем в списке
//                    payments.set(position, newPayment);
//                    expense.setExpenseList(payments);
//
//                    // Обновляем в БД (нужно добавить метод в ExpenseService)
//                    if (expenseService.updatePayment(expense, position, newPayment)) {
//                        Toast.makeText(context, "Платёж изменён", Toast.LENGTH_SHORT).show();
//                        listener.onPaymentChanged(); // Обновляем общую сумму
//                        notifyDataSetChanged();
//                    } else {
//                        Toast.makeText(context, "Ошибка при изменении", Toast.LENGTH_SHORT).show();
//                    }
//                } catch (NumberFormatException e) {
//                    Toast.makeText(context, "Введите корректное число", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        builder.setNegativeButton("Отмена", null);
//        builder.show();
//    }
//
//    private void showDeleteConfirmationDialog(int position, Double payment) {
//        new AlertDialog.Builder(context)
//                .setTitle("Удаление платежа")
//                .setMessage(String.format("Удалить платёж %.2f руб.?", payment))
//                .setPositiveButton("Удалить", (dialog, which) -> {
//                    // Удаляем из БД (нужно добавить метод в ExpenseService)
//                    if (expenseService.deletePayment(expense, position)) {
//                        payments.remove(position);
//                        Toast.makeText(context, "Платёж удалён", Toast.LENGTH_SHORT).show();
//                        listener.onPaymentChanged(); // Обновляем общую сумму
//                        notifyDataSetChanged();
//                    } else {
//                        Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Отмена", null)
//                .show();
//    }
//}