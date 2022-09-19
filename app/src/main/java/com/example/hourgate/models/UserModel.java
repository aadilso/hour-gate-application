package com.example.hourgate.models;

import com.google.firebase.firestore.DocumentId;

public class UserModel {

    public UserModel() {} // empty constructor will be needed for firebase

    @DocumentId
    private String id;
    private String name;
    private String email;
    private String password;

    // constructor for a user which takes in a name,email and password
    public UserModel(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // getters

    public String getId() {
        return id;
    }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    // setters

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}