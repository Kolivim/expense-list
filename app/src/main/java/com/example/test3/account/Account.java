package com.example.test3.account;

import java.time.ZonedDateTime;
import java.util.ArrayList;

/** Счёт / банк источник финансирования */
public class Account {

    private Long id;                                                                                /** Необходимо для DAO */

    /** expenseId либо depositId, в зависимости от типа */
    private Long parentId;
    /** Тип Аккаунта - 0 либо 1 (0 это expense, 1 это deposit) */
    private Long type;
    /** Может быть как наименование счёта, так и наименование банка-эмитента карты */
    private String name;
    /** Номер счёта */
    private String number;


    public Account() {}


    public Account(Long id, Long parentId, Long type, String name, String number) {
        this.id = id;
        this.parentId = parentId;
        this.type = type;
        this.name = name;
        this.number = number;
    }


    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Long getParentId() {return parentId;}
    public void setParentId(Long parentId) {this.parentId = parentId;}

    public Long getType() {return type;}
    public void setType(Long type) {this.type = type;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getNumber() {return number;}
    public void setNumber(String number) {this.number = number;}


    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", type=" + type +
                ", name=" + name +
                ", number=" + number +
                '}';
    }

}
