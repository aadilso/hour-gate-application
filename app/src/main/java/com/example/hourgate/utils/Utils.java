package com.example.hourgate.utils;

import android.content.Context;
import android.location.LocationManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// show a short toast message
public class Utils {
    public void showShortToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // check if location services are enabled
    // https://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // aided by: https://www.tabnine.com/code/java/methods/java.util.Calendar/setTimeInMillis
    // https://stackoverflow.com/questions/7670355/convert-date-time-for-given-timezone-java

    // converts millis to years format
    public String getYearFromMillis(Long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.getDefault());
        return format.format(calendar.getTime());
    }
    // converts millis to months format
    public String getMonthFromMillis(Long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat("MMMM", Locale.getDefault());
        return format.format(calendar.getTime());
    }

    // converts millis to days format
    public String getDayFromMillis(Long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat("dd", Locale.getDefault());
        return format.format(calendar.getTime());
    }

    // getting current datetime
    public String getDateTimeFromMilSeconds(Long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat("dd.MMM.yyyy, HH:mm", Locale.getDefault());
        return format.format(calendar.getTime());
    }

    // https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
    // method for converting distance between 2 long/lat point into KM
    public Double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
    // https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
    // method for calculating distance between 2 long/lat points
    public double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }


    // method for getting the difference between the check in and out time in hours (ie hours worked)
    public float getHoursWorked(Long checkInTime, Long checkOutTime) {
        // Calculating the difference in milliseconds
        long differenceInMilliSeconds
                = Math.abs(checkOutTime - checkInTime);
        // Calculating the difference in Hours
        long differenceInHours
                = (differenceInMilliSeconds / (60 * 60 * 1000))
                % 24;
        return differenceInHours;
    }
}

