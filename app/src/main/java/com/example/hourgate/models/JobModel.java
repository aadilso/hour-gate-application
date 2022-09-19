package com.example.hourgate.models;

public class JobModel {
    String userEmail;
    String userName;
    Long checkInTime;
    Long checkOutTime;
    SitesModel site;
    String day;
    String month;
    String year;

    public JobModel() {} // empty constructor for firebase

    // constructor for jobs which take in user email/username , day/month/year and check in and out time and a object - "site" from the SitesModel class
    public JobModel(String userEmail, String userName, String day, String month, String year, Long checkInTime, Long checkOutTime, SitesModel site) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.day = day;
        this.month = month;
        this.year = year;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.site = site;
    }

    // getters

    public String getUserEmail() { return userEmail; }

    public String getUserName() { return userName; }

    public Long getCheckInTime() { return checkInTime; }

    public Long getCheckOutTime() { return checkOutTime; }

    public SitesModel getSite() { return site; }

    public String getDay() { return day; }

    public String getMonth() { return month; }

    public String getYear() { return year; }



    // setters

    public void setUserEmail(String userEmail) { this.userEmail = userEmail;}

    public void setUserName(String userName) { this.userName = userName; }

    public void setCheckInTime(Long checkInTime) { this.checkInTime = checkInTime; }

    public void setCheckOutTime(Long checkOutTime) { this.checkOutTime = checkOutTime; }

    public void setSite(SitesModel site) { this.site = site; }

    public void setDay(String day) { this.day = day; }

    public void setMonth(String month) { this.month = month; }

    public void setYear(String year) { this.year = year; }


}
