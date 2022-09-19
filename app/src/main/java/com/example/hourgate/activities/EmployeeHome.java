package com.example.hourgate.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.example.hourgate.models.LoginModel;
import com.example.hourgate.utils.UserPreferences;
import com.example.hourgate.utils.Utils;
import com.example.hourgate.databinding.ActivityEmployeeHomeBinding;

// employees homepage
public class EmployeeHome extends AppCompatActivity implements LocationListener {

    ActivityEmployeeHomeBinding binding;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LatLng latLng;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeeHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // aided:
        // https://developers.google.com/android/reference/com/google/android/gms/location/LocationCallback#onLocationResult(com.google.android.gms.location.LocationResult)

        //Used for receiving notifications from the FusedLocationProviderApi when the device location has changed or can no longer be determined.
        locationCallback = new LocationCallback() {

            // Called when device location information is available.
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {

                    //The most recent location is returned by LocationResult.getLastLocation()
                    double latitude = locationResult.getLastLocation().getLatitude();
                    double longitude = locationResult.getLastLocation().getLongitude();

                    latLng = new LatLng(latitude, longitude);
                    Log.e("Lat", latitude + "");
                    Log.e("Lng", longitude + "");
                }
            }
        };
        clickListener();
        checkLocationPermission();
    }
    //https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime
    // method for checking location permissions
    private void checkLocationPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Prompt the user once explanation has been shown
                                getLocationPermission();
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                getLocationPermission();
            }
        } else {
            getDeviceLocation();
        }
    }

    // https://stackoverflow.com/questions/48763263/getdevicelocation-method-update-current-position-marker
    // https://github.com/googlecodelabs/current-place-picker-android/blob/master/app/src/main/java/com/google/codelab/currentplace/MapsActivity.java
    // method for prompting the user for permission to use the device location.

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

// all click listeners are inside this method

    private void clickListener() {

        // click listener for the check in and out button
        binding.BtnCheckInOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if the latLng is not null start the activity
                if (latLng != null) {
                    startActivity(new Intent(getApplicationContext(), ClockActivity.class)
                            .putExtra("lat", latLng.latitude)
                            .putExtra("lng", latLng.longitude));
                }
                // else inform the user that location needs to be enabled
                else {
                    new Utils().showShortToast(EmployeeHome.this, "Enable GPS Location"); }
            }
        });

        // click listener for the my profile button
        binding.BtnMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), UserDataAdmin.class));
            }
        });
        // click listener for the logout button
        binding.BtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginModel loginModel = new LoginModel("Employee", false);
                new UserPreferences().saveLogin(EmployeeHome.this, loginModel);
                gotoLoginScreen();
            }
        });
    }

    // https://github.com/googlecodelabs/current-place-picker-android/blob/master/app/src/main/java/com/google/codelab/currentplace/MapsActivity.java
    // Handles the result of the request for location permissions.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    // method for redirecting to the login screen
    private void gotoLoginScreen() {
        Intent intent = new Intent(EmployeeHome.this, Login.class);
        startActivity(intent);
        finish();
    }

    // method for getting the employees location
    private void getDeviceLocation() {
        //https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#setInterval(long)
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000); // get location every 1000 milliseconds
        locationRequest.setFastestInterval(500); // fastest you can get location if other sevices are also using location
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        }else {
            LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public void onLocationChanged(Location location) {


    }
}