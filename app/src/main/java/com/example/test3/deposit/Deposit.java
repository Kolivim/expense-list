package com.example.test3.deposit;

import com.example.test3.util.Util;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Deposit implements Serializable {
    private Long id;

    /** Может быть как NULL, так monthlyId, так и expenseId */
    private Long expenseId;                                                                         /** ссылка на расход (может быть null), для типа взноса == 1 - погашение ежемесячных затрат - не смогу никакой поставить expenseId, т.к. нет итоговой записи, она суммируется */
    private Long typeId;                                                                            /** тип взноса == 1 - погашение ежемесячных затрат в проработке сейчас */
    private String name;
    private String description;
    private ZonedDateTime dateTime;

    private boolean isDeleted;
    private Integer rowColor;

    private List<Double> payments;


    public Deposit() {this.payments = new ArrayList<>();}


    public Deposit(String name, Long typeId) {
        this.name = name;
        this.typeId = typeId;
        this.dateTime = ZonedDateTime.now();
        this.isDeleted = false;
        this.payments = new ArrayList<>();
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ZonedDateTime getDateTime() { return dateTime; }
    public void setDateTime(ZonedDateTime dateTime) { this.dateTime = dateTime; }
    public String getDateTimeString() { return dateTime.format(Util.dateFormatterSee); }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public Integer getRowColor() { return rowColor; }
    public void setRowColor(Integer rowColor) { this.rowColor = rowColor; }

    public List<Double> getPayments() { return payments; }
    public void setPayments(List<Double> payments) { this.payments = payments; }


    public void addPayment(double payment) {
        if (this.payments == null) this.payments = new ArrayList<>();
        this.payments.add(payment);
    }


    public double getTotalAmount() {
        if (payments == null) return 0.0;
        double total = 0.0;
        for (Double payment : payments) {
            total += payment;
        }
        return total;
    }


}