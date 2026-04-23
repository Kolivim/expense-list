package com.example.test3.monthly.expense.refund.planning;

import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_PLANNED_REFUND_PLANNING;
import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_REFUND_PLANNING;
import static com.example.test3.util.Util.TYPE_EXPENSE_MONTH_REFUND_PLANNING;
import static com.example.test3.util.Util.TYPE_EXPENSE_UTILITY_BILLS;
import static com.example.test3.util.Util.TYPE_MONTHLY_REFUND_PLANNING;
import static com.example.test3.util.Util.TYPE_MONTHLY_UTILITY_BILLS;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;
import com.example.test3.expenseList.Expense;
import com.example.test3.expenseList.ExpenseDetailWithDeleteActivity;
import com.example.test3.month.Month;
import com.example.test3.monthly.expense.planning.MonthlyExpensePlanningDto;
import com.example.test3.monthly.expense.utility.service.MonthUtilityServiceAdapter;
import com.example.test3.monthly.expense.utility.service.MonthUtilityServiceDto;
import com.example.test3.service.DepositService;
import com.example.test3.service.ExpenseService;
import com.example.test3.service.MeterService;
import com.example.test3.service.MonthService;
import com.example.test3.service.RefundService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class MonthRefundPlannedServiceActivity extends AppCompatActivity {

    private static final String TAG = "MonthRefundPlannedServiceActivity";

    private MonthService monthService;
    private RefundService refundService;
    private ExpenseService expenseService;
    private DepositService depositService;

    private Long monthType = TYPE_MONTHLY_REFUND_PLANNING;
    private Long expenseType = TYPE_EXPENSE_MONTH_REFUND_PLANNING;
    private Long depositType = TYPE_DEPOSIT_MONTH_REFUND_PLANNING;
    private Long plannedDepositType = TYPE_DEPOSIT_MONTH_PLANNED_REFUND_PLANNING;

    private RecyclerView recyclerView;
    private ExpenseRefundAdapter adapter;

    private List<ExpenseRefund> refundList;
    private ExpenseRefund selectedExpense = null;
    private int selectedExpenseRefund = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MonthUtilityServiceActivity " + getCurrentMethodName(), " startMethod");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_refund_planning);

        refundService = new RefundService(this);
        monthService = new MonthService(this);
        expenseService = new ExpenseService(this);
        depositService = new DepositService(this);

        recyclerView = findViewById(R.id.recyclerViewRefunds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();

        Log.d("loadExpenses" + getCurrentMethodName(), "endMethod");
    }


    private void loadData() {
        Log.d(TAG, "loadData startMethod");

        refundList = refundService.getExpenseRefundList();
        if (refundList.isEmpty()) {
            Toast.makeText(this, "Нет данных", Toast.LENGTH_LONG).show();
        }

        adapter = new ExpenseRefundAdapter(this, refundList);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "loadData end, size=" + refundList.size());
    }


    public void create(View view) {
        Log.d(TAG + getCurrentMethodName(), "startMethod");

        Intent intent = new Intent(this, ExpensePlannedRefundActivity.class);
        this.startActivity(intent);

        Log.d(TAG + getCurrentMethodName(), "endMethod");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume start");
        /** Обновляет список при каждом возврате на главную активити */
        loadData();
        Log.d(TAG, "onResume end");
    }


    public void back(View view) {finish();}


    public static String getCurrentMethodName() {return new Throwable().getStackTrace()[1].getMethodName();}


//    public void add(View view) {
//        Log.d(TAG + getCurrentMethodName(), "startMethod");
//
//        EditText expenseNameEditText = findViewById(R.id.editTextNameExpense);
//        String expenseName = expenseNameEditText.getText().toString();
//
//        EditText expenseEditText = findViewById(R.id.editTextNumberDecimal);
//        Double expense = null;
//        if (expenseEditText.getText() != null && !expenseEditText.getText().toString().isEmpty())
//            expense = Double.parseDouble(expenseEditText.getText().toString());
//
//        EditText expenseDateEditText = findViewById(R.id.editTextDate);
//        String expenseDateTimeString = expenseDateEditText.getText().toString();
//
//        String expenseDescription = null;
//
//        Expense newExpense = null;
//        if (!expenseName.isEmpty()) {
//            newExpense = getNewExpense(expenseName, expense, expenseDateTimeString, expenseDescription);
//            expenseService.insertExpense(newExpense);
//        }
//
//        if (newExpense != null) monthService.getOrCreateExpenseMonth(newExpense, monthType);
//
//        loadData();
//        cleanUserInput(expenseNameEditText, expenseEditText, expenseDateEditText);
//        Log.d(TAG + getCurrentMethodName(), "endMethod");
//    }
//
//
//    public void remove(View view){
//        Log.d(TAG + getCurrentMethodName(), "startMethod");
//
//        if (selectedMonthPosition == -1) {
//            Toast.makeText(this, "Выберите месяц для удаления", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        MonthUtilityServiceDto dto = monthDtoList.get(selectedMonthPosition);
//
//        Log.d(TAG + getCurrentMethodName(),
//                "Получен к удалению MonthlyExpensePlanningDto: ".concat(dto.toString()));
//
//        boolean success = monthService.removeMonth(dto);
//
//
//        if (success) {
//            Toast.makeText(this, "Расход удалён", Toast.LENGTH_SHORT).show();
//            loadData();
//        } else {
//            Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
//        }
//
//
//        Log.d(TAG + getCurrentMethodName(), "endMethod");
//    }
//
//
//    public Expense getNewExpense(String expenseName, Double expense,
//                                 String expenseDateTimeString, String expenseDescription) {
//        Log.d(TAG + getCurrentMethodName(), "startMethod");
//
//        if(expenseName != null && !expenseName.isEmpty()) {
//
//            Expense newExpense = new Expense(expenseName, expenseType);
//
//            if(expense != null && !expense.isNaN()) newExpense.addPayment(expense);
//
//            if(expenseDateTimeString != null && !expenseDateTimeString.isEmpty()) {
//                ZonedDateTime expenseZonedDateTime = getZoneDateTime(expenseDateTimeString);
//                if(expenseZonedDateTime != null) newExpense.setDateTime(expenseZonedDateTime);
//            }
//
//            if(expenseDescription != null && !expenseDescription.isEmpty()) newExpense.setDescription(expenseDescription);
//
//            Log.d(TAG + getCurrentMethodName(),
//                    "endMethod к возврату newExpense: " + newExpense);
//            return newExpense;
//        }
//
//
//        return null;
//    }


}