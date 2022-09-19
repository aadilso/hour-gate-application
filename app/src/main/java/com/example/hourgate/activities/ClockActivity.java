package com.example.hourgate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.example.hourgate.models.JobModel;
import com.example.hourgate.models.SitesModel;
import com.example.hourgate.models.UserModel;
import com.example.hourgate.utils.Constants;
import com.example.hourgate.utils.UserPreferences;
import com.example.hourgate.utils.Utils;
import com.example.hourgate.R;
import com.example.hourgate.databinding.ActivityClockBinding;

import java.util.ArrayList;

// Clock in and out activity for employees

public class ClockActivity extends AppCompatActivity {

    ActivityClockBinding binding;
    UserModel userModel;
    SitesModel site;
    Double myLat;
    Double myLng;
    ArrayAdapter<String> employee_adapter;
    ArrayList<String> Site_Names = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialisation();
        loadSites();
        clickListener();
    }

// all click listeners are within this method

    private void clickListener() {

        // click listener for the Check In Button
        binding.BtnCheckInClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // creating (and some further customisation) dialog popup when Clock In Button is clicked
                // https://stackoverflow.com/questions/6257332/custom-screen-dim-with-dialog
                Dialog dialog = new Dialog(ClockActivity.this, R.style.EmployeeAddingDialog);
                dialog.setContentView(R.layout.clockin_dialog_layout);
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.dimAmount = 0.7f;
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                // linking dialog buttons to variables, dialog has "cancel" and "yes" options
                Button Btn_Cancel = dialog.findViewById(R.id.Btn_cancel_ClockIN);
                Button Btn_Yes = dialog.findViewById(R.id.Btn_Yes_ClockIN);

                // when you select "Yes" invoke checkIfAlreadyCheckedIn method
                Btn_Yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkIfAlreadyCheckedIn(dialog);
                    }
                });
                // when you click "cancel" close the dialog
                Btn_Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show(); // ensure the dialog shows on the screen
            }
        });

        // click listener for the Check Out button
        binding.BtnCheckOutClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // creating (and some further customisation) dialog popup when Clock Out Button is clicked
                // https://stackoverflow.com/questions/6257332/custom-screen-dim-with-dialog
                Dialog dialog = new Dialog(ClockActivity.this, R.style.EmployeeAddingDialog);
                dialog.setContentView(R.layout.clockout_dialog_layout);
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.dimAmount = 0.7f;
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                // linking dialog buttons to variables, dialog has "cancel" and "yes" options
                Button Btn_Cancel = dialog.findViewById(R.id.Btn_cancel_CLOCKOUT);
                Button Btn_Yes = dialog.findViewById(R.id.Btn_Yes_CLOCKOUT);

                // when you select "Yes" close the dialog and invoke the checkIfAlreadyCheckedOut method
                Btn_Yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        checkIfAlreadyCheckedOut(dialog);
                    }
                });
                // when you click "cancel" close the dialog
                Btn_Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show(); // ensure the dialog shows on the screen
            }
        });

        // click listener for the back button
        binding.BtnBackClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // close the activity hence redirect to last activity on the stack
            }
        });
    }


    // method for checking if employee has checked out already
    private void checkIfAlreadyCheckedOut(Dialog dialog) {

        // getting day and year
        String todayDate = new Utils().getDayFromMillis(System.currentTimeMillis());
        String currentYear = new Utils().getYearFromMillis(System.currentTimeMillis());


        // accessing the employees jobs on the day from the database
        FirebaseFirestore.getInstance().collection(Constants.REPORTS_COLLECTIONS + currentYear).document(userModel.getEmail())
                .collection(Constants.JOBS).document(todayDate).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // if there is a job instance on the day in the database:
                if (documentSnapshot.exists()) {
                    dialog.dismiss();
                    JobModel jobModel = documentSnapshot.toObject(JobModel.class);
                    assert jobModel != null;
                    // if a check in was made on the day and not a check out, check employee out and save job details by invoking loadSiteDetails method
                    if (jobModel.getCheckOutTime().equals(0L)) {
                        loadSiteDetails("CheckedOut", jobModel);
                    }
                    // if a check in was made on the day and a check out was already made then inform the employee they already checked out
                    else {
                        new Utils().showShortToast(ClockActivity.this, "You have Already Checked-Out");
                    }
                }
                // if there is no job instance on the day then employee has not yet checked in successfully
                else {
                    dialog.dismiss();
                    new Utils().showShortToast(ClockActivity.this, "Please check-in first");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Utils().showShortToast(ClockActivity.this, e.getLocalizedMessage());
            }
        });

    }

    // method for saving employee job details to a jobModel object when they check in and out

    private void loadSiteDetails(String type, JobModel model) {

        // getting the site selected on the spinner from the database
        FirebaseFirestore.getInstance().collection(Constants.SITES_COLLECTION).document(binding.sitesSpinner.getSelectedItem().toString()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // if site exists in the database:
                        if (documentSnapshot.exists()) {
                            site = documentSnapshot.toObject(SitesModel.class);
                            assert site != null;

                            // if site exists and employee is within the site range:
                            if (userIsNearSiteLocation(site)) {
                                // getting current times
                                Long currentTime = System.currentTimeMillis();
                                String day = new Utils().getDayFromMillis(currentTime);
                                String month = new Utils().getMonthFromMillis(currentTime);
                                String year = new Utils().getYearFromMillis(System.currentTimeMillis());
                                // if user is checking out the current time is the check out time and invoke the checkOutToday method
                                if (type.equals("CheckedOut")) {
                                    model.setCheckOutTime(currentTime);
                                    checkOutToday(model);
                                }
                                // if user is checking in create a new job model; setting the check in time as current time and the check out time as empty (0L)
                                // and invoke the checkInToday method
                                else {
                                    JobModel jobModel = new JobModel(userModel.getEmail(), userModel.getName(), day, month, year, currentTime, 0L, site);
                                    checkInToday(jobModel);
                                }
                            }
                            // if user is not within the site range then inform them
                            else {
                                new Utils().showShortToast(ClockActivity.this, "You are not within the site range!");
                            }
                        }
                        // if site cannot be found
                        else {
                            new Utils().showShortToast(ClockActivity.this, "Site not found");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Utils().showShortToast(ClockActivity.this, e.getLocalizedMessage());
            }
        });

    }

// methods for saving employee job details to the database upon successfully checking out

    private void checkOutToday(JobModel jobModel) {

        // accessing and saving the job details in the employees jobs collection
        FirebaseFirestore.getInstance().collection(Constants.REPORTS_COLLECTIONS + jobModel.getYear()).document(jobModel.getUserEmail())
                .collection(Constants.JOBS).document(jobModel.getDay()).set(jobModel, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            // if job details are successfully saved to the database inform the employee
            @Override
            public void onSuccess(Void unused) {
                new Utils().showShortToast(ClockActivity.this, "Checked Out Successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Utils().showShortToast(ClockActivity.this, e.getLocalizedMessage());
            }
        });

    }


    // initialising
    private void initialisation() {
        // Creating the ArrayAdapter instance
        employee_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Site_Names);
        employee_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        userModel = new UserPreferences().getCurrentUser(ClockActivity.this);
        myLat = getIntent().getDoubleExtra("lat", 0);
        myLng = getIntent().getDoubleExtra("lng", 0);

        Log.e("myLat", myLat.toString());
        Log.e("myLng", myLng.toString());
    }

    // method for checking if employee has checked in already

    private void checkIfAlreadyCheckedIn(Dialog dialog) {

        // getting the date and year
        String todayDate = new Utils().getDayFromMillis(System.currentTimeMillis());
        String currentYear = new Utils().getYearFromMillis(System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection(Constants.REPORTS_COLLECTIONS + currentYear).document(userModel.getEmail())
                .collection(Constants.JOBS).document(todayDate).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // if a job model on the day exists, the employee has already checked in:
                if (documentSnapshot.exists()) {
                    dialog.dismiss();
                    new Utils().showShortToast(ClockActivity.this, "You have Already Checked-In");
                }
                // if a job model does not exist the employee has not checked in; invoke the loadSiteDetails methods:
                else {
                    dialog.dismiss();
                    loadSiteDetails("CheckedIn", new JobModel());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Utils().showShortToast(ClockActivity.this, e.getLocalizedMessage());
            }
        });
    }

    // method for checking employee is within range of the site to check in and out

    private boolean userIsNearSiteLocation(SitesModel siteModel) {
        Double distance = new Utils().getDistanceFromLatLonInKm(myLat, myLng, siteModel.getLat(), siteModel.getLng());
        Log.e("distance", distance.toString());
        return distance < 1.6; // distance from site needs to be less than 1.6km i.e 1 mile
    }

    // methods for saving the employees job details of a on going job to the database upon successfully checking in

    private void checkInToday(JobModel jobModel) {
        // accessing and saving the job details of the ongoing job in the employees jobs collection
        FirebaseFirestore.getInstance().collection(Constants.REPORTS_COLLECTIONS + jobModel.getYear()).document(jobModel.getUserEmail())
                .collection(Constants.JOBS).document(jobModel.getDay()).set(jobModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    // if job details of the ongoing job is successfully saved to the database inform the employee
                    @Override
                    public void onSuccess(Void unused) {
                        new Utils().showShortToast(ClockActivity.this, "Checked-in Successfully");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Utils().showShortToast(ClockActivity.this, e.getLocalizedMessage());
            }
        });
    }

    // loading sites onto the spinner

    private void loadSites() {

        FirebaseFirestore.getInstance().collection(Constants.SITES_COLLECTION).orderBy("addedTime", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        // if sites collection is not empty:
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // for each site in the sites collection add it to the spinner
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                SitesModel model = documentSnapshot.toObject(SitesModel.class); // each documentSnapshot is a site object from the sitesModel class
                                assert model != null;
                                Site_Names.add(model.getName());
                            }
                            binding.sitesSpinner.setAdapter(employee_adapter);
                        }
                        else {

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ClockActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}