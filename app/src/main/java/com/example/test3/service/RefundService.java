package com.example.test3.service;

import static com.example.test3.dao.ExpenseSQLite.EXPENSE_DATETIME;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_DESCRIPTION;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_EXPENSE_TYPE_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_IS_DELETED;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_NAME;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_REFUND_EXPENSE_ID;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_REFUND_MONTH_COUNT;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_REFUND_START_DATE;
import static com.example.test3.dao.ExpenseSQLite.EXPENSE_ROW_COLOR;
import static com.example.test3.dao.ExpenseSQLite.TABLE_EXPENSE;
import static com.example.test3.dao.ExpenseSQLite.TABLE_EXPENSE_REFUND;
import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_PLANNED_REFUND_PLANNING;
import static com.example.test3.util.Util.TYPE_DEPOSIT_MONTH_REFUND_PLANNING;
import static com.example.test3.util.Util.TYPE_EXPENSE_MONTH_REFUND_PLANNING;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.test3.dao.ExpenseSQLite;
import com.example.test3.deposit.Deposit;
import com.example.test3.monthly.expense.refund.planning.ExpenseRefund;
import com.example.test3.util.Util;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class RefundService {

    private static final String TAG = "RefundService";

    private Long expenseType = TYPE_EXPENSE_MONTH_REFUND_PLANNING;
    private Long depositType = TYPE_DEPOSIT_MONTH_REFUND_PLANNING;
    private Long plannedDepositType = TYPE_DEPOSIT_MONTH_PLANNED_REFUND_PLANNING;

    private Context context;

    private ExpenseSQLite dbHelper;

    private SQLiteDatabase dbWrite;

    private SQLiteDatabase dbRead;

    private ExpenseService expenseService;
    private DepositService depositService;


    public RefundService(Context baseContext) {
        this.context = baseContext;
        this.dbHelper = new ExpenseSQLite(this.context);
        this.dbWrite = dbHelper.getWritableDatabase();
        this.dbRead = dbHelper.getReadableDatabase();
        this.expenseService = new ExpenseService(context);
        this.depositService = new DepositService(context);
    }


    public List<ExpenseRefund> getExpenseRefundList() {
        Log.d(TAG, "getExpenseRefundList() start");

        List<ExpenseRefund> refunds = expenseService.getExpenseRefundList();

        /** Загружаем запланированные взносы */
        for (ExpenseRefund expenseRefund : refunds) {
            List<Deposit> plannedDeposits = depositService.getExpenseDeposits(expenseRefund.getId(), plannedDepositType);
            expenseRefund.setPlannedDepositList(plannedDeposits);
        }

        /** Загружаем внесённые взносы */
        for (ExpenseRefund expenseRefund : refunds) {
            List<Deposit> deposits = depositService.getExpenseDeposits(expenseRefund.getId(), depositType);
            expenseRefund.setDepositList(deposits);
        }

        Log.d(TAG, "getExpenseRefundList() end, size=" + refunds.size());
        return refunds;
    }


    public boolean insertExpense(ExpenseRefund expense) {
        Log.d(TAG, "insertExpense() startMethod, ExpenseRefund: " + expense);

        /** Вставляем саму Expense : */
        ExpenseRefund insertedExpense = expenseService.insertExpenseRow(expense);
        if(insertedExpense.getId() == -1) return false;

        expense.setParentId(insertedExpense.getId());

        /** Вставляем PlannedDeposit к Expense :
         * (обычный Deposit не вставляем, ввиду того, что при создании считается не возможным наличие возврата сразу) */
        for (Deposit plannedDeposit : expense.getPlannedDepositList()) {
            Long depositId = depositService.insertDeposit(plannedDeposit);
            if(depositId == -1) Log.d(TAG, "insertExpense() ошибка записи PlannedDeposit: " + plannedDeposit + " для ExpenseRefund: " + expense);;
        }

        return insertedExpense.getId() != -1;
    }


}
