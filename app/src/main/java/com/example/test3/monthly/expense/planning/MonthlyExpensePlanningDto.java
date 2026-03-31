package com.example.test3.monthly.expense.planning;

import com.example.test3.expenseList.Expense;
import com.example.test3.month.Month;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MonthlyExpensePlanningDto implements Serializable {

    /** Месяц, к которому относится планируемый бюджет
     * (при наличии записей бюджета в месяце сохраняется в БД для Month,
     * сохранение и пересчёт рассчётных величин происходит
     * при каждом входе на соответствующюю Активити) */
    private Month month;


    /** Список Expense запланированного бюджета, относящихся к указанному в Month месяцу */
    private List<Expense> expenseList;

    /** Рассчётная величина */
    private double totalExpenseAmount;
    /** Рассчётная величина */
    private int expensesCount;
    /** Рассчётная величина */
    private int paymentsCount;


    /** Расчётная величина, сумма всех взносов, по каждой из Expense, хранящейся в expenseList */
    private double totalDepositAmount;
    /** Рассчётная величина, количество всех взносов, по каждой из Expense, хранящейся в expenseList */
    private int depositsCount;
//    /** Рассчётная величина, количество всех платежей ко всем взносам, по каждой из Expense, хранящейся в expenseList */
//    private int depositsPaymentsCount;


    /** Итог, рассчётная величина, сколько осталось внести Deposit до полного покрытия всех Expense */
    private double balance;


    public MonthlyExpensePlanningDto() {}

    public MonthlyExpensePlanningDto(Month month) {this.month = month;}


    /** Добавляет расход Expense в список запланированных расходов
     * и пересчитывает статистику всего MonthlyExpensePlannyngDto */
    public void addExpense(Expense expense) {

        if (this.expenseList == null) {
            this.expenseList = new ArrayList<>();
        }

        this.expenseList.add(expense);
        recalcExpenseStats();

    }


    /** Пересчитывает статистику по расходам для MonthlyExpensePlannyngDto */
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

//        calculateBalance();
//        calculatePlannedBalance();
    }


    /** Очищает все данные */
    public void clear() {
        if (expenseList != null) expenseList.clear();
        this.totalExpenseAmount = 0.0;
        this.expensesCount = 0;
        this.paymentsCount = 0;
    }


    public List<Expense> getExpenseList() {return expenseList;}
    public void setExpenseList(List<Expense> expenseList) {
        this.expenseList = expenseList;
        recalcExpenseStats();
    }

    public double getTotalExpenseAmount() {return totalExpenseAmount;}
    public void setTotalExpenseAmount(double totalExpenseAmount) {this.totalExpenseAmount = totalExpenseAmount;}

    public int getExpensesCount() {return expensesCount;}
    public void setExpensesCount(int expensesCount) {this.expensesCount = expensesCount;}

    public int getPaymentsCount() {return paymentsCount;}
    public void setPaymentsCount(int paymentsCount) {this.paymentsCount = paymentsCount;}

    public double getTotalDepositAmount() {return totalDepositAmount;}
    public void setTotalDepositAmount(double totalDepositAmount) {this.totalDepositAmount = totalDepositAmount;}

    public int getDepositsCount() {return depositsCount;}
    public void setDepositsCount(int depositsCount) {this.depositsCount = depositsCount;}

//    public int getDepositsPaymentsCount() {return depositsPaymentsCount;}
//    public void setDepositsPaymentsCount(int depositsPaymentsCount) {this.depositsPaymentsCount = depositsPaymentsCount;}

    public double getBalance() {return balance;}
    public void setBalance(double balance) {this.balance = balance;}


    public Month getMonth() {return month;}
    public void setMonth(Month month) {this.month = month;}


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MonthlyExpensePlannyngDto{")
                .append("month=").append(month != null ? month.getMonthYear() : "null")
                .append(", totalExpense=").append(String.format("%.2f", totalExpenseAmount))
                .append(", expenses=").append(expensesCount)
                .append(", payments=").append(paymentsCount)
                .append("}");
        return sb.toString();
    }


}
