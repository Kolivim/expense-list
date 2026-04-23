package com.example.test3.monthly.expense.refund.planning;

import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_PLANNED_REFUND_PLANNING;
import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_REFUND_PLANNING;
import static com.example.test3.util.Util.TYPE_EXPENSE_MONTH_PLANNING;
import static com.example.test3.util.Util.TYPE_EXPENSE_MONTH_REFUND_PLANNING;
import static com.example.test3.util.Util.TYPE_MONTHLY_REFUND_PLANNING;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test3.R;
import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.Expense;
import com.example.test3.month.Month;
import com.example.test3.monthly.expense.utility.service.MonthUtilityServiceDto;
import com.example.test3.service.ExpenseService;
import com.example.test3.service.RefundService;
import com.example.test3.util.Util;
import com.example.test3.util.UtilService;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class ExpensePlannedRefundActivity extends AppCompatActivity {

    private static final String TAG = "ExpensePlannedRefundActivity";

    private Long monthType = TYPE_MONTHLY_REFUND_PLANNING;
    private Long expenseType = TYPE_EXPENSE_MONTH_REFUND_PLANNING;
    private Long depositType = TYPE_DEPOSIT_MONTH_REFUND_PLANNING;
    private Long plannedDepositType = TYPE_DEPOSIT_MONTH_PLANNED_REFUND_PLANNING;

    /** Относящееся к самому расходу */
    private TextView textViewName, textViewDescription, textViewDate, textViewExpense;
    /** Относящееся к планированию выплат */
    private TextView textViewMonthCount, textViewStartDate;

    private RefundService refundService;
    private ExpenseService expenseService;

    /** Кратность округления, при рассчёте плановых Deposit */
    private int step = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expense_planned_refund);

        refundService = new RefundService(this);
        expenseService = new ExpenseService(this);

        initViews();
    }


    private void initViews() {
        textViewName = findViewById(R.id.name);
        textViewDescription = findViewById(R.id.description);
        textViewDate = findViewById(R.id.date);
        textViewExpense = findViewById(R.id.expense);

        textViewMonthCount = findViewById(R.id.monthCount);
        textViewStartDate = findViewById(R.id.startDate);
    }


    public void save(View view) {
        Log.d(TAG, "save() startMethod");

        /** Параметры Expense : */
        String expenseName = textViewName.getText().toString();

        Double expense = null;
        if (textViewExpense.getText() != null && !textViewExpense.getText().toString().isEmpty())
            expense = Double.parseDouble(textViewExpense.getText().toString());

        String expenseDateTimeString = textViewDate.getText().toString();

        String expenseDescription = textViewDescription.getText().toString();
        /** !Параметры Expense */


        /** Параметры возврата : */
        Integer monthCount = null;
        if (textViewMonthCount.getText() != null && !textViewMonthCount.getText().toString().isEmpty())
            monthCount = Integer.parseInt(textViewMonthCount.getText().toString());

        String startDateString = textViewStartDate.getText().toString();
        /** !Параметры возврата */


        ExpenseRefund newExpense = null;
        if (!expenseName.isEmpty() /** заменить на check */ ) {

            newExpense = getNewExpense(
                    expenseName, expense, expenseDateTimeString, expenseDescription,
                    monthCount, startDateString, expenseType);

            /* expenseService */ refundService.insertExpense(newExpense);

        }

        /** Закрываем активити после выполнения сохранения */
        finish();
        Log.d(TAG, "save() endMethod");
    }


    public void cancel(View view) {finish();}


    public ExpenseRefund getNewExpense(String expenseName, Double expense,
                                 String expenseDateTimeString, String expenseDescription,
                                 Integer monthCount, String startDateString,
                                 Long expenseType) {
        Log.d(TAG + getCurrentMethodName(), "startMethod");

        if(expenseName != null && !expenseName.isEmpty()) {

            ExpenseRefund newExpense = new ExpenseRefund(expenseName, expenseType);

            ZonedDateTime expenseZonedDateTime = null;
            if(expenseDateTimeString != null && !expenseDateTimeString.isEmpty()) {
                expenseZonedDateTime = UtilService.parseDate(expenseDateTimeString);
            } else {
                expenseZonedDateTime = ZonedDateTime.now();
            }
            newExpense.setDateTime(expenseZonedDateTime);

            if(expense != null && !expense.isNaN()) newExpense.addPayment(expense);

            if(expenseDescription != null && !expenseDescription.isEmpty())
                newExpense.setDescription(expenseDescription);

            ZonedDateTime startZonedDate = null;
            if(startDateString != null && !startDateString.isEmpty()) {
                startZonedDate = UtilService.parseDate(startDateString);
            } else {
                startZonedDate = ZonedDateTime.now();
            }
            newExpense.setStartDate(startZonedDate);

            newExpense.setMonthCount(monthCount);


            /** Рассчитываем и прикрепляем список плановых возвратов : */
            newExpense.setPlannedDepositList(getPlannedDepositList(monthCount, startZonedDate, expense, step, expenseName));
            /** !Рассчитываем и прикрепляем список плановых возвратов */


            Log.d(TAG + getCurrentMethodName(),
                    "endMethod к возврату newExpense: " + newExpense);
            return newExpense;
        }

        return null;
    }


    public ArrayList<Deposit> getPlannedDepositList(Integer monthCount, ZonedDateTime startZonedDate,
                                                    Double expense, int step, String expenseName) {
        Log.d(TAG + getCurrentMethodName(),
                "startMethod monthCount: " + monthCount + ", startZonedDate: " + startZonedDate + ", expense" + expense);

        ArrayList<Deposit> plannedDepositList = new ArrayList<>();

        double monthPayment = expense / monthCount;
        monthPayment = Math.floor(monthPayment/step) * step;

        double addedPayment = 0.0;
        ZonedDateTime depositDateTime = startZonedDate;
        for (int i = 0; i <= monthCount; i++) {

            //
            ZonedDateTime dateTime = startZonedDate.plusMonths(i);

            Deposit plannedDeposit = new Deposit(expenseName, plannedDepositType, dateTime,
                    null /* parentId - на данном этапе нет ещё id записи Expense, после получения подсетить */ ,
                    monthPayment);
            //

            addedPayment += monthPayment;
            plannedDepositList.add(plannedDeposit);
        }


        /** Добавляем последний платёж : */
        //
        ZonedDateTime lastDateTime = startZonedDate.plusMonths(monthCount);
        Double lastDepositPayment = expense - addedPayment;
        Deposit lastDeposit = new Deposit(expenseName, plannedDepositType, lastDateTime,
                null /* parentId - на данном этапе нет ещё id записи Expense, после получения подсетить */ ,
                lastDepositPayment);
        //
        plannedDepositList.add(lastDeposit);


        Log.d(TAG + getCurrentMethodName(),
                "endMethod к возврату ArrayList<Deposit> plannedDepositList: " + plannedDepositList);
        return plannedDepositList;
    }


    public static String getCurrentMethodName() {return new Throwable().getStackTrace()[1].getMethodName();}


}