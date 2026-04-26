package com.example.test3.monthly.expense.refund.planning;

import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.Expense;
import com.example.test3.util.Util;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRefund extends Expense implements Serializable {

    /** Запланированные к возврату Deposit */
    private List<Deposit> plannedDepositList;

    /** Взносы помимо запланированных, на один Deposit все относим */
    private Deposit overPlannedDeposit;

    private boolean isRefunded;

    private Integer monthCount;

    private ZonedDateTime startDate;


    public ExpenseRefund() {}

    public ExpenseRefund(String expenseName, Long typeId)  {
        super(expenseName, typeId);
        isRefunded = false;
    }


    public ExpenseRefund(Long id, Long typeId, String name, String description,
                         ZonedDateTime dateTime, boolean isDeleted, Integer rowColor) {
        super(id, typeId, name, description, dateTime, isDeleted, rowColor);
    }


    /** plannedDepositList : */
    public List<Deposit> getPlannedDepositList() {return plannedDepositList;}
    public void setPlannedDepositList(List<Deposit> plannedDepositList) {this.plannedDepositList = plannedDepositList;}

    public void addPlannedDeposit(Deposit plannedDeposit) {
        if(this.plannedDepositList == null) this.plannedDepositList = new ArrayList<>();
        this.plannedDepositList.add(plannedDeposit);
        refundedCalculate();
    }

    public double getPlannedDepositListTotalAmount() {

        double totalAmount = 0.0;
        if(this.plannedDepositList == null) return totalAmount;

        for (Deposit deposit : this.plannedDepositList) totalAmount = totalAmount + deposit.getTotalAmount();
        return totalAmount;
    }


    public void setParentId(Long parentId) {

        if(this.plannedDepositList != null) {
            for (Deposit plannedDeposit : plannedDepositList) {
                plannedDeposit.setExpenseId(parentId);
            }
        }

        if(super.depositList != null) {
            for (Deposit deposit : depositList) {
                deposit.setExpenseId(parentId);
            }
        }

    }
    /** !plannedDepositList */


    public Deposit getOverPlannedDeposit() {return overPlannedDeposit;}
    public void setOverPlannedDeposit(Deposit overPlannedDeposit) {this.overPlannedDeposit = overPlannedDeposit;}

    public Integer getMonthCount() {return monthCount;}
    public void setMonthCount(Integer monthCount) {this.monthCount = monthCount;}
    public Integer getMonthCountInt() {return monthCount == null ? 0 : monthCount;}

    public ZonedDateTime getStartDate() {return startDate;}
    public void setStartDate(ZonedDateTime startDate) {this.startDate = startDate;}

    public boolean isRefunded() {return isRefunded;}
    public void setRefunded(boolean refunded) {isRefunded = refunded;}
    public void refundedCalculate() {
        isRefunded = getDepositListTotalAmount() >= getExpenseListTotalAmount();
    }


    @Override
    public void setExpenseList(ArrayList<Double> expenseList) {
        super.setExpenseList(expenseList);
        refundedCalculate();
    }

    @Override
    public void addPayment(double expense) {
        super.addPayment(expense);
        refundedCalculate();
    }

    @Override
    public void setDepositList(List<Deposit> depositList) {
        super.setDepositList(depositList);
        refundedCalculate();
    }

    @Override
    public void addDeposit(Deposit deposit) {
        super.addDeposit(deposit);
        refundedCalculate();
    }


    @Override
    public String toString() {
        return "ExpenseRefund{" +
                super.toString() +
                ", overPlannedDeposit=" + overPlannedDeposit +
                ", isRefunded=" + isRefunded +
                '}';
    }


}
