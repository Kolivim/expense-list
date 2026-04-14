package com.example.test3.monthly.expense.utility.service;

import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_PLANNING;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.test3.R;
import com.example.test3.expenseList.Expense;
import com.example.test3.expenseList.ExpenseDetailWithDeleteActivity;
import com.example.test3.monthly.expense.planning.UniversalDepositsActivity;
import com.example.test3.service.DepositService;
import com.example.test3.service.ExpenseService;

import java.util.List;

public class ExpenseInMonthAdapter extends ArrayAdapter<Expense> {
    private final Context context;
    private final List<Expense> expenses;
//    private final ExpenseService expenseService;
//    private final DepositService depositService; // если нужен

    public ExpenseInMonthAdapter(Context context, List<Expense> expenses/*,
                                 ExpenseService expenseService, DepositService depositService*/) {
        super(context, R.layout.list_child_expense, expenses);
        this.context = context;
        this.expenses = expenses;
//        this.expenseService = expenseService;
//        this.depositService = depositService;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_child_expense, parent, false);
        }

        Expense expense = expenses.get(position);


        TextView textViewInfo = convertView.findViewById(R.id.textViewExpenseInfo);
        TextView textViewDate = convertView.findViewById(R.id.textViewExpenseDate);
        TextView textViewExpenseAmount = convertView.findViewById(R.id.textViewExpenseAmount);

        String expenseText = expense.getName();
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            expenseText += " (" + expense.getDescription() + ")";
        }
        /*
        if (expense.getExpenseList() != null && !expense.getExpenseList().isEmpty()) {
            expenseText += "\nСумма: " + String.format("%.2f", expense.getExpenseListTotalAmount()) +
                    " руб. | Платежей: " + expense.getExpenseList().size();
        } else {
            expenseText += "\nНет платежей";
        }
        */


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
//        TextView textViewDepositName = convertView.findViewById(R.id.textViewDepositName);
//        TextView textViewDepositDate = convertView.findViewById(R.id.textViewDepositDate);
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

            double balance = expense.getBalance();   /* expense.getExpenseListTotalAmount() - expense.getDepositListTotalAmount(); */
            textViewBalance.setText(context.getString(R.string.balance, balance));


            /*
            textViewDepositInfo
            textViewDepositAmount
            textViewDepositDate
            textViewBalance

                    double amount = deposit.getTotalAmount();                                                   //  String amountStr = String.format("%.2f руб.", deposit.getTotalAmount());
                    String dateStr = deposit.getDateTime() != null ? deposit.getDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yy")) : "без даты";
                    String desc = deposit.getDescription() != null && !deposit.getDescription().isEmpty() ? " (" + deposit.getDescription() + ")" : "";
                    int paymentsCount = deposit.getPayments() == null ? 0 : deposit.getPayments().size();

                    textViewDepositName.setText(deposit.getName() + desc);
                    textViewDepositDate.setText(dateStr);
                    textViewDepositAmount.setText(context.getString(R.string.amount, amount));                  //  textViewDepositAmount.setText(amountStr);
                    textViewPaymentsCount.setText(context.getString(R.string.payments_count, paymentsCount));   //  textViewPaymentsCount.setText(deposit.getPayments() == null ? "0" : String.valueOf(deposit.getPayments().size()));
            */

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
        depositButton.setOnClickListener(v -> {
            Log.d("depositButton.setOnClickListener", "pushDepositButton");



//            if (depositClickListener != null) {
//                depositClickListener.onDepositClick(expense);

            Intent intent = new Intent(context, UniversalDepositsActivity.class);
            intent.putExtra(UniversalDepositsActivity.EXTRA_PARENT_ID, expense.getId());
            intent.putExtra(UniversalDepositsActivity.EXTRA_PARENT_TYPE, UniversalDepositsActivity.TYPE_EXPENSE);
            intent.putExtra(UniversalDepositsActivity.EXTRA_TITLE, "Взносы: " + expense.getName());
            intent.putExtra(UniversalDepositsActivity.EXTRA_DEPOSIT_TYPE_ID, TYPE_DEPOSIT_MONTH_PLANNING);
            context.startActivity(intent);

//            }

        });


        return convertView;
    }


    @Override
    public int getCount() {
        return expenses.size();
    }


    /*
    public interface OnPaymentAddListener {
        void onAddPayment(Expense expense, double amount);
    }
     */


}