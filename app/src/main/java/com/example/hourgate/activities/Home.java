package com.example.hourgate.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.hourgate.models.LoginModel;
import com.example.hourgate.utils.UserPreferences;
import com.example.hourgate.databinding.ActivityHomeBinding;

// home page for admin main menu
public class Home extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // click listener for employees button
        binding.BtnEmployees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Employees.class));
            }
        });

        // click listener for sites button
        binding.BtnSites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Sites.class));
            }
        });

        // click listener for reports button
        binding.BtnReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Reports.class));
            }
        });


        // click listener for My Profile button
        binding.BtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), UserDataAdmin.class));
            }
        });

        // click listener for logout button
        binding.BtnLogout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                LoginModel loginModel = new LoginModel("Admin",false);
                new UserPreferences().saveLogin(Home.this,loginModel);
                gotoLoginScreen();
            }
        });
    }

    // method for redirecting user to login screen
    private void gotoLoginScreen() {
        Intent intent = new Intent(Home.this,Login.class);
        startActivity(intent);
        finish();
    }
}