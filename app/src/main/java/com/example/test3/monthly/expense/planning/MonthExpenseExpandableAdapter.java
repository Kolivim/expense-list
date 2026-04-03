package com.example.test3.monthly.expense.planning;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.test3.ExpenseDetailActivity;
import com.example.test3.R;
import com.example.test3.expenseList.Expense;

import java.util.List;

public class MonthExpenseExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<MonthlyExpensePlanningDto> groups;
    private LayoutInflater inflater;

    /** Для подсветки заголовка у выбранного месяца */
    private int selectedGroupPosition = -1;

    private OnAddExpenseClickListener addExpenseListener;


    public interface OnAddExpenseClickListener {
        void onAddExpense(MonthlyExpensePlanningDto dto);
    }


    public void setOnAddExpenseClickListener(OnAddExpenseClickListener listener) {
        this.addExpenseListener = listener;
    }


    private OnGroupClickListener groupClickListener;


    public interface OnGroupClickListener {
        void onGroupClick(int groupPosition);
    }


    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.groupClickListener = listener;
    }


    public MonthExpenseExpandableAdapter(Context context, List<MonthlyExpensePlanningDto> groups) {
        this.context = context;
        this.groups = groups;
        this.inflater = LayoutInflater.from(context);
    }


    public void setSelectedGroupPosition(int position) {
        this.selectedGroupPosition = position;
        notifyDataSetChanged();                                                                     /** Перерисовка */
    }


    @Override
    public int getGroupCount() {
        return groups.size();
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        List<Expense> children = groups.get(groupPosition).getExpenseList();
        return children == null ? 0 : children.size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).getExpenseList().get(childPosition);
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groups.get(groupPosition).getMonth().getId();
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groups.get(groupPosition).getExpenseList().get(childPosition).getId();
    }


    @Override
    public boolean hasStableIds() {
        return true;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Log.d("getGroupView", "startMethod");

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_group_month, parent, false);
        }

        MonthlyExpensePlanningDto dto = groups.get(groupPosition);

        TextView textViewMonthName = convertView.findViewById(R.id.textViewMonthName);
        TextView textViewExpenseStats = convertView.findViewById(R.id.textViewExpenseStats);
        TextView textViewDepositStats = convertView.findViewById(R.id.textViewDepositStats);
        TextView textViewBalance = convertView.findViewById(R.id.textViewBalance);

        textViewMonthName.setText(dto.getMonth().getMonthYear());

        String stats = String.format("Расходы: %.2f руб. (%d шт., %d платежей)",
                dto.getTotalExpenseAmount(),
                dto.getExpensesCount(),
                dto.getPaymentsCount());
        textViewExpenseStats.setText(stats);
        /***/


        /** Устанавливаем статистику по взносам : */
        String depositStats = String.format("Внесено: %.2f руб. (%d шт.)",
                dto.getTotalDepositAmount(),
                dto.getDepositsCount());
//                dto.getDepositsPayments);                                                         /** Не реализован сбор статистики по Deposit Payments */
        textViewDepositStats.setText(depositStats);
        /***/


        /** Устанавливаем balance : */
        String balance = String.format("Итог: %.2f руб.", dto.getBalance());
        textViewBalance.setText(balance);
        /***/


        // Также можно установить цвет текста для баланса
        /** Устанавливает фон для выделения */
        if (groupPosition == selectedGroupPosition) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_color));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }


        /** Кнопка добавить Expense для месяца */
        Button buttonAddExpense = convertView.findViewById(R.id.buttonAddExpense);
        buttonAddExpense.setOnClickListener(v -> {
            if (addExpenseListener != null) {
                addExpenseListener.onAddExpense(groups.get(groupPosition));
            }
        });


        convertView.setOnClickListener(v -> {
            if (groupClickListener != null) {
                groupClickListener.onGroupClick(groupPosition);
            }
        });


        Log.d("getGroupView", "endMethod");
        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Log.d("getChildView", "startMethod");

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_child_expense, parent, false);
        }

        Expense expense = (Expense) getChild(groupPosition, childPosition);

        TextView textViewInfo = convertView.findViewById(R.id.textViewExpenseInfo);
        TextView textViewDate = convertView.findViewById(R.id.textViewExpenseDate);

        String expenseText = expense.getName();
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            expenseText += " (" + expense.getDescription() + ")";
        }
        if (expense.getExpenseList() != null && !expense.getExpenseList().isEmpty()) {
            expenseText += "\nСумма: " + String.format("%.2f", expense.getExpenseListTotalAmount()) +
                    " руб. | Платежей: " + expense.getExpenseList().size();
        } else {
            expenseText += "\nНет платежей";
        }

        textViewInfo.setText(expenseText);
        textViewDate.setText(expense.getDateTimeString());

        if (expense.getRowColor() != null && expense.getRowColor() != -1) {
            textViewInfo.setTextColor(expense.getRowColor());
        } else {
            textViewInfo.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExpenseDetailActivity.class);
            intent.putExtra("expense_id", expense.getId());
            context.startActivity(intent);
        });


        Log.d("getChildView", "endMethod");
        return convertView;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;                                                                                /** дочерние элементы кликабельны */
    }


}