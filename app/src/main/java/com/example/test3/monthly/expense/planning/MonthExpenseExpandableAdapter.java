package com.example.test3.monthly.expense.planning;

import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_CONTRIBUTION;
import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_PLANNING;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.ExpenseDetailWithDeleteActivity;
import com.example.test3.R;
import com.example.test3.expenseList.Expense;

import java.util.List;

public class MonthExpenseExpandableAdapter extends BaseExpandableListAdapter {

    private List<MonthlyExpensePlanningDto> groups;
    private Long depositType;

    private Context context;
    private LayoutInflater inflater;
    private int selectedGroupPosition = -1;                                                         /** Для подсветки заголовка у выбранного месяца */

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


    public MonthExpenseExpandableAdapter(Context context, List<MonthlyExpensePlanningDto> groups, Long depositType) {
        this.context = context;
        this.groups = groups;
        this.depositType = depositType;
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

        String expenseStats = context.getString(R.string.total_month_expense_amount,
                dto.getTotalExpenseAmount(), dto.getExpensesCount(), dto.getPaymentsCount());
        textViewExpenseStats.setText( /* stats */ expenseStats);
        /***/


        /** Устанавливаем статистику по взносам : (Не реализован сбор статистики по Deposit Payments) */
        String depositStats = context.getString(R.string.total_month_deposit_amount, dto.getTotalDepositAmount());
        textViewDepositStats.setText(depositStats);
        /***/


        /** Устанавливаем balance : */
        String balance = context.getString(R.string.balance_2, dto.getBalance());
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
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        Log.d("getChildView", "startMethod");

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_child_expense, parent, false);
        }

        Expense expense = (Expense) getChild(groupPosition, childPosition);

        TextView textViewInfo = convertView.findViewById(R.id.textViewExpenseInfo);
        TextView textViewDate = convertView.findViewById(R.id.textViewExpenseDate);
        TextView textViewExpenseAmount = convertView.findViewById(R.id.textViewExpenseAmount);

        String expenseText = expense.getName();
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            expenseText += " (" + expense.getDescription() + ")";
        }

        textViewInfo.setText(expenseText);
        textViewDate.setText(expense.getDateTimeString());
        textViewExpenseAmount.setText(context.getString(R.string.amount, expense.getExpenseListTotalAmount()));


        /** Устанавливает цвет : */
        if (expense.getRowColor() != null && expense.getRowColor() != -1) {
            textViewInfo.setTextColor(expense.getRowColor());
        } else {
            textViewInfo.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }
        /** !Устанавливает цвет */


        View depositContainer = convertView.findViewById(R.id.depositContainer);
        TextView textViewDepositAmount = convertView.findViewById(R.id.textViewDepositAmount);

        TextView textViewBalance = convertView.findViewById(R.id.textViewBalance);


        /** Управляет видимостью контейнера с Deposit и поля Balance : */
        if (expense.getDepositList() == null || expense.getDepositList().isEmpty()) {

            depositContainer.setVisibility(View.GONE);
            textViewBalance.setVisibility(View.GONE);

        } else {

            depositContainer.setVisibility(View.VISIBLE);
            textViewBalance.setVisibility(View.VISIBLE);

            double depositTotalAmount = expense.getDepositListTotalAmount();
            textViewDepositAmount.setText(context.getString(R.string.total_deposit_amount, depositTotalAmount));

            double balance = expense.getBalance();
            textViewBalance.setText(context.getString(R.string.balance, balance));

        }
        /***/


        /** Вызывает Activity для редактирования Expense */
        convertView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ExpenseDetailActivity.class);
//            Intent intent = new Intent(context, ExpenseDeleteActivity.class);
            Intent intent = new Intent(context, ExpenseDetailWithDeleteActivity.class);
            intent.putExtra("expense_id", expense.getId());
            context.startActivity(intent);
        });


        /** Вызывает Activity для редактирования списка Deposit, относящихся к Expense */
        Button depositButton = convertView.findViewById(R.id.deposit);
        if(depositType == TYPE_DEPOSIT_MONTH_CONTRIBUTION) depositButton.setText("Потрачено");
        depositButton.setOnClickListener(v -> {
            Log.d("depositButton.setOnClickListener", "pushDepositButton");



//            if (depositClickListener != null) {
//                depositClickListener.onDepositClick(expense);

                Intent intent = new Intent(context, UniversalDepositsActivity.class);
                intent.putExtra(UniversalDepositsActivity.EXTRA_PARENT_ID, expense.getId());
                intent.putExtra(UniversalDepositsActivity.EXTRA_PARENT_TYPE, UniversalDepositsActivity.TYPE_EXPENSE);
                intent.putExtra(UniversalDepositsActivity.EXTRA_TITLE, "Взносы: " + expense.getName());
                intent.putExtra(UniversalDepositsActivity.EXTRA_DEPOSIT_TYPE_ID, depositType);      /** TYPE_DEPOSIT_MONTH_PLANNING */
                context.startActivity(intent);

//            }

        });


        Log.d("getChildView", "endMethod");
        return convertView;
    }


    /** Обработчик нажатий кнопки Взносы : */
//    public interface OnDepositClickListener {
//        void onDepositClick(Expense expense);
//    }
//
//    private OnDepositClickListener depositClickListener;
//
//    public void setOnDepositClickListener(OnDepositClickListener listener) {
//        this.depositClickListener = listener;
//    }
    /** !Обработчик нажатий кнопки Взносы */


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;                                                                                /** дочерние элементы кликабельны */
    }


}