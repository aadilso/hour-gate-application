package com.example.hourgate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.hourgate.adapter.EmployeesAdapter;
import com.example.hourgate.models.UserModel;
import com.example.hourgate.utils.Constants;
import com.example.hourgate.utils.Utils;
import com.example.hourgate.R;
import com.example.hourgate.databinding.ActivityEmployeesBinding;



import java.util.ArrayList;

// employees page for admin
public class Employees extends AppCompatActivity {

    ActivityEmployeesBinding binding;
    private ArrayList<UserModel> employeesModelArrayList; // Array List of type UserModel (Array list of employees)
    EmployeesAdapter employeesAdapter; // employeesAdapter object from the employees adapter class
    String Email_Pattern = "^[\\p{L}\\p{N}\\._%+-]+@[\\p{L}\\p{N}\\.\\-]+\\.[\\p{L}]{2,}$"; // https://howtodoinjava.com/java/regex/java-regex-validate-email-address/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadEmployees();
        clickListeners();
    }


    private void clickListeners() {
        // click listener for add employee button
        binding.BtnAddEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // invoke the showEmployeeAddingDialog method when add employee button is clicked
                showEmployeeAddingDialog();
            }
        });

        // on query text listener to get the filter method to be called every time text is changed in the search bar
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            // if text in search view changes call the filter method
            @Override
            public boolean onQueryTextChange(String s) {
                filter(s);
                return false;
            }
        });
    }

    // method for loading all employees (so that all employees can be seen on screen)

    private void loadEmployees() {
        // initialise empty array list of type UserModel
        employeesModelArrayList = new ArrayList<>();
        // access the users collection in the database
        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // if the users collections is not empty for each employee add the employee to our array list
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        employeesModelArrayList.add(userModel);
                    }
                    // initialise employees adapter with our employees array list
                    employeesAdapter = new EmployeesAdapter(employeesModelArrayList, new EmployeesAdapter.EmployeeActionListener() {
                        @Override
                        // on delete button click invoke the deleteUser method for that employee
                        public void onDeleteButtonClicked(UserModel userModel) {
                            deleteUser(userModel);
                        }
                        // on clicking employee invoke openUserProfile method
                        @Override
                        public void onEmployeeClicked(UserModel userModel) {
                            openUserProfile(userModel);
                        }
                    });
                    // setting the employees recycler view to our employees adapter
                    binding.RecyclerviewEmployees.setAdapter(employeesAdapter);
                }
                // if the users collection is empty
                else {
                    new Utils().showShortToast(Employees.this, "No employees in the database");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Utils().showShortToast(Employees.this, e.getLocalizedMessage()); }
        });}

    // method for opening user profile -  not finished
    private void openUserProfile(UserModel userModel) {
        //new Utils().showShortToast(Employees.this, userModel.getName());
    }

    // method for deleting a employee
    private void deleteUser(UserModel userModel) {
        // accessing and deleting the employee in the database
        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).document(userModel.getEmail()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    // if employee is successfully deleted:
                    @Override
                    public void onSuccess(Void unused) {
                        // remove employee from our array list
                        employeesModelArrayList.remove(userModel);
                        // to notify our adapter that a employee has been removed to update the employees recycler view.
                        employeesAdapter.notifyDataSetChanged();
                        // inform the user that the employee has been removed
                        new Utils().showShortToast(Employees.this, "you removed " + userModel.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Utils().showShortToast(Employees.this, e.getLocalizedMessage());
            }
        });
    }

    /*
    method for filtering employees for search functionality, adapted from:
    https://stackoverflow.com/questions/24769257/custom-listview-adapter-with-filter-android
    */
    private void filter(String s) {
        // creating a new array list to filter our data.
        ArrayList<UserModel> filteredlist = new ArrayList<>();
        // running a for loop to compare elements.
        for (UserModel item : employeesModelArrayList) {
            // checking if the entered string matched with any item (employee) of our recycler view.
            if (item.getName().toLowerCase().contains(s.toLowerCase())) {
                // if the item is matched we add it to our filteredlist array.
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            // if no employee is found in filtered list inform the user
            Toast.makeText(this, "No Employee Found..", Toast.LENGTH_SHORT).show();
        } else {
            // at last we pass the filtered list to our adapter
            employeesAdapter.filterList(filteredlist);
        }
    }

    // method for showing the adding employee dialog
    private void showEmployeeAddingDialog() {
        // creating and customising dialog for adding a employee
        Dialog dialog = new Dialog(Employees.this, R.style.EmployeeAddingDialog);
        dialog.setContentView(R.layout.add_employee_dialog_layout);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.7f;
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        // linking buttons in the dialog to variables
        ImageButton Btn_Cancel = dialog.findViewById(R.id.Btn_Close_EmployeeDialog);
        Button Btn_Add = dialog.findViewById(R.id.Btn_Add_EMPLOYEEDIALOG);
        EditText Et_Name_Dialog = dialog.findViewById(R.id.Et_Name_Dialog);
        EditText Et_Email_Dialog = dialog.findViewById(R.id.Et_Email_Dialog);
        EditText Et_Password_Dialog = dialog.findViewById(R.id.Et_Password_Dialog);

        // click listener for add employee button in the dialog
        Btn_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // converting the text in the fields to strings and trimming any spaces at the end or start
                String newEmployeeName = Et_Name_Dialog.getText().toString().trim();
                String newEmployeeEmail = Et_Email_Dialog.getText().toString().trim();
                String newEmployeePass = Et_Password_Dialog.getText().toString();
                // validation
                if (TextUtils.isEmpty(newEmployeeName) || TextUtils.isEmpty(newEmployeeEmail) || TextUtils.isEmpty(newEmployeePass)) {
                    Toast.makeText(getApplicationContext(), "All Fields are Required", Toast.LENGTH_SHORT).show();
                } else if (!newEmployeeEmail.matches(Email_Pattern)) {
                    Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
                } else if (newEmployeePass.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password length should be at least 6 characters", Toast.LENGTH_SHORT).show();
                }
                // if validation is passed create the employee object and invoke the saveUser method to save the object to the database
                else {
                    UserModel employee = new UserModel(newEmployeeName, newEmployeeEmail, newEmployeePass);
                    saveUser(employee,dialog);
                }
            }
        });

        // click listener for the cancel button in the dialog
        Btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss(); // close the dialog
            }
        });
        dialog.show(); // ensure dialog is shown on screen
    }

    // method for saving a employee to the database
    private void saveUser(UserModel employee, Dialog dialog) {
        // adding the employee to the database
        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).document(employee.getEmail()).set(employee)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    // if employee is added successfully:
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss(); // close the dialog
                        // add new employee to our employees array list
                        employeesModelArrayList.add(employee);
                        // to notify our adapter that a employee has been added to update the employees recycler view.
                        employeesAdapter.notifyDataSetChanged();
                        // inform the user that the employee has been registered
                        new Utils().showShortToast(Employees.this, "Successfully Registered");
                    }
                })
                // if employee is not added successfully
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        new Utils().showShortToast(Employees.this, e.getLocalizedMessage());
                    }
                });
    }
}