package com.example.test3.mapper;

import com.example.test3.month.Month;
import com.example.test3.monthly.expense.planning.MonthlyExpensePlanningDto;

import java.util.ArrayList;
import java.util.List;

public class MonthMapper {


    /** Реализация для MonthlyExpensePlanning : */
    public List<MonthlyExpensePlanningDto> getMonthlyExpensePlanningDtoList(List<Month> monthList) {

        List<MonthlyExpensePlanningDto> dtos = new ArrayList<>();
//        for (Month month : monthList) dtos.add(getMonthlyDto(month));

        return dtos;
    }


}
