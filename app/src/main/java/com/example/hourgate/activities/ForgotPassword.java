package com.example.hourgate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hourgate.models.UserModel;
import com.example.hourgate.utils.Constants;
import com.example.hourgate.utils.Utils;
import com.example.hourgate.R;
import com.example.hourgate.databinding.ActivityForgotPasswordBinding;

import java.util.Locale;

// forgot password page
public class ForgotPassword extends AppCompatActivity {

    ActivityForgotPasswordBinding binding;
    String Email_Pattern ="^[\\p{L}\\p{N}\\._%+-]+@[\\p{L}\\p{N}\\.\\-]+\\.[\\p{L}]{2,}$"; // https://howtodoinjava.com/java/regex/java-regex-validate-email-address/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // click listener for get forgotten password button
        binding.BtnForgotPasswordSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // converting the text in the fields to strings and trimming any spaces on the end or start
                String forgotPasswordEmail = binding.fpEmail.getText().toString().trim();
                String forgotPasswordName = binding.fpName.getText().toString().trim();
                // validation
                if(TextUtils.isEmpty(forgotPasswordEmail)||TextUtils.isEmpty(forgotPasswordName)){
                    Toast.makeText(getApplicationContext(), "Both Fields are Required", Toast.LENGTH_SHORT).show();
                }else if (!forgotPasswordEmail.matches(Email_Pattern)){
                    Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
                }
                // if validation is passed:
                else {
                    // for admin we don't want to allow this feature
                    if(forgotPasswordEmail.equals("hourgate@admin.com")){
                        Toast.makeText(ForgotPassword.this, "This feature is not for Admins", Toast.LENGTH_SHORT).show();
                    }
                    // if not admin and a employee invoke checkIfEmailExists method
                    else {
                        checkIfEmailExists(forgotPasswordEmail,forgotPasswordName);
                    }
                }
            }
        });
    }

    // method for checking if entered email and subsequent name exists in the database

    private void checkIfEmailExists(String fpEmail, String fpName) {
        // read call to the database with the the email the user enters
        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).document(fpEmail).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        // if email exists in the database:
                        if(documentSnapshot.exists()){

                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
                            assert userModel != null;
                            // if entered name is the same as the name in the database show user their password
                            if(userModel.getName().toLowerCase(Locale.ROOT).equals(fpName.toLowerCase(Locale.ROOT))){
                                // creating and customising dialog
                                Dialog dialog = new Dialog(ForgotPassword.this, R.style.EmployeeAddingDialog);
                                dialog.setContentView(R.layout.dialog_password);
                                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                                lp.dimAmount = 0.7f;
                                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                                Button Btn_Cancel = dialog.findViewById(R.id.closeBtn);
                                TextView textView = dialog.findViewById(R.id.tvPassword);

                                // get users password and show the password through the dialog text view
                                textView.setText("The password for email \n\""+ fpEmail +"\"\n is\n "+ "\""+ userModel.getPassword()+"\"");

                                // on click listener for the cancel button
                                Btn_Cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // close the dialog
                                        dialog.dismiss();
                                        finish();
                                    }
                                });

                                dialog.show(); // ensure dialog is shown on the screen

                            }
                            // if email is found but the entered name does not match that of the database inform the user
                            else {
                                Toast.makeText(ForgotPassword.this, "Name not matched", Toast.LENGTH_SHORT).show();
                            }
                        }
                        // if the email is not found inform the user no account exists with the email
                        else {
                            new Utils().showShortToast(ForgotPassword.this,"This email address is not registered");
                        }
                    }
                });
    }
}