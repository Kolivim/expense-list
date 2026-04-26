package com.example.test3.monthly.expense.refund.planning;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.DepositDetailActivity;
import com.example.test3.R;
import com.example.test3.deposit.Deposit;
import com.example.test3.deposit.DepositAddActivity;
import com.example.test3.util.Util;

import java.time.YearMonth;
import java.util.List;

public class PlannedDepositAdapter extends RecyclerView.Adapter<PlannedDepositAdapter.ViewHolder> {

    private static final String TAG = "PlannedDepositAdapter";
    private List<Deposit> deposits;
    /** Внесённые Deposit'ы, необходимы для определения выплачен ли полностью PlannedDeposit (сопоставляются по дате) */
    private List<Deposit> paidDeposits;
    private Context context;

//    private OnAddActualDepositListener addListener;
//    @Deprecated
//    public PlannedDepositAdapter(Context context, List<Deposit> deposits, OnAddActualDepositListener listener) {
//        this.context = context;
//        this.deposits = deposits;
//        this.addListener = listener;
//    }
//    public interface OnAddActualDepositListener {
//        void onAddActualDeposit(Deposit plannedDeposit);
//    }


    public PlannedDepositAdapter(Context context, List<Deposit> deposits, List<Deposit> paidDeposits) {
        this.context = context;
        this.deposits = deposits;
        this.paidDeposits = paidDeposits;
        Log.d(TAG, "Constructor called, deposits size=" + (deposits == null ? 0 : deposits.size()));
    }

    @Deprecated
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

//        /** 1 */ view.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());

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


        /** Кнопка (+) – добавить фактический взнос для этого plannedDeposit : */
//        /*
        holder.buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(context, DepositAddActivity.class);
            intent.putExtra(DepositAddActivity.EXTRA_PARENT_ID, deposit.getExpenseId());
            intent.putExtra(DepositAddActivity.EXTRA_DEFAULT_NAME, "Внесёно в счёт " + deposit.getName());
            intent.putExtra(DepositAddActivity.EXTRA_DEFAULT_DATE, deposit.getDateTime().format(Util.dateFormatterSee));
            context.startActivity(intent);
        });
//        */
//        holder.buttonAdd.setOnClickListener(v -> {if (addListener != null) {addListener.onAddActualDeposit(deposit);}});
        /** !Кнопка (+) – добавить фактический взнос для этого plannedDeposit */


        /** Меняет фон, при условии полного погашения PlannedDeposit : */
        Drawable originalBg = holder.itemView.getBackground();
//        Drawable original = ContextCompat.getDrawable(context, R.drawable.rounded_edittext_bg);
//        Drawable wrapped = original.mutate();
        if (isPlannedDepositPaid(deposit)) {

//            Drawable wrapped = DrawableCompat.wrap(originalBg.mutate());
//            DrawableCompat.setTint(wrapped, ContextCompat.getColor(context, R.color.mint));
//            holder.itemView.setBackground(wrapped);

//            GradientDrawable original = (GradientDrawable) originalBg;
//            GradientDrawable newBg = (GradientDrawable) original.getConstantState().newDrawable().mutate();
//            newBg.setColor(ContextCompat.getColor(context, R.color.mint));
//            holder.itemView.setBackground(newBg);

            ViewCompat.setBackgroundTintList(holder.itemView, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mint)));

        } else {
            /** Восстанавливает исходный фон */
            holder.itemView.setBackground(originalBg);
        }
        /** !Меняет фон, при условии полного погашения PlannedDeposit */

        Log.d(TAG, "onBindViewHolder end");
    }


    @Override
    public int getItemCount() {
        return deposits == null ? 0 : deposits.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textDate, textAmount;
        Button buttonAdd;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDepositDate);
            textAmount = itemView.findViewById(R.id.textDepositAmount);
            buttonAdd = itemView.findViewById(R.id.buttonAddActualDeposit);
        }

    }


    public boolean isPlannedDepositPaid(Deposit plannedDeposit) {
        Log.d(TAG, "isPlannedDepositPaid() startMethod, plannedDeposit: " + plannedDeposit);

        boolean isPaid = false;

        for (Deposit paidDeposit : paidDeposits) {

            boolean isThisPaid = YearMonth.from(plannedDeposit.getDateTime()).equals(YearMonth.from(paidDeposit.getDateTime()));

            if(isThisPaid && plannedDeposit.getTotalAmount() <= paidDeposit.getTotalAmount()) {
                isPaid = isThisPaid;
            }

        }

        Log.d(TAG, "isPlannedDepositPaid() endMethod, для plannedDeposit: " + plannedDeposit);
        return isPaid;
    }

}