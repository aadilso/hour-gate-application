package com.example.hourgate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.hourgate.models.UserModel;
import com.example.hourgate.utils.UserPreferences;
import com.example.hourgate.databinding.ActivityUserDataAdminBinding;

// admin my profile page
public class UserDataAdmin extends AppCompatActivity {

    private ActivityUserDataAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDataAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUserData();
    }

    // method for setting the admins details

    private void setUserData() {

        UserModel userModel = new UserPreferences().getCurrentUser(UserDataAdmin.this);

        if(userModel!=null){
            binding.EmailAdminData.setText(userModel.getEmail());
            binding.NameAdminData.setText(userModel.getName());
            binding.PasswordAdminData.setText(userModel.getPassword());
        }
    }
}