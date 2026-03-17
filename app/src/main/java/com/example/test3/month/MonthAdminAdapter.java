package com.example.test3.month;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.test3.R;

import java.text.DecimalFormat;
import java.util.List;

public class MonthAdminAdapter extends ArrayAdapter<MonthlyDto> {

    private static final String TAG = "MonthAdminAdapter";

    private Context context;
    private List<MonthlyDto> monthlyDtos;
    private OnItemClickListener listener;
    private int selectedPosition = -1;


    public interface OnItemClickListener { void onItemClick(MonthlyDto dto, int position);}


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }


    public MonthAdminAdapter(Context context, List<MonthlyDto> monthlyDtos) {
        super(context, R.layout.list_item_month_admin, monthlyDtos);
        this.context = context;
        this.monthlyDtos = monthlyDtos;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_month_admin, parent, false);
        }

        MonthlyDto dto = monthlyDtos.get(position);
        DecimalFormat df = new DecimalFormat("#,##0.00");

        TextView textViewMonthName = convertView.findViewById(R.id.textViewMonthName);
        TextView textViewMonthBalance = convertView.findViewById(R.id.textViewMonthBalance);
        TextView textViewExpenseStats = convertView.findViewById(R.id.textViewExpenseStats);
        TextView textViewDepositStats = convertView.findViewById(R.id.textViewDepositStats);

        TextView textViewPlannedReturn = convertView.findViewById(R.id.textViewPlannedReturn);
        TextView textViewPlannedBalance = convertView.findViewById(R.id.textViewPlannedBalance);


        /** Название месяца */
        if (dto.getMonth() != null) {
            textViewMonthName.setText(dto.getMonth().getMonthYear());
        } else {
            textViewMonthName.setText("Неизвестный месяц");
        }


        /** Баланс */
        double balance = dto.getBalance();
        textViewMonthBalance.setText(df.format(balance) + " руб.");

        if (balance > 0) {
            textViewMonthBalance.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else if (balance < 0) {
            textViewMonthBalance.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else {
            textViewMonthBalance.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        }


        /** Статистика по расходам */
        String expenseText = String.format("Расходы: %s руб. (%d шт., %d платежей)",
                df.format(dto.getTotalExpenseAmount()),
                dto.getExpensesCount(),
                dto.getPaymentsCount());
        textViewExpenseStats.setText(expenseText);


        /** Статистика по взносам */
        String depositText = String.format("Взносы: %s руб. (%d шт.)",
                df.format(dto.getTotalDepositAmount()),
                dto.getDepositsCount());
        textViewDepositStats.setText(depositText);


        double plannedReturn = dto.getPlannedReturnAmount();
        double plannedBalance = dto.getPlannedBalance();

        if (plannedReturn != 0) {

            /** Если есть планируемый возврат, показываем информацию */
            textViewPlannedReturn.setText("План: " + df.format(plannedReturn) + " руб.");
            textViewPlannedReturn.setVisibility(View.VISIBLE);

            textViewPlannedBalance.setText("Остаток от плана: " + df.format(plannedBalance) + " руб.");
            textViewPlannedBalance.setVisibility(View.VISIBLE);

            if (plannedBalance > 0) {
                textViewPlannedBalance.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else if (plannedBalance < 0) {
                textViewPlannedBalance.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            } else {
                textViewPlannedBalance.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            }

        } else {

            /** Если планируемого возврата нет, скрываем строку */
            textViewPlannedReturn.setVisibility(View.GONE);
            textViewPlannedBalance.setVisibility(View.GONE);

        }


        Log.d(TAG, "Displaying month: " + dto.getMonth().getMonthYear() +
                ", plannedReturn=" + plannedReturn + ", plannedBalance=" + plannedBalance);


        /** Выделение выбранной строки */
        if (position == selectedPosition) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_color));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }


        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(dto, position);
            }
        });

        return convertView;
    }


}