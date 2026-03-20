package com.example.test3;

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

import androidx.core.content.ContextCompat;

import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.Expense;
import com.example.test3.service.DepositService;
import com.example.test3.service.ExpenseService;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class LongLoanAdapter extends ArrayAdapter<Expense> {

    private Context context;
    private ArrayList<Expense> loansList;
    private ExpenseService expenseService;
    private DepositService depositService;

    private OnItemClickListener listener;
    private OnRepayClickListener repayListener;

    /** Выделение выбранной строки */
    private int selectedPosition = -1;

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener { void onItemClick(Expense expense, int position);}
    public interface OnRepayClickListener { void onRepayClick(Expense expense);}

    public void setOnItemClickListener(OnItemClickListener listener) {this.listener = listener;}
    public void setOnRepayClickListener(OnRepayClickListener listener) {this.repayListener = listener;}

    public LongLoanAdapter(Context context, ArrayList<Expense> loansList,
                           ExpenseService expenseService, DepositService depositService) {
        super(context, R.layout.list_item_long_loan, loansList);
        this.context = context;
        this.loansList = loansList;
        this.expenseService = expenseService;
        this.depositService = depositService;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_long_loan, parent, false);
        }

        Expense loan = loansList.get(position);

        TextView textViewInfo = convertView.findViewById(R.id.textViewExpenseInfo);
        TextView textViewTotalRepaid = convertView.findViewById(R.id.textViewTotalRepaid);
        TextView textViewPaymentsCount = convertView.findViewById(R.id.textViewPaymentsCount);
        TextView textViewRemainingDebt = convertView.findViewById(R.id.textViewRemainingDebt);
        Button buttonAdd = convertView.findViewById(R.id.buttonAddPayment);
        Button buttonRepay = convertView.findViewById(R.id.buttonRepayLoan);

        /** Показывает информацию о займе */
        String loanText = loan.getName();
        if (loan.getDescription() != null && !loan.getDescription().isEmpty()) {
            loanText += " (" + loan.getDescription() + ")";
        }

        /** Добавляет информацию о исходной сумме и количестве платежей в займе */
        int originalPaymentsCount = (loan.getExpenseList() != null) ? loan.getExpenseList().size() : 0;
        loanText += "\nСумма займа: " + String.format("%.2f", loan.getExpenseListTotalAmount()) + " руб.";
        loanText += " | Платежей: " + originalPaymentsCount;

        textViewInfo.setText(loanText);

        /** Получаем сумму погашений (deposit) */
        List<Deposit> repayments = depositService.getRepaymentsForExpense(loan.getId());
        double totalRepaid = 0.0;
        for (Deposit deposit : repayments) {
            totalRepaid += deposit.getTotalAmount();
        }
        int repaymentsCount = repayments.size();

        double totalLoan = loan.getExpenseListTotalAmount();
        double remainingDebt = totalLoan - totalRepaid;

        /** Отображаем сумму погашений */
        textViewTotalRepaid.setText(String.format("%.2f руб.", totalRepaid));

        /** Отображаем количество погашений (отдельное поле) */
        textViewPaymentsCount.setText(String.valueOf(repaymentsCount));

        /** Отображаем остаток долга */
        textViewRemainingDebt.setText(String.format("%.2f руб.", remainingDebt));

        if (remainingDebt <= 0) {
            textViewRemainingDebt.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            /** Можно также изменить цвет кнопки или добавить надпись */
            buttonRepay.setEnabled(false);
            buttonRepay.setText("Погашен");
        } else {
            textViewRemainingDebt.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            buttonRepay.setEnabled(true);
            buttonRepay.setText("Погасить");
        }

        /** Устанавливает цвет текста основной информации */
        if (loan.getRowColor() != null && loan.getRowColor() != -1) {
            textViewInfo.setTextColor(loan.getRowColor());
        } else {
            textViewInfo.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        /** Добавляет в интерфейс вывод даты */
        TextView textViewDate = convertView.findViewById(R.id.textViewExpenseDate);
        textViewDate.setText(loan.getDateTimeString());

        /** Обработчик нажатия на кнопку + (добавить платёж) */
        buttonAdd.setOnClickListener(v -> {
            v.setFocusable(true);
            v.requestFocus();
            showAddPaymentDialog(position, loan);
        });

        /** Обработчик нажатия на кнопку Погасить */
        buttonRepay.setOnClickListener(v -> {
            if (repayListener != null && remainingDebt > 0) {
                repayListener.onRepayClick(loan);
            }
        });

        /** Обработчик нажатия на всю строку */
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(loan, position);
            }
        });

        /** Выделяет выбранную строку */
        if (position == selectedPosition) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_color));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        return convertView;
    }
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        if (convertView == null) {
//            LayoutInflater inflater = LayoutInflater.from(context);
//            convertView = inflater.inflate(R.layout.list_item_long_loan, parent, false);
//        }
//
//        Expense loan = loansList.get(position);
//
//        TextView textViewInfo = convertView.findViewById(R.id.textViewExpenseInfo);
//        TextView textViewTotalRepaid = convertView.findViewById(R.id.textViewTotalRepaid);
//        TextView textViewRemainingDebt = convertView.findViewById(R.id.textViewRemainingDebt);
//        Button buttonAdd = convertView.findViewById(R.id.buttonAddPayment);
//        Button buttonRepay = convertView.findViewById(R.id.buttonRepayLoan);
//
//        /** Показывает информацию о займе */
//        String loanText = loan.getName();
//        if (loan.getDescription() != null && !loan.getDescription().isEmpty()) {
//            loanText += " (" + loan.getDescription() + ")";
//        }
//
//        /** Добавляет информацию о исходной сумме и платежах */
//        if (loan.getExpenseList() != null && !loan.getExpenseList().isEmpty()) {
//            loanText += "\nИсходная сумма: " + String.format("%.2f", loan.getExpenseListTotalAmount()) + " руб.";
//            loanText += " | Платежей: " + loan.getExpenseList().size();
//        } else {
//            loanText += "\nИсходная сумма: 0.00 руб.";
//            loanText += " | Платежей: 0";
//        }
//
//        textViewInfo.setText(loanText);
//
//        /** Получаем сумму погашений (deposit) */
//        double totalRepaid = getTotalRepaid(loan);
//        double totalLoan = loan.getExpenseListTotalAmount();
//        double remainingDebt = totalLoan - totalRepaid;
//
//        /** Отображаем сумму погашений */
//        textViewTotalRepaid.setText(String.format("%.2f руб.", totalRepaid));
//
//        /** Отображаем остаток долга */
//        textViewRemainingDebt.setText(String.format("%.2f руб.", remainingDebt));
//
//        if (remainingDebt <= 0) {
//            textViewRemainingDebt.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
//        } else {
//            textViewRemainingDebt.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
//        }
//
//        /** Устанавливает цвет текста основной информации */
//        if (loan.getRowColor() != null && loan.getRowColor() != -1) {
//            textViewInfo.setTextColor(loan.getRowColor());
//        } else {
//            textViewInfo.setTextColor(ContextCompat.getColor(context, android.R.color.black));
//        }
//
//        /** Добавляет в интерфейс вывод даты */
//        TextView textViewDate = convertView.findViewById(R.id.textViewExpenseDate);
//        textViewDate.setText(loan.getDateTimeString());
//
//        /** Обработчик нажатия на кнопку + (добавить платёж) */
//        buttonAdd.setOnClickListener(v -> {
//            v.setFocusable(true);
//            v.requestFocus();
//            showAddPaymentDialog(position, loan);
//        });
//
//        /** Обработчик нажатия на кнопку Погасить */
//        buttonRepay.setOnClickListener(v -> {
//            if (repayListener != null) {
//                repayListener.onRepayClick(loan);
//            }
//        });
//
//        /** Обработчик нажатия на всю строку */
//        convertView.setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onItemClick(loan, position);
//            }
//        });
//
//        /** Выделяет выбранную строку */
//        if (position == selectedPosition) {
//            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_color));
//        } else {
//            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
//        }
//
//        return convertView;
//    }


    private double getTotalRepaid(Expense loan) {

        double total = 0.0;

        List<Deposit> repayments = depositService.getRepaymentsForExpense(loan.getId());

        for (Deposit deposit : repayments) {
            total += deposit.getTotalAmount();
        }

        return total;
    }


    private void showAddPaymentDialog(int position, Expense loan) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Добавить платёж");
        builder.setMessage("Заём: " + loan.getName());

        final EditText input = new EditText(context);
        input.setHint("Введите сумму");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_SIGNED |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);


        builder.setPositiveButton("Добавить", (dialog, which) -> {

            String valueStr = input.getText().toString().trim();
            if (valueStr.isEmpty()) {
                Toast.makeText(context, "Введите сумму", Toast.LENGTH_SHORT).show();
                return;
            }


            try {

                double payment = Double.parseDouble(valueStr);

                if (expenseService.addPaymentToExpense(loan, payment)) {
                    Toast.makeText(context, "Платёж добавлен: " + String.format("%.2f", payment),
                            Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Ошибка при добавлении платежа",
                            Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Введите корректное число",
                        Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }


}