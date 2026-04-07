package com.example.test3.deposit;

import static android.provider.Settings.System.getString;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.test3.DepositDetailActivity;
import com.example.test3.R;
import com.example.test3.expenseList.Expense;
import com.example.test3.service.DepositService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DepositAdapter extends ArrayAdapter<Deposit> {

    private static final String TAG = "DepositAdapter";

    private final Context context;
    private final List<Deposit> deposits;
    private final DepositService depositService;
    private final OnDepositDeletedListener onDepositDeletedListener;


    public interface OnDepositDeletedListener {
        void onDepositDeleted();
    }


    public DepositAdapter(Context context, List<Deposit> deposits,
                          DepositService depositService,
                          OnDepositDeletedListener listener) {

//        super(context, android.R.layout.simple_list_item_2, deposits);
        super(context, R.layout.list_item_deposit, deposits);
        this.context = context;
        this.deposits = deposits;
        this.depositService = depositService;
        this.onDepositDeletedListener = listener;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView startMethod");

        if (convertView == null) {
//            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_deposit, parent, false);
        }

        Deposit deposit = getItem(position);


        TextView textViewDepositName = convertView.findViewById(R.id.textViewDepositName);
        TextView textViewDepositDate = convertView.findViewById(R.id.textViewDepositDate);
        TextView textViewDepositAmount = convertView.findViewById(R.id.textViewDepositAmount);
        TextView textViewPaymentsCount = convertView.findViewById(R.id.textViewPaymentsCount);


        double amount = deposit.getTotalAmount();                                                   //  String amountStr = String.format("%.2f руб.", deposit.getTotalAmount());
        String dateStr = deposit.getDateTime() != null ? deposit.getDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yy")) : "без даты";
        String desc = deposit.getDescription() != null && !deposit.getDescription().isEmpty() ? " (" + deposit.getDescription() + ")" : "";
        int paymentsCount = deposit.getPayments() == null ? 0 : deposit.getPayments().size();

        textViewDepositName.setText(deposit.getName() + desc);
        textViewDepositDate.setText(dateStr);
        textViewDepositAmount.setText(context.getString(R.string.amount, amount));                  //  textViewDepositAmount.setText(amountStr);
        textViewPaymentsCount.setText(context.getString(R.string.payments_count, paymentsCount));   //  textViewPaymentsCount.setText(deposit.getPayments() == null ? "0" : String.valueOf(deposit.getPayments().size()));


        /** Установка цвета текста : */
        if (deposit.getRowColor() != null && deposit.getRowColor() != -1) {

            textViewDepositName.setTextColor(deposit.getRowColor());
            textViewDepositDate.setTextColor(deposit.getRowColor());
            textViewDepositAmount.setTextColor(deposit.getRowColor());
            textViewPaymentsCount.setTextColor(deposit.getRowColor());

        } else {

            textViewDepositName.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            textViewDepositDate.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            textViewDepositAmount.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            textViewPaymentsCount.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));

        }
        /** !Установка цвета текста */


        /** Обработчик длинного нажатия */
        convertView.setOnLongClickListener(v -> {

            /** Диалог удаления */
            startDeleteDialog(deposit);
            return true;

        });


        /** Обработчик короткого нажатия */
        convertView.setOnClickListener(v -> {

            Intent intent = new Intent(context, DepositDetailActivity.class);
            intent.putExtra("deposit_id", deposit.getId());
            context.startActivity(intent);

        });


        Log.d(TAG, "getView endMethod");
        return convertView;
    }


    public void startDeleteDialog(Deposit deposit) {
        Log.d(TAG, "getView startMethod, deposit: " + deposit);

        /** Диалог удаления */
        new AlertDialog.Builder(context)
                .setTitle("Удалить взнос")
                .setMessage("Удалить взнос на " + String.format("%.2f", deposit.getTotalAmount()) + " руб.?")
                .setPositiveButton("Да", (dialog, which) -> {

                    if (depositService.deleteDeposit(deposit.getId())) {
                        deposits.remove(deposit);
                        notifyDataSetChanged();
                        if (onDepositDeletedListener != null) {
                            onDepositDeletedListener.onDepositDeleted();
                        }
                        Toast.makeText(context, "Взнос удалён", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Нет", null)
                .show();

        Log.d(TAG, "getView endMethod, для deposit: " + deposit);
    }


}