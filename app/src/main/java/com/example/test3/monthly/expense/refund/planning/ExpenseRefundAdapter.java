package com.example.test3.monthly.expense.refund.planning;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;
import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.ExpenseDetailWithDeleteActivity;
import com.example.test3.expenseList.ExpenseRefundDetailActivity;

import java.util.List;

public class ExpenseRefundAdapter extends RecyclerView.Adapter<ExpenseRefundAdapter.ViewHolder> {

    private static final String TAG = "ExpenseRefundAdapter";
    private List<ExpenseRefund> refunds;
    private Context context;
    private int spanCount = 2; // количество столбцов для взносов


    public ExpenseRefundAdapter(Context context, List<ExpenseRefund> refunds) {
        this.context = context;
        this.refunds = refunds;
        Log.d(TAG, "Constructor called, refunds size=" + (refunds == null ? 0 : refunds.size()));
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder start");
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_expense_refund, parent, false);
        Log.d(TAG, "onCreateViewHolder end");
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder start position=" + position);

        ExpenseRefund refund = refunds.get(position);
        holder.textName.setText(refund.getDescription() == null ?
                refund.getName() : refund.getName() + "(" + refund.getDescription() + ")");

        /** Общая сумма запланированных взносов */
        double totalPlanned = 0;
        if (refund.getDepositList() != null) {
            for (com.example.test3.deposit.Deposit deposit : refund.getDepositList()) {
                totalPlanned += deposit.getTotalAmount();
            }
        }
        holder.textTotalPlanned.setText(String.format("Запланировано взносов: %.2f руб.", totalPlanned));


        /** Детали расхода */
        StringBuilder details = new StringBuilder();
        if (refund.getMonthCount() != null && refund.getMonthCount() > 0) {
            details.append("Срок: ").append(refund.getMonthCount()).append(" мес. ");
        }
        if (refund.getStartDate() != null) {
            details.append("Начало: ").append(refund.getStartDate().format(com.example.test3.util.Util.dateFormatterSee));
        }
        double totalExpense = refund.getExpenseListTotalAmount();
        details.append("\nСумма к возврату: ").append(String.format("%.2f", totalExpense)).append(" руб.");
        holder.textDetails.setText(details.toString());


        /** Выделение цветом, если возврат полностью выплачен */
        Drawable originalBg = holder.itemView.getBackground();
        if (refund.isRefunded()) {
            Drawable wrapped = DrawableCompat.wrap(originalBg.mutate());
            DrawableCompat.setTint(wrapped, ContextCompat.getColor(context, R.color.refunded_color));
            holder.itemView.setBackground(wrapped);
        } else {
            holder.itemView.setBackground(originalBg);
        }
        /*
        if (refund.isRefunded()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.refunded_color));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }
        */


        /** Настройка сетки для запланированных взносов : */
        RecyclerView depositsRecycler = holder.recyclerDeposits;
        List<Deposit> plannedDeposits = refund.getPlannedDepositList();

        if (plannedDeposits != null && !plannedDeposits.isEmpty()) {

            /*
            GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
            depositsRecycler.setLayoutManager(layoutManager);
            */
//            /*
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            depositsRecycler.setLayoutManager(layoutManager);
//            */

            PlannedDepositAdapter depositAdapter = new PlannedDepositAdapter(context, plannedDeposits);
            depositsRecycler.setAdapter(depositAdapter);
            depositsRecycler.setVisibility(View.VISIBLE);

            depositsRecycler.setNestedScrollingEnabled(false); /** 1 */
        } else {
            depositsRecycler.setVisibility(View.GONE);
        }
        /** !Настройка сетки для запланированных взносов */


        // Настройка сетки для :
//        if (refund.getDepositList() != null && !refund.getDepositList().isEmpty()) {
//            holder.recyclerDeposits.setVisibility(View.VISIBLE);
//            GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
//            holder.recyclerDeposits.setLayoutManager(layoutManager);
//            PlannedDepositAdapter depositAdapter = new PlannedDepositAdapter(context, refund.getDepositList());
//            holder.recyclerDeposits.setAdapter(depositAdapter);
//        } else {
//            holder.recyclerDeposits.setVisibility(View.GONE);
//        }
        // !Настройка сетки для взносов


        /** Клик по элементу – открыть детали расхода */
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, /*ExpenseDetailWithDeleteActivity*/ ExpenseRefundDetailActivity.class);
            intent.putExtra("expense_object", refund); // intent.putExtra("expense_id", refund.getId());
            context.startActivity(intent);
        });

        Log.d(TAG, "onBindViewHolder end");
    }


    @Override
    public int getItemCount() {return refunds == null ? 0 : refunds.size();}


    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textTotalPlanned, textDetails;
        RecyclerView recyclerDeposits;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textTotalPlanned = itemView.findViewById(R.id.textTotalPlanned);
            textDetails = itemView.findViewById(R.id.textDetails);
            recyclerDeposits = itemView.findViewById(R.id.recyclerPlannedDeposits);
        }

    }


}