package com.example.test3.month;

import com.example.test3.deposit.Deposit;
import com.example.test3.expenseList.Expense;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Создаётся для каждого месяца автоматически, при наличии трат в месяце, пересчитывается и
 * создаются новые при каждом переходе на соответсвующюю Активити */
public class MonthlyDto implements Serializable {

    /** Месяц, к которому относятся траты / возвраты
     * (при наличии трат в месяце сохраняется в БД для Month,
     * сохранение и пересчёт рассчётных величин происходит
     * при каждом входе на соответствующюю Активити) */
    private Month month;


    /** Рассчётная величина */
    private double totalExpenseAmount;
    /** Рассчётная величина */
    private int expensesCount;
    /** Рассчётная величина */
    private int paymentsCount;

    /** Список Expense, относящихся к указанному в Month месяцу */
    private List<Expense> expenseList;


    /** Расчётная величина */
    private double totalDepositAmount;
    /** Рассчётная величина */
    private int depositsCount;

    /** Список Deposit, относящихся к Month
     * (ищем в таблице deposit по type == 1 и ссылке expenseId == monthId из таблицы Month,
     * что реализовано в отдельном DepositService, вызываемом из соответствующей Активити,
     * которую нужно создать) */
    private List<Deposit> depositList;


    /** Итог, после возврата (может быть не полным возвратом), рассчётная величина */
    private double balance;


    public MonthlyDto() {
        this.expenseList = new ArrayList<>();
        this.depositList = new ArrayList<>();
    }


    /** Конструктор с конкретным месяцем конкретного года */
    public MonthlyDto(Month month) {

        this.month = month;
        this.expenseList = new ArrayList<>();
        this.depositList = new ArrayList<>();
        this.totalExpenseAmount = 0.0;
        this.expensesCount = 0;
        this.paymentsCount = 0;
        this.totalDepositAmount = 0.0;
        this.depositsCount = 0;
        this.balance = 0.0;

    }


    public Month getMonth() {return month;}
    public void setMonth(Month month) {this.month = month;}


    public double getTotalExpenseAmount() {return totalExpenseAmount;}
    public void setTotalExpenseAmount(double totalExpenseAmount) {
        this.totalExpenseAmount = totalExpenseAmount;
        calculateBalance();
    }


    public int getExpensesCount() {return expensesCount;}
    public void setExpensesCount(int expensesCount) {this.expensesCount = expensesCount;}


    public int getPaymentsCount() {return paymentsCount;}
    public void setPaymentsCount(int paymentsCount) {this.paymentsCount = paymentsCount;}


    public List<Expense> getExpenseList() {return expenseList;}
    public void setExpenseList(List<Expense> expenseList) {
        this.expenseList = expenseList;
        recalcExpenseStats();
    }


    public double getTotalDepositAmount() {return totalDepositAmount;}
    public void setTotalDepositAmount(double totalDepositAmount) {
        this.totalDepositAmount = totalDepositAmount;
        calculateBalance();
    }


    public int getDepositsCount() {return depositsCount;}
    public void setDepositsCount(int depositsCount) {this.depositsCount = depositsCount;}


    public List<Deposit> getDepositList() {return depositList;}
    public void setDepositList(List<Deposit> depositList) {
        this.depositList = depositList;
        recalcDepositStats();
    }


    public double getBalance() {return balance;}
    public void setBalance(double balance) {this.balance = balance;}


    /** Добавляет расход в список и пересчитывает статистику */
    public void addExpense(Expense expense) {

        if (this.expenseList == null) {
            this.expenseList = new ArrayList<>();
        }

        this.expenseList.add(expense);
        recalcExpenseStats();

    }


    /** Добавляет взнос в список и пересчитывает статистику */
    public void addDeposit(Deposit deposit) {

        if (this.depositList == null) {
            this.depositList = new ArrayList<>();
        }

        this.depositList.add(deposit);
        recalcDepositStats();

    }


    /** Пересчитывает статистику по расходам */
    private void recalcExpenseStats() {

        if (expenseList == null || expenseList.isEmpty()) {

            this.expensesCount = 0;
            this.paymentsCount = 0;
            this.totalExpenseAmount = 0.0;

        } else {

            this.expensesCount = expenseList.size();
            this.paymentsCount = 0;
            this.totalExpenseAmount = 0.0;

            for (Expense expense : expenseList) {
                this.totalExpenseAmount += expense.getExpenseListTotalAmount();
                if (expense.getExpenseList() != null) {
                    this.paymentsCount += expense.getExpenseList().size();
                }
            }

        }

        calculateBalance();
    }


    /** Пересчитывает статистику по взносам */
    private void recalcDepositStats() {

        if (depositList == null || depositList.isEmpty()) {

            this.depositsCount = 0;
            this.totalDepositAmount = 0.0;

        } else {

            this.depositsCount = depositList.size();
            this.totalDepositAmount = 0.0;

            for (Deposit deposit : depositList) {
                this.totalDepositAmount += deposit.getTotalAmount();
            }

        }

        calculateBalance();
    }


    /** Пересчитывает баланс */
    private void calculateBalance() {
        this.balance = this.totalDepositAmount - this.totalExpenseAmount;
    }


    /** Очищает все данные */
    public void clear() {
        if (expenseList != null) expenseList.clear();
        if (depositList != null) depositList.clear();
        this.totalExpenseAmount = 0.0;
        this.expensesCount = 0;
        this.paymentsCount = 0;
        this.totalDepositAmount = 0.0;
        this.depositsCount = 0;
        this.balance = 0.0;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MonthlyDto{")
                .append("month=").append(month != null ? month.getMonthYear() : "null")
                .append(", totalExpense=").append(String.format("%.2f", totalExpenseAmount))
                .append(", expenses=").append(expensesCount)
                .append(", payments=").append(paymentsCount)
                .append(", totalDeposit=").append(String.format("%.2f", totalDepositAmount))
                .append(", deposits=").append(depositsCount)
                .append(", balance=").append(String.format("%.2f", balance))
                .append("}");
        return sb.toString();
    }


}