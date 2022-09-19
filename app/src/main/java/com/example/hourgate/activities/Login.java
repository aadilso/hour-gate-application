package com.example.hourgate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hourgate.models.LoginModel;
import com.example.hourgate.models.UserModel;
import com.example.hourgate.utils.Constants;
import com.example.hourgate.utils.UserPreferences;
import com.example.hourgate.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {

    ActivityLoginBinding binding;
    Context context;
    String userType;
    String Email_Pattern = "^[\\p{L}\\p{N}\\._%+-]+@[\\p{L}\\p{N}\\.\\-]+\\.[\\p{L}]{2,}$"; // https://howtodoinjava.com/java/regex/java-regex-validate-email-address/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = Login.this;

        LoginModel loginModel = new LoginModel();

        // getting the login state from our user preferences class
        loginModel = new UserPreferences().getLogin(context);

        // on activity creation if login model is not null display the appropriate screen depending on the saved login model type ("admin" or "employee")
        if(loginModel!=null){
            if(loginModel.getLoggedIn()){
                if(loginModel.getType().equals("Admin")){
                    startAdminHome();
                }else {
                    startEmployeeHome();
                }
                finish();
            }
        }

        // click listener for the login button
        binding.BtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // converting the text in the fields to strings and trimming any spaces on the end or start
                String email = binding.EmailLogin.getText().toString().trim(); // .trim just removes end or start space
                String pass = binding.PasswordLogin.getText().toString().trim();

                // various forms of validation
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                    Toast.makeText(getApplicationContext(), "All Fields are Required", Toast.LENGTH_SHORT).show();
                } else if (!email.matches(Email_Pattern)) {
                    Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
                } else if (pass.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password length should be at least 6 characters", Toast.LENGTH_SHORT).show();
                }
                // if validation is passed call checkUserType method
                else {
                    checkUserType(email, pass);
                }
            }
        });

        // forgot password click listener
        binding.ForgotPasswordLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
            }
        });
    }

    // method for checking employee login

    private void checkUserType(String employee_email, String employee_pass) {

        //ProgressbarLogin is a progress bar which will become visible
        binding.ProgressbarLogin.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).document(employee_email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // if the typed email exists in the users collection:
                        if (documentSnapshot.exists()) {
                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
                            assert userModel != null;
                            // if the typed password matches the corresponding password for the email in the database log the employee in
                            if (userModel.getPassword().equals(employee_pass)) {
                                binding.ProgressbarLogin.setVisibility(View.GONE); // remove progress bar
                                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show();
                                Constants.USER_TYPE = "Employee";

                                LoginModel loginModel = new LoginModel("Employee",true);
                                new UserPreferences().saveLogin(context,loginModel);
                                new UserPreferences().saveCurrentUser(context,userModel);
                                startEmployeeHome();
                            }
                            // if the typed password does not match the corresponding password for the email in the database
                            else {
                                binding.ProgressbarLogin.setVisibility(View.GONE); // remove progress bar
                                Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show();
                            }
                        }
                        // if the typed email is not in the users collection invoke the checkAdminLogin method
                        else {
                            checkAdminLogin(employee_email,employee_pass);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.ProgressbarLogin.setVisibility(View.GONE);
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // method for checking admin login
    private void checkAdminLogin(String admin_email, String admin_pass) {
        FirebaseFirestore.getInstance().collection(Constants.ADMIN_COLLECTION).document(admin_email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // if typed email exists in the admin collection:
                        if (documentSnapshot.exists()) {
                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
                            assert userModel != null;
                            // if the typed password matches the corresponding password for the email in the database log the admin in
                            if (userModel.getPassword().equals(admin_pass)) {
                                binding.ProgressbarLogin.setVisibility(View.GONE);
                                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show();
                                Constants.USER_TYPE = "Admin";

                                LoginModel loginModel = new LoginModel("Admin",true);
                                new UserPreferences().saveLogin(context,loginModel);
                                new UserPreferences().saveCurrentUser(context,userModel);

                                startAdminHome();
                            }
                            // if the typed password does not match the corresponding password for the email in the database
                            else {
                                binding.ProgressbarLogin.setVisibility(View.GONE);
                                Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show();
                            }
                        }
                        // if email does not exist in admin collection
                        else {
                            binding.ProgressbarLogin.setVisibility(View.GONE);
                            Toast.makeText(context, "Email address not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.ProgressbarLogin.setVisibility(View.GONE);
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // method for redirecting to admin main menu
    private void startAdminHome() {
        Intent homeIntent = new Intent(Login.this,Home.class);
        startActivity(homeIntent);
        finish();
    }

    // method for redirecting to employee main menu
    private void startEmployeeHome() {
        Intent homeIntent = new Intent(Login.this,EmployeeHome.class);
        startActivity(homeIntent);
        finish();
    }

}