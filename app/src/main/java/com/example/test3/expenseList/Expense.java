package com.example.test3.expenseList;

import com.example.test3.deposit.Deposit;
import com.example.test3.util.Util;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Expense implements Serializable {

    private String name;
    private String description;
    private ZonedDateTime dateTime;
    private Long typeId;

    private ArrayList<Double> expenseList;


    /** Необходимое для UI : */
    private boolean isDeleted;
    private Integer rowColor;


    /** Необходимое для DAO : */
    private Long id;


    /** Необходимое для DTO : */
    /** Deposit может быть (при расходах, относящихся к планированию бюджета),
     * а может и отсутствовать (при текущих месячных расходах) */
    protected List<Deposit> depositList;


    public Expense() {}


    public Expense(String expenseName, Long typeId)  {
        this.typeId = typeId;
        this.name = expenseName;
        this.isDeleted = false;
        this.dateTime = ZonedDateTime.now();
    }


    public Expense(String expenseName, String expenseDescription)  {
        this.name= expenseName;
        this.description = expenseDescription;
        this.isDeleted = false;
        this.dateTime = ZonedDateTime.now();
    }


    public Expense(String expenseName, String expenseDescription, boolean isDeleted)  {
        this.name= expenseName;
        this.description = expenseDescription;
        this.isDeleted = isDeleted;
        this.dateTime = ZonedDateTime.now();
    }


    public Expense(String name, String descripton, double expense) {
        this.name = name;
        this.description = descripton;
        this.expenseList = new ArrayList<>();
        this.expenseList.add(expense);
        this.dateTime = ZonedDateTime.now();
    }


    public Expense(String name, String description, double expense, ZonedDateTime expenseDateTime) {
        this.name = name;
        this.description = description;
        this.expenseList = new ArrayList<>();
        this.expenseList.add(expense);
        this.dateTime = expenseDateTime;
    }


    public Expense(Long id, Long typeId, String name, String description, ZonedDateTime dateTime, boolean isDeleted, Integer rowColor) {
        this.typeId = typeId;
        this.name = name;
        this.description = description;
        this.dateTime = dateTime;
        this.isDeleted = isDeleted;
        this.rowColor = rowColor;
        this.id = id;
    }


    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public ArrayList<Double> getExpenseList() {return expenseList;}
    public void setExpenseList(ArrayList<Double> expenseList) {this.expenseList = expenseList;}
    public void addPayment(double expense) {
        if(this.expenseList == null) this.expenseList = new ArrayList<>();
        this.expenseList.add(expense);
    }
    public double getExpenseListTotalAmount() {
        if(this.expenseList == null) return 0.0;
        double totalAmount = 0.0;
        for (Double expense : this.expenseList) {
            totalAmount = totalAmount + expense;
        }
        return totalAmount;
    }
    public String getExpenseListTotalAmountString() {
        return String.valueOf(getExpenseListTotalAmount());
    }


    public double getDepositListTotalAmount() {

        double totalAmount = 0.0;

        if(this.depositList == null) return totalAmount;

        for (Deposit deposit : this.depositList) totalAmount = totalAmount + deposit.getTotalAmount();

        return totalAmount;
    }


    public double getBalance() {
        return getDepositListTotalAmount() - getExpenseListTotalAmount();
    }


    public boolean isDeleted() {return isDeleted;}
    public void setDeleted(boolean deleted) {isDeleted = deleted;}

    public ZonedDateTime getDateTime() {return dateTime;}
    public void setDateTime(ZonedDateTime dateTime) {this.dateTime = dateTime;}
    public String getDateTimeString() {return this.dateTime.format(Util.dateFormatterSee);}

    public Integer getRowColor() {return rowColor;}
    public void setRowColor(Integer rowColor) {this.rowColor = rowColor;}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Long getTypeId() {return typeId;}
    public void setTypeId(Long typeId) {this.typeId = typeId;}


    public List<Deposit> getDepositList() {return depositList;}
    public void setDepositList(List<Deposit> depositList) {this.depositList = depositList;}
    public void addDeposit(Deposit deposit) {
        if(this.depositList == null) this.depositList = new ArrayList<>();
        this.depositList.add(deposit);
    }


    @Override
    public String toString() {
        return getDateTimeString()
                .concat("\t\t").concat(this.name)
                .concat(this.description == null ? "" : " (".concat(this.description).concat(")"))
                .concat(", Total Amount = ").concat(getExpenseListTotalAmountString());
    }

}
