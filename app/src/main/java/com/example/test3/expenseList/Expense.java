package com.example.test3.expenseList;

import com.example.test3.util.Util;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Expense implements Serializable {

    private String name;
    private String description;
    private ZonedDateTime dateTime;

    private ArrayList<Double> expenseList;


    /** Необходимое для UI : */
    private boolean isDeleted;
    private Integer rowColor;


    /** Необходимое для DAO : */
    private Long id;


    public Expense() {}


    public Expense(String expenseName)  {
        this.name= expenseName;
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


    public Expense(Long id, String name, String description, ZonedDateTime dateTime, boolean isDeleted, Integer rowColor) {
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
    public void setDescription(String descripton) {this.description = description;}

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

    public boolean isDeleted() {return isDeleted;}
    public void setDeleted(boolean deleted) {isDeleted = deleted;}

    public ZonedDateTime getDateTime() {return dateTime;}
    public void setDateTime(ZonedDateTime dateTime) {this.dateTime = dateTime;}
    public String getDateTimeString() {return this.dateTime.format(Util.dateFormatterSee);}

    public Integer getRowColor() {return rowColor;}
    public void setRowColor(Integer rowColor) {this.rowColor = rowColor;}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}


    @Override
    public String toString() {
        return getDateTimeString()
                .concat("\t\t").concat(this.name)
                .concat(this.description == null ? "" : " (".concat(this.description).concat(")"))
                .concat(", Total Amount = ").concat(getExpenseListTotalAmountString());
    }

}
