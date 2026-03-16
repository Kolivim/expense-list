package com.example.test3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.test3.deposit.Deposit;

import java.util.List;

public class DepositListAdapter extends ArrayAdapter<Deposit> {

    private Context context;
    private List<Deposit> deposits;
    private OnDepositActionListener listener;


    public interface OnDepositActionListener {
        void onEditClick(Deposit deposit);
        void onDeleteClick(Deposit deposit);
    }


    public DepositListAdapter(Context context, List<Deposit> deposits, OnDepositActionListener listener) {
        super(context, 0, deposits);
        this.context = context;
        this.deposits = deposits;
        this.listener = listener;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_deposit_with_buttons, parent, false);
        }

        Deposit deposit = deposits.get(position);

        TextView textViewName = convertView.findViewById(R.id.textViewDepositName);
        TextView textViewAmount = convertView.findViewById(R.id.textViewDepositAmount);
        TextView textViewPayments = convertView.findViewById(R.id.textViewPaymentsCount);
        Button buttonEdit = convertView.findViewById(R.id.buttonEditDeposit);
        Button buttonDelete = convertView.findViewById(R.id.buttonDeleteDeposit);

        /** Название */
        textViewName.setText(deposit.getName());

        /** Сумма */
        textViewAmount.setText(String.format("%.2f руб.", deposit.getTotalAmount()));

        /** Количество платежей */
        int paymentsCount = deposit.getPayments() != null ? deposit.getPayments().size() : 0;
        textViewPayments.setText("Платежей: " + paymentsCount);

        /** Кнопки */
        buttonEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(deposit);
        });

        buttonDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(deposit);
        });

        return convertView;
    }


}