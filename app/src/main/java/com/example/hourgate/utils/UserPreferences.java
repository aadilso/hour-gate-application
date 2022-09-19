package com.example.hourgate.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.example.hourgate.models.LoginModel;
import com.example.hourgate.models.UserModel;

/*
Methods adapted from https://stackoverflow.com/questions/33208205/how-to-store-json-object-to-shared-preferences
in the context of this application of course
 */



public class UserPreferences {

    // saving current user
    @SuppressLint("CommitPrefEdits")
    public void saveCurrentUser(Context context, UserModel model) {
        // Shared preferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences("userModel", 0);
        // Interface used for modifying values in a SharedPreferences object.
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        // Converting java object to json
        Gson gson = new Gson();
        String json = gson.toJson(model);
        // Set a String value in the preferences editor, to be written back once commit() or apply() are called.
        sharedPreferencesEditor.putString("userModel", json);
        // Commit preferences changes back from the Editor to the SharedPreferences object
        sharedPreferencesEditor.apply();
    }

    // getting current user
    @SuppressLint("CommitPrefEdits")
    public UserModel getCurrentUser(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("userModel", 0);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("userModel", "");

        // Converting JSON to Java object
        return gson.fromJson(json, UserModel.class);
    }

    // saving the login of user (admin or employee)
    @SuppressLint("CommitPrefEdits")
    public void saveLogin(Context context, LoginModel model) {
        // Shared preferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences("userModel", 0);
        // Interface used for modifying values in a SharedPreferences object.
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        // Converting java object to JSON
        Gson gson = new Gson();
        String json = gson.toJson(model);
        // Set a String value in the preferences editor, to be written back once commit() or apply() are called.
        sharedPreferencesEditor.putString("loginModel", json);
        // Commit preferences changes back from the Editor to the SharedPreferences object
        sharedPreferencesEditor.apply();
    }

    // getting the login of the user
    @SuppressLint("CommitPrefEdits")
    public LoginModel getLogin(Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences("userModel", 0);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("loginModel", "");

        // Converting JSON to Java object
        return gson.fromJson(json, LoginModel.class);
    }
}
