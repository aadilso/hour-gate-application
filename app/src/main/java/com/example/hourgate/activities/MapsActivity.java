package com.example.hourgate.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.example.hourgate.models.SitesModel;
import com.example.hourgate.utils.Common;
import com.example.hourgate.R;
import com.example.hourgate.databinding.ActivityMapsBinding;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private ActivityMapsBinding binding;
    //  variable for Current Place Picker
    private static final String TAG = "";

    private ActivityResultLauncher<Intent> resultLauncher;
    private GoogleMap mMap;
    // The geographical location where the device is currently located. That is, the last-known location
    // location retrieved by the Fused Location Provider.
    private Location mLastLocation;
    private LatLng latLng;
    private MarkerOptions myLocationMarker;
    double Lat = 0.0, Lng = 0.0;

    // FusedLocationProviderClient is for interacting with the location using fused location provider (need gps on for it to work)
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    CameraPosition mCameraPosition;
    private MapsActivity context;
    private SitesModel sitesModel;
    // A default location (London, England) and default zoom to use when location permission is not granted
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final int DEFAULT_ZOOM = 16;
    private LatLng london = new LatLng(51.509865, -0.118092);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            mLastLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        initialisation();
        getIntentData();
        clickListeners();
        initPlacesSdk();
        initLaunchers();
    }

    // method for retrieving created sites data

    private void getIntentData() {

        // getSerializableData - Retrieve extended data from the intent. (from official docs)
        sitesModel = (SitesModel) getIntent().getSerializableExtra("siteModel");

        if (sitesModel != null) {
            binding.searchPlaces.setVisibility(View.GONE);
            binding.location.setVisibility(View.GONE);
            binding.pickLocationBtn.setVisibility(View.GONE);
        }
    }


    // https://developers.google.cn/maps/documentation/places/android-sdk/client-migration

    //  Method for initialising Places.
    private void initPlacesSdk() {
        if (!Places.isInitialized()) {

            //Places.initialize(MapsActivity.this, getString(R.string.google_maps_key));
        }
    }

    private void clickListeners() {

        // click listener for the back button
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // click listener for the location button (bottom right)
        binding.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocationUI();
                getDeviceLocation();
            }
        });

        // click listener for the search places button/component
        binding.searchPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlacesActivity();
            }
        });

        // click listener for the pick location button
        binding.pickLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (latLng != null) {
                    Common.latlng = latLng;
                    Toast.makeText(context, "Location picked", Toast.LENGTH_SHORT).show();
                    /// https://stackoverflow.com/questions/61023968/what-do-i-use-now-that-handler-is-deprecated
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1500);
                }
            }
        });

    }

    //from https://developers.google.com/maps/documentation/places/android-sdk/client-migration#place_picker
    // method to return place predictions in response to user search queries
    private void startPlacesActivity() {

        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(context);
        resultLauncher.launch(intent);

    }


    // https://developers.google.com/maps/documentation/android-sdk/map
    // initialising our map
    private void initialisation() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        // or essentially - Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        context = MapsActivity.this;
    }

    // This method manipulates the map once the map is ready to be used.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // if there is not SitesModel available:
        if (sitesModel == null) {
            // asking for user permission to use device location
            getLocationPermission();
            // Turn on the My Location layer and the related control on the map.
            updateLocationUI();
            // Get the current location of the device and set the position of the map.
            getDeviceLocation();
        }
        // if the sitesModel is not null add the marker to that sites location
        else {
            LatLng latLng = new LatLng(sitesModel.getLat(), sitesModel.getLng());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(false);
            markerOptions.title(sitesModel.getName());
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    latLng, DEFAULT_ZOOM));
        }
    }

    // https://www.youtube.com/watch?v=NxQY0-QRM1c along with other tutorials mentioned this also assisted

    private void initLaunchers() {

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            if (result != null && result.getData() != null) {

                try {

                    Place placeFromIntent = Autocomplete
                            .getPlaceFromIntent(result.getData());

                    latLng = placeFromIntent.getLatLng();

                    Log.e("place", latLng.latitude + " " + latLng.longitude);

                    myLocationMarker = new MarkerOptions();
                    myLocationMarker.draggable(true);
                    myLocationMarker.position(latLng);
                    myLocationMarker.title(placeFromIntent.getName());

                    mMap.clear();
                    mMap.addMarker(myLocationMarker);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            latLng, DEFAULT_ZOOM));
                    mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDrag(@NonNull Marker marker) {

                        }

                        @Override
                        public void onMarkerDragEnd(@NonNull Marker marker) {
                            latLng = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                        }

                        @Override
                        public void onMarkerDragStart(@NonNull Marker marker) {

                        }
                    });


                } catch (Exception exception) {

                    Log.d("Error -> %s", exception.getMessage());
                }

            }

        });

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    //  method to get the current location of the device, and position the map's camera
    // https://developers.google.com/maps/documentation/places/android-sdk/current-place-tutorial#add-a-map adapted from here
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastLocation = task.getResult();

                            if (mLastLocation != null) {

                                double latitude = mLastLocation.getLatitude();
                                double longitude = mLastLocation.getLongitude();

                                latLng = new LatLng(latitude, longitude);

                                myLocationMarker = new MarkerOptions();
                                myLocationMarker.position(latLng);

                                myLocationMarker.draggable(true);
                                myLocationMarker.title("Drag to move location");

                                Log.e("location", latLng.latitude + " " + latLng.longitude);
                                mMap.clear();
                                mMap.addMarker(myLocationMarker);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        latLng, DEFAULT_ZOOM));

                                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                                    @Override
                                    public void onMarkerDrag(@NonNull Marker marker) {

                                    }

                                    @Override
                                    public void onMarkerDragEnd(@NonNull Marker marker) {
                                        latLng = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                                    }

                                    @Override
                                    public void onMarkerDragStart(@NonNull Marker marker) {

                                    }
                                });

                            } else {
                                Toast.makeText(MapsActivity.this, "Enable Device Location", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(london, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            } else {
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

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
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
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
                    updateLocationUI();
                }
            }
        }
    }

    // https://developers.google.com/maps/documentation/places/android-sdk/current-place-tutorial#add-a-map
    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.setBuildingsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
            // if location is not enabled then invoke the getLocationPermission method
            else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}