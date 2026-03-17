package com.example.test3.month;

import java.io.Serializable;

/** Месяц, для сохранения итогов затрат по месяцу, в котором были совершены траты
 * и для прикрепления соответствующего им итогового возврата */
public class Month implements Serializable {

    public static final Long TYPE_MONTHLY_EXPENSES = 1L;                                            /** Константа для типа "Ежемесячные расходы" */
    public static final Long TYPE_METER_READINGS = 2L;                                              /** Константа для типа "Передача показаний" */

    private Long id;
    private Long typeId;
    private Integer year;
    private Integer month;                                                                          /** 1-12 */
    private String monthYear;                                                                       /** Март 2026 */


    public Month() {}


    public Month(int year, int month, Long typeId) {
        this.year = year;
        this.month = month;
        this.typeId = typeId;
        this.monthYear = getMonthName(month) + " " + year;
    }


    private String getMonthName(int month) {

        String[] monthNames = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        return monthNames[month - 1];
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getYear() { return year; }
    public void setYear(int year) {
        this.year = year;
        updateMonthYear();
    }

    public int getMonth() { return month; }
    public void setMonth(int month) {
        this.month = month;
        updateMonthYear();
    }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    private void updateMonthYear() {
        if(this.year != null && this.month != null) this.monthYear = getMonthName(month) + " " + year;
    }


    public Long getTypeId() {return typeId;}
    public void setTypeId(Long typeId) {this.typeId = typeId;}

    public void setYear(Integer year) {this.year = year;}

    public void setMonth(Integer month) {this.month = month;}


    @Override
    public String toString() {
        return monthYear;
    }


}