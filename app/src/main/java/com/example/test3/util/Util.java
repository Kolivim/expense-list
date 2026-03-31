package com.example.test3.util;

import java.time.format.DateTimeFormatter;

public class Util {

    public static final DateTimeFormatter dateFormatterInsert = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm Z");                                  /** "MM/dd/yyyy - HH:mm:ss Z" */
    public static final DateTimeFormatter dateFormatterSee = DateTimeFormatter.ofPattern("dd.MM.yy");

    public static final String EXTRA_EXPENSE_TYPE = "expense_type";


    /** MonthType : */
    /* public static final Long TYPE_MONTHLY_EXPENSES = 1L;  */ // <string name="expense_monthly">Ежемесячные расходы</string>
    //    public static final Long TYPE_METER_READINGS = 2L;    /** Константа для типа "Передача показаний" */
    public static final Long TYPE_MONTHLY_EXPENSE_PLANNYNG = 3L;                                    /** Константа для типа "Ежемесячное планирование расходов" */


    /** ExpenseType : */
    public static final Long TYPE_EXPENSE_MONTH_PLANNING = 2L;                                      /** Ежемесячное планирование бюджета */


    /** DepositType : */
    public static final Long TYPE_DEPOSIT_MONTH_PLANNING = 6L;                                      /** Ежемесячное планирование бюджета */
/*

Expense's
    <string name="expense_monthly_planning">Ежемесячное планирование бюджета</string>
    <string name="expense_long_credit_loans">Длинные займы с кредитных средств</string>
    <string name="expense_long_myself_loans">Длинные займы с собственных средств</string>
    <string name="expense_utility_bills">Коммунальные платежи</string>

Deposit's
<!--    "('Погашение ежемесячных затрат')-->
<!--    "('Ежемесячные взносы на кредитку')-->
<!--    "('Погашение затрат по длинным займам с кредитных средств')-->
<!--    "('Погашение затрат по длинным займам с собственных средств')-->
<!--    "('Планируемый возврат ежемесячных трат')-->


Month's
    public static final Long TYPE_MONTHLY_EXPENSES = 1L;   // <string name="expense_monthly">Ежемесячные расходы</string>
    public static final Long TYPE_METER_READINGS = 2L;    Константа для типа "Передача показаний"

*/


}
