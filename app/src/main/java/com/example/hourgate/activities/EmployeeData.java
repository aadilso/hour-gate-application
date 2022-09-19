package com.example.hourgate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.hourgate.models.UserModel;
import com.example.hourgate.utils.UserPreferences;
import com.example.hourgate.databinding.ActivityEmployeeDataBinding;


// employee my profile page

public class EmployeeData extends AppCompatActivity {

    private ActivityEmployeeDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeeDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUserData();
    }

    // method for setting the employees details

    private void setUserData() {
        UserModel userModel = new UserPreferences().getCurrentUser(EmployeeData.this);
        if(userModel!=null){
            binding.EmailEmployeeData.setText(userModel.getEmail());
            binding.NameEmployeeData.setText(userModel.getName());
            binding.PasswordEmployeeData.setText(userModel.getPassword());
        }
    }
}