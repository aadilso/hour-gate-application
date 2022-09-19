package com.example.hourgate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.hourgate.adapter.SitesAdapter;
import com.example.hourgate.models.SitesModel;
import com.example.hourgate.utils.Common;
import com.example.hourgate.utils.Constants;
import com.example.hourgate.utils.Utils;
import com.example.hourgate.R;
import com.example.hourgate.databinding.ActivitySitesBinding;

import java.util.ArrayList;

// sites activity for admin
public class Sites extends AppCompatActivity {

    ActivitySitesBinding binding;

    private SitesAdapter sitesAdapter;
    private ArrayList<SitesModel> sitesModelArrayList;
    private LatLng latLng = null;
    private TextView Et_Location_Dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySitesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clickListener();

        loadAllSites();
    }

    private void loadAllSites() {

        sitesModelArrayList = new ArrayList<>(); // a new empty array list

        FirebaseFirestore.getInstance().collection(Constants.SITES_COLLECTION).orderBy("addedTime", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        // if sites collections is not empty for each site add the site to our empty array (of course not empty anymore)
                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                SitesModel siteModel = documentSnapshot.toObject(SitesModel.class);
                                sitesModelArrayList.add(siteModel);
                            }

                            sitesAdapter = new SitesAdapter(sitesModelArrayList,
                                    new SitesAdapter.SiteActionListener() {
                                        // on delete button click invoke the deleteSite method for that specific site/sitesModel
                                        @Override
                                        public void siteDeleteClicked(SitesModel sitesModel) {
                                            deleteSite(sitesModel);
                                        }
                                        // on clicking a site invoke openSite method
                                        @Override
                                        public void siteOpenClicked(SitesModel sitesModel) {
                                            openSite(sitesModel);
                                        }
                                    });
                            binding.recyclerViewSites.setAdapter(sitesAdapter);
                        }else {

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Utils().showShortToast(Sites.this, e.getLocalizedMessage());
            }
        });
    }

    // method for deleting a site
    private void deleteSite(SitesModel sitesModel) {
        // accessing and deleting the site in the database
        FirebaseFirestore.getInstance().collection(Constants.SITES_COLLECTION).document(sitesModel.getName()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    // if site is successfully deleted remove site from our array adapter invoke the notifyDataSetChanged as well
                    @Override
                    public void onSuccess(Void unused) {
                        sitesModelArrayList.remove(sitesModel);
                        sitesAdapter.notifyDataSetChanged();
                        Toast.makeText(Sites.this, sitesModel.getName() + " site removed successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Sites.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // method for showing a sites locations
    private void openSite(SitesModel sitesModel) {
        // redirect to the maps activity which has a method for showing a sites location
        Intent intent = new Intent(Sites.this, MapsActivity.class);
        intent.putExtra("siteModel", sitesModel); // adding the sites data to the intent as well
        startActivity(intent);
    }

    private void clickListener() {

        // click listener for add sites button
        binding.BtnAddSites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSitesAddingDialog();
            }
        });

        // click listener for sites search view
        binding.searchViewSites.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // when text changes in search field:
            @Override
            public boolean onQueryTextChange(String newText) {
                // inside on query text change method we are calling the method "filter" to filter our recycler view.
                filter(newText);
                return false;
            }
        });
    }

    /*
    method for filtering sites for search functionality, adapted from:
    https://stackoverflow.com/questions/24769257/custom-listview-adapter-with-filter-android
    */

    private void filter(String text) {
        // creating a new array list to filter our data.
        ArrayList<SitesModel> filteredlist = new ArrayList<>();

        // running a for loop to compare elements.
        for (SitesModel item : sitesModelArrayList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are adding it to our filtered list.
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are displaying a toast message as no data found.
            Toast.makeText(this, "No Site Found..", Toast.LENGTH_SHORT).show();
        } else {
            // at last we are passing that filtered list to our adapter class.
            sitesAdapter.filterList(filteredlist);
        }
    }

    // method for showing the adding sites dialog

    private void showSitesAddingDialog() {
        // creating and customising dialog for adding a employee
        Dialog dialog = new Dialog(Sites.this, R.style.EmployeeAddingDialog);
        dialog.setContentView(R.layout.add_site_dialog_layout);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.7f;
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // linking buttons in the dialog so program knows what is what
        ImageButton Btn_Close = dialog.findViewById(R.id.Btn_Close_SITEDIALOG);
        Button Btn_Add = dialog.findViewById(R.id.Btn_Add_SITEDIALOG);
        EditText Et_SiteName_Dialog = dialog.findViewById(R.id.Et_SiteName_SITEDIALOG);
        EditText Et_HourRate_Dialog = dialog.findViewById(R.id.Et_HourRate_SITEDIALOG);
        Et_Location_Dialog = dialog.findViewById(R.id.Et_SiteLocation_SITEDIALOG);

        // click listener for add site button in the dialog
        Btn_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // converting the text in the fields to strings
                String newSiteName = Et_SiteName_Dialog.getText().toString();
                String newHourRate = Et_HourRate_Dialog.getText().toString();

                // if any fields are empty inform the user they need to be filled
                if (TextUtils.isEmpty(newSiteName) || TextUtils.isEmpty(newHourRate)) {
                    Toast.makeText(getApplicationContext(), "All Fields are Required", Toast.LENGTH_SHORT).show();
                }
                // if validation is passed and latLng is not null invoke the saveSiteToDB method
                else {
                    if (latLng == null) {
                        Toast.makeText(Sites.this, "Please Pick Location first", Toast.LENGTH_SHORT).show();
                    }
                    else { saveSiteToDB(latLng, newSiteName, newHourRate, dialog); }
                }

            }
        });

        // click listener for the add location view in the dialog
        dialog.findViewById(R.id.Et_SiteLocation_SITEDIALOG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isLocationEnabled(Sites.this)) {
                    startActivity(new Intent(Sites.this, MapsActivity.class));
                } else {
                    Toast.makeText(Sites.this, "Enable your GPS Location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // // click listener for the cancel button in the dialog
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show(); // ensure dialog is shown on screen
    }

    // method for saving a site to the database
    private void saveSiteToDB(LatLng latLng, String st_siteName, String st_hourRAte, Dialog dialog) {
        // getting current system time
        Long addedTime = System.currentTimeMillis();
        // creating a object (site) from the SitesModel class
        SitesModel sitesModel = new SitesModel(st_siteName, Float.parseFloat(st_hourRAte), latLng.latitude, latLng.longitude, addedTime);
        // accessing the sites collection in the database
        FirebaseFirestore.getInstance().collection(Constants.SITES_COLLECTION).document(sitesModel.getName()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // if site name already exists inform the user
                        if (documentSnapshot.exists()) {
                            new Utils().showShortToast(Sites.this, "Site Name Already Exists");
                        }
                        // if site name does not already exist
                        else {
                            FirebaseFirestore.getInstance().collection(Constants.SITES_COLLECTION).document(sitesModel.getName()).set(sitesModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        // if site is added successfully also add site to our sitesAdapter and invoke notifyDataSetChanged method
                                        @Override
                                        public void onSuccess(Void unused) {
                                            dialog.dismiss();
                                            sitesModelArrayList.add(sitesModel);
                                            // So sites RecyclerView updates
                                            sitesAdapter.notifyDataSetChanged();
                                            Toast.makeText(Sites.this, st_siteName + " Site Added Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    // if site is not added to the database successfully
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            Toast.makeText(Sites.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
    }

// to ensure already picked location latlng coordinates remain if user goes off screen
    @Override
    protected void onResume() {
        super.onResume();
        if (Common.latlng != null) {
            latLng = Common.latlng;
            Et_Location_Dialog.setText("Picked : " + latLng.latitude + ", " + latLng.longitude);
        }
    }
// if ondestroy occurs in the cycle reset latlng to null
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Common.latlng = null;
    }
}