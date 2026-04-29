package com.example.test3.longs.loans;

import com.example.test3.account.Account;
import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.Expense;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpenseLongLoans extends Expense implements Serializable {

    /** Счёт/банк-источник займа */
    private Account account;


    public ExpenseLongLoans() {}

    public ExpenseLongLoans(String expenseName, Long typeId)  {
        super(expenseName, typeId);
    }


    public ExpenseLongLoans(Expense expense) {

        super(expense.getId(), expense.getTypeId(), expense.getName(), expense.getDescription(),
                expense.getDateTime(), expense.isDeleted(), expense.getRowColor());

        if (expense.getExpenseList() != null)
            this.setExpenseList(new ArrayList<>(expense.getExpenseList()));


        if (expense.getDepositList() != null)
            this.setDepositList(new ArrayList<>(expense.getDepositList()));

    }
//    public ExpenseLongLoans(Long id, Long typeId, String name, String description,
//                            ZonedDateTime dateTime, boolean isDeleted, Integer rowColor) {
//        super(id, typeId, name, description, dateTime, isDeleted, rowColor);
//    }


    public Account getAccount() {return account;}
    public void setAccount(Account account) {this.account = account;}


    @Override
    public String toString() {
        return "ExpenseLongLoans{ " +
                super.toString() +
                ", account: " + account +
                " }";
    }


}
