package com.example.test3.meter;

public class Meter {

    private Long id;
    private Long monthId;
    private String name;
    private Double value;


    public Meter() {}

    public Meter(Long id, Long monthId, String name, Double value) {
        this.id = id;
        this.monthId = monthId;
        this.name = name;
        this.value = value;
    }


    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Long getMonthId() {return monthId;}
    public void setMonthId(Long monthId) {this.monthId = monthId;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public Double getValue() {return value;}
    public void setValue(Double value) {this.value = value;}


    @Override
    public String toString() {
        return "Meter{" +
                "id=" + id +
                ", monthId=" + monthId +
                ", name=" + name +
                ", value=" + value +
                '}';
    }


}
