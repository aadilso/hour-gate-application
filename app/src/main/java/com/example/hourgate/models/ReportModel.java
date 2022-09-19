package com.example.hourgate.models;

import java.io.Serializable;

public class ReportModel implements Serializable {

    String name;
    float totalWages;
    float hourWorked;

    public ReportModel() {} // empty constructor needed for firebase

    // constructor for reports which take in name, total wages and hours worked
    public ReportModel(String name, float totalWages, float hourWorked) {
        this.name = name;
        this.totalWages = totalWages;
        this.hourWorked = hourWorked;
    }

    // getters

    public String getName() { return name; }

    public float getTotalWages() {
        return totalWages;
    }

    public float getHourWorked() {
        return hourWorked;
    }

    // setters

    public void setName(String name) {
        this.name = name;
    }

    public void setTotalWages(float totalWages) {
        this.totalWages = totalWages;
    }

    public void setHourWorked(float hourWorked) {
        this.hourWorked = hourWorked;
    }
}
