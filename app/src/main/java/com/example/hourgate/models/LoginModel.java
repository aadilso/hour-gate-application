package com.example.hourgate.models;


public class LoginModel {

    String type;
    Boolean isLoggedIn;

    // constructor for the login model which will take in in type (employee or admin) and isloggedin boolean

    public LoginModel(String type, Boolean isLoggedIn) {
        this.type = type;
        this.isLoggedIn = isLoggedIn;
    }

    public LoginModel() { } // empty constructor which will be needed for firebase

    // getters

    public String getType() {
        return type;
    }

    public Boolean getLoggedIn() {
        return isLoggedIn;
    }

    // setters

    public void setType(String type) {this.type = type;}

    public void setLoggedIn(Boolean loggedIn) { isLoggedIn = loggedIn;}
}
