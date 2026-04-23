package com.example.test3.monthly.expense.refund.planning;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.DepositDetailActivity;
import com.example.test3.R;
import com.example.test3.deposit.Deposit;
import com.example.test3.util.Util;

import java.util.List;

public class PlannedDepositAdapter extends RecyclerView.Adapter<PlannedDepositAdapter.ViewHolder> {

    private static final String TAG = "PlannedDepositAdapter";
    private List<Deposit> deposits;
    private Context context;


    public PlannedDepositAdapter(Context context, List<Deposit> deposits) {
        this.context = context;
        this.deposits = deposits;
        Log.d(TAG, "Constructor called, deposits size=" + (deposits == null ? 0 : deposits.size()));
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder start");
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_planned_deposit, parent, false);

        /** 1 */ view.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());

        Log.d(TAG, "onCreateViewHolder end");
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder start position=" + position);

        Deposit deposit = deposits.get(position);
        holder.textDate.setText(deposit.getDateTime().format(Util.dateFormatterMonthYearSee));
        String textAmountString = context.getString(R.string.low_amount, deposit.getTotalAmount());
        holder.textAmount.setText(textAmountString);
//        holder.textAmount.setText(String.format("%.2f руб.", deposit.getTotalAmount()));

        /** При клике на Deposit открываем его детали */
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DepositDetailActivity.class);
            intent.putExtra("deposit_id", deposit.getId());
            context.startActivity(intent);
        });

        Log.d(TAG, "onBindViewHolder end");
    }


    @Override
    public int getItemCount() {
        return deposits == null ? 0 : deposits.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textDate, textAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDepositDate);
            textAmount = itemView.findViewById(R.id.textDepositAmount);
        }

    }


}