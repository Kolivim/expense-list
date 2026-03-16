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
import com.example.test3.deposit.Deposit;
import com.example.test3.service.DepositService;

import java.util.List;

public class DepositPaymentAdapter extends ArrayAdapter<Double> {

    private Context context;
    private List<Double> payments;
    private Deposit deposit;
    private DepositService depositService;
    private OnPaymentChangedListener listener;

    public interface OnPaymentChangedListener { void onPaymentChanged();}


    public DepositPaymentAdapter(Context context, List<Double> payments,
                                 Deposit deposit, DepositService depositService,
                                 OnPaymentChangedListener listener) {
        super(context, R.layout.list_item_payment, payments);
        this.context = context;
        this.payments = payments;
        this.deposit = deposit;
        this.depositService = depositService;
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

                    if (depositService.updateDepositPayment(deposit, position, newPayment)) {
                        Toast.makeText(context, "Платёж изменён", Toast.LENGTH_SHORT).show();
                        listener.onPaymentChanged(); // Обновит общую сумму
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

                    if (depositService.deleteDepositPayment(deposit, position)) {
                        Toast.makeText(context, "Платёж удалён", Toast.LENGTH_SHORT).show();
                        listener.onPaymentChanged();                                                /** Обновляет общую сумму */
                    } else {
                        Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Отмена", null)
                .show();

    }


}