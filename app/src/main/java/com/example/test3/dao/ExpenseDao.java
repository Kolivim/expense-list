package com.example.test3.dao;

import com.example.test3.expenseList.Expense;

public interface ExpenseDao {

    void insertExpense(Expense expense);

    Expense selectExpense(int id);

    void updatePerson(Expense expense, int id);

    void deleteExpense(int id);

}
