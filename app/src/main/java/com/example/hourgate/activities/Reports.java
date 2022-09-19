package com.example.hourgate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.hourgate.models.JobModel;
import com.example.hourgate.models.ReportModel;
import com.example.hourgate.models.SitesModel;
import com.example.hourgate.models.UserModel;
import com.example.hourgate.utils.Constants;
import com.example.hourgate.utils.Utils;
import com.example.hourgate.databinding.ActivityReportsBinding;

import java.util.ArrayList;

public class Reports extends AppCompatActivity {

    ActivityReportsBinding binding;
    ArrayList<String> Employees = new ArrayList<>();
    ArrayList<ReportModel> reports = new ArrayList<>();
    ArrayAdapter<String> employee_adapter;
    String year = new Utils().getYearFromMillis(System.currentTimeMillis());
    String[] TimePeriod = {"Whole Year", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadEmployees();

        //Creating the ArrayAdapter instance containing the employees
        employee_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Employees);
        employee_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Creating the ArrayAdapter instance containing the months
        ArrayAdapter<String> timePeriod_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TimePeriod);
        timePeriod_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Setting the ArrayAdapter data on the Spinner
        binding.timePeriodSpinner.setAdapter(timePeriod_adapter);



        // on click listener for the create reports button
        binding.BtnCreateReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reports = new ArrayList<>();

                // if user selects Whole Year and All employees from the spinners invoke the loadAllEmployeesAllWages method
                if (binding.timePeriodSpinner.getSelectedItem().toString().equals("Whole Year")
                        && binding.employeeSpinner.getSelectedItem().toString().equals("All Employees")) {
                    loadAllEmployeesAllMonths();
                }
                // if user selects a specific month but still selects all employees from the spinners invoke loadSelectedMonthAllEmployees method
                if (!binding.timePeriodSpinner.getSelectedItem().toString().equals("Whole Year")
                        && binding.employeeSpinner.getSelectedItem().toString().equals("All Employees"))
                {
                    loadSelectedMonthAllEmployees(binding.timePeriodSpinner.getSelectedItem().toString());
                }
                // if user selects Whole Year but selects a specific employee from the spinners invoke loadAllMonthSelectedEmployee method
                if (binding.timePeriodSpinner.getSelectedItem().toString().equals("Whole Year")
                        && !binding.employeeSpinner.getSelectedItem().toString().equals("All Employees"))
                {
                    loadAllMonthSelectedEmployee(binding.employeeSpinner.getSelectedItem().toString());
                }
                // if user selects a both a specific month and a specific employee from the spinner invoke loadSelectedMonthSelectedEmployee method
                if (!binding.timePeriodSpinner.getSelectedItem().toString().equals("Whole Year")
                        && !binding.employeeSpinner.getSelectedItem().toString().equals("All Employees"))
                {
                    loadSelectedMonthSelectedEmployee(binding.employeeSpinner.getSelectedItem().toString(), binding.timePeriodSpinner.getSelectedItem().toString());
                }
            }
        });
    }

    // method for loading a specific employee job details for a specific month
    private void loadSelectedMonthSelectedEmployee(String selectedEmployee, String selectedMonth) {

        // from the database accessing the selected employee
        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).whereEqualTo("name", selectedEmployee).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        // if employee does not exist in database inform the user
                        if (queryDocumentSnapshots.isEmpty()) {
                            new Utils().showShortToast(Reports.this, "Employee not found");
                        }
                        // if the name does exist in the database:
                        else {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                                assert userModel != null;

                                String year = new Utils().getYearFromMillis(System.currentTimeMillis());

                                // from the database - for the selected employee accessing there jobs in the selected month from the reports collection of that year
                                FirebaseFirestore.getInstance().collection(Constants.REPORTS_COLLECTIONS + year)
                                        .document(userModel.getEmail()).collection(Constants.JOBS).whereEqualTo("month", selectedMonth).get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                // if the employee has worked in the selected month then
                                                if (!queryDocumentSnapshots.isEmpty()) {

                                                    float totalHoursWorked = 0;
                                                    float totalWages = 0;

                                                    // for each job which is in the selected month:
                                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {

                                                        JobModel jobModel = document.toObject(JobModel.class); //each "document" is a job object from the jobModel class
                                                        assert jobModel != null;

                                                        // calculating total hours worked and subsequent total wages
                                                        float hoursWorked = new Utils().getHoursWorked(jobModel.getCheckInTime(), jobModel.getCheckOutTime());
                                                        float wage = hoursWorked * jobModel.getSite().getHourRate();
                                                        totalHoursWorked = totalHoursWorked + hoursWorked;
                                                        totalWages = totalWages + wage;

                                                        // logging
                                                        Log.e("day", jobModel.getDay() + "");
                                                        Log.e("hoursWorked", hoursWorked + "");
                                                        Log.e("wage", wage + "");
                                                    }
                                                    // logging
                                                    Log.e("user", userModel.getName());
                                                    Log.e("wages", totalWages + "");
                                                    Log.e("hours", totalHoursWorked + "");
                                                    // creating a report object from the information we calculated and adding it to our reports array list
                                                    reports.add(new ReportModel(userModel.getName(), totalWages, totalHoursWorked));
                                                }
                                                // if employee has not worked in the specified month
                                                else {
                                                    //logging
                                                    Log.e("empty user", userModel.getName());
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        new Utils().showShortToast(Reports.this, e.getLocalizedMessage());
                                    }
                                });

                            }
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(Reports.this, ShowReportActivity.class);
                                    intent.putExtra("employee", selectedEmployee);
                                    intent.putExtra("time", selectedMonth);
                                    intent.putExtra("reports", reports);
                                    startActivity(intent);
                                }
                            }, 2500);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Reports.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // method for loading a specific employees jobs from all months
    private void loadAllMonthSelectedEmployee(String selectedEmployee) {

        // from the database accessing the selected employee
        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).whereEqualTo("name", selectedEmployee).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        // if employee does not exist in database inform the user
                        if (queryDocumentSnapshots.isEmpty()) {
                            new Utils().showShortToast(Reports.this, "Employee not found");
                        }
                        // if the name does exist in the database:
                        else {

                            UserModel userModel = queryDocumentSnapshots.getDocuments().get(0).toObject(UserModel.class);
                            assert userModel != null;

                            // from the database - for the selected employee accessing all there jobs for the reports collection of that year
                            FirebaseFirestore.getInstance().collection(Constants.REPORTS_COLLECTIONS + year).document(userModel.getEmail())
                                    .collection(Constants.JOBS).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    // if the employee has worked in the specified month:
                                    if (!queryDocumentSnapshots.isEmpty()) {

                                        float totalHoursWorked = 0;
                                        float totalWages = 0;

                                        // for each job:
                                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {

                                            JobModel jobModel = document.toObject(JobModel.class); //each "document" is a job object from the jobModel class
                                            assert jobModel != null;
                                            // calculating total hours worked and subsequent total wages
                                            float hoursWorked = new Utils().getHoursWorked(jobModel.getCheckInTime(), jobModel.getCheckOutTime());
                                            float wage = hoursWorked * jobModel.getSite().getHourRate();
                                            totalHoursWorked = totalHoursWorked + hoursWorked;
                                            totalWages = totalWages + wage;

                                            // logging
                                            Log.e("day", jobModel.getDay() + "");
                                            Log.e("hourWord", hoursWorked + "");
                                            Log.e("wage", wage + "");
                                        }

                                        // logging
                                        Log.e("user", userModel.getName());
                                        Log.e("total wages", totalWages + "");
                                        Log.e("total hours", totalHoursWorked + "");
                                        Log.e("", "");

                                        // creating a report object from the information we calculated and adding it to our reports array list
                                        reports.add(new ReportModel(userModel.getName(), totalWages, totalHoursWorked));
                                    }
                                    // if the employee has yet not worked in the year:
                                    else {
                                        // logging
                                        Log.e("empty user", userModel.getName());
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    new Utils().showShortToast(Reports.this, e.getLocalizedMessage());
                                }
                            });
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("reports-abc", reports.size() + "");
                                    Intent intent = new Intent(Reports.this, ShowReportActivity.class);
                                    intent.putExtra("employee", selectedEmployee);
                                    intent.putExtra("time", "Whole Year");
                                    intent.putExtra("reports", reports);
                                    startActivity(intent);
                                }
                            }, 2500);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Reports.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // method to load a specific months jobs for all employees
    private void loadSelectedMonthAllEmployees(String selectedMonth) {

        // accessing the users collection from the database
        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                // if there are no employees inform the user
                if (queryDocumentSnapshots.isEmpty()) {
                    new Utils().showShortToast(Reports.this, "Employees not found");
                }
                // if there are employees
                else {
                    // for each employee:
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                        UserModel userModel = documentSnapshot.toObject(UserModel.class); // each documentSnapshot is a user object from the userModel class
                        assert userModel != null;

                        String year = new Utils().getYearFromMillis(System.currentTimeMillis());
                        // from the database accessing employee jobs for that month in the reports collection of that year
                        FirebaseFirestore.getInstance().collection(Constants.REPORTS_COLLECTIONS + year).document(userModel.getEmail())
                                .collection(Constants.JOBS).whereEqualTo("month", selectedMonth).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        // if the employee has worked in the specified month:
                                        if (!queryDocumentSnapshots.isEmpty()) {

                                            float totalHoursWorked = 0;
                                            float totalWages = 0;

                                            // for each job in the specified month:
                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {

                                                JobModel jobModel = document.toObject(JobModel.class); // each "document" is a job object from the JobClass
                                                assert jobModel != null;
                                                // calculating total hours worked and subsequent total wages
                                                float hourWorked = new Utils().getHoursWorked(jobModel.getCheckInTime(), jobModel.getCheckOutTime());
                                                float wage = hourWorked * jobModel.getSite().getHourRate();
                                                totalHoursWorked = totalHoursWorked + hourWorked;
                                                totalWages = totalWages + wage;
                                                // logging
                                                Log.e("day", jobModel.getDay() + "");
                                                Log.e("hourWord", hourWorked + "");
                                                Log.e("wage", wage + "");
                                            }
                                            //logging

                                            Log.e("user", userModel.getName());
                                            Log.e("wages", totalWages + "");
                                            Log.e("hours", totalHoursWorked + "");
                                            // creating a report object from the information we calculated and adding it to our reports array list
                                            reports.add(new ReportModel(userModel.getName(), totalWages, totalHoursWorked));
                                        }
                                        // if the employee does not have any completed jobs that month:
                                        else {
                                            // logging
                                            Log.e("empty user", userModel.getName());
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                new Utils().showShortToast(Reports.this, e.getLocalizedMessage());
                            }
                        });

                    }


                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Reports.this, ShowReportActivity.class);
                            intent.putExtra("employee", "All Employees");
                            intent.putExtra("time", selectedMonth);
                            intent.putExtra("reports", reports);
                            startActivity(intent);
                        }
                    },2500);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Reports.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // method for loading all employees wages for the whole year
    private void loadAllEmployeesAllMonths() {
        // accessing the user collection from the database
        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                // if no employees inform the user
                if (queryDocumentSnapshots.isEmpty()) {
                    new Utils().showShortToast(Reports.this, "Employees not found");
                }
                // if there are employees
                else {
                    // for each employee:
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                        UserModel userModel = documentSnapshot.toObject(UserModel.class);

                        assert userModel != null;

                        // accessing the employees completed jobs from the reports collection of that year
                        FirebaseFirestore.getInstance().collection(Constants.REPORTS_COLLECTIONS + year)
                                .document(userModel.getEmail()).collection(Constants.JOBS).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        // if the employee has worked in the year:
                                        if (!queryDocumentSnapshots.isEmpty()) {

                                            float totalHourWorked = 0;
                                            float totalWages = 0;

                                            // for each job:
                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {

                                                JobModel jobModel = document.toObject(JobModel.class); // a "document" is a job object from the jobModel class
                                                assert jobModel != null;
                                                // calculating employees total hours worked and subsequent total wages
                                                float hourWorked = new Utils().getHoursWorked(jobModel.getCheckInTime(), jobModel.getCheckOutTime());
                                                float wage = hourWorked * jobModel.getSite().getHourRate();
                                                totalHourWorked = totalHourWorked + hourWorked;
                                                totalWages = totalWages + wage;

                                                // logging
                                                Log.e("day", jobModel.getDay() + "");
                                                Log.e("hourWord", hourWorked + "");
                                                Log.e("wage", wage + "");

                                            }
                                            // logging
                                            Log.e("user", userModel.getName());
                                            Log.e("total wages", totalWages + "");
                                            Log.e("total hours", totalHourWorked + "");
                                            Log.e("", "");

                                            // // creating a report object from the information we calculated and adding it to our reports array list
                                            reports.add(new ReportModel(userModel.getName(), totalWages, totalHourWorked));
                                        }
                                        // if the employee has not worked in the year so far:
                                        else {
                                            // logging
                                            Log.e("empty user", userModel.getName());
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                new Utils().showShortToast(Reports.this, e.getLocalizedMessage());
                            }
                        });
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("reports-abc", reports.size() + "");
                            Intent intent = new Intent(Reports.this, ShowReportActivity.class);
                            intent.putExtra("employee", "All Employees");
                            intent.putExtra("time", "Whole Year");
                            intent.putExtra("reports", reports);
                            startActivity(intent);
                        }
                    }, 2500);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Reports.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // method for loading employees on to the employee spinner

    private void loadEmployees() {

        Employees.add("All Employees");
        FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        SitesModel model = documentSnapshot.toObject(SitesModel.class);
                        assert model != null;
                        Employees.add(model.getName());
                    }
                    binding.employeeSpinner.setAdapter(employee_adapter);
                }
                else {

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Reports.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}