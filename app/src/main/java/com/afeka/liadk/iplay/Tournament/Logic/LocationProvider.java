package com.afeka.liadk.iplay.Tournament.Logic;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;
import java.util.Locale;

/**
 * Created by liadk
 */
public class LocationProvider implements LocationListener {

    private final String LOCATION_LANGUISH = "en-US";
    private final int REFRESH = 1000 * 60 * 2;

    private LocationManager mLocationManager;
    private MyLocation myLocation;
    private Activity mActivity;
    private static boolean permission;

    public interface MyLocation {
        void myCityName(String cityName);

        boolean checkPermission();
    }

    public LocationProvider(MyLocation myLocation, Activity activity) {
        this.myLocation = myLocation;
        permission = myLocation.checkPermission();
        mActivity = activity;
        mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        if (permission) {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null && location.getTime() > System.currentTimeMillis() - REFRESH) {
                onLocationChanged(location);
            } else {
                startSearchLocation();
            }
        }
    }

    public void startSearchLocation() {
        permission = myLocation.checkPermission();
        if (permission) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            String city = getCurrentCity(mActivity, location);
            if (city != null) {
                myLocation.myCityName(city);
                mLocationManager.removeUpdates(this);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private String getCurrentCity(Context context, Location location) {
        Geocoder gcd = new Geocoder(context, new Locale(LOCATION_LANGUISH));
        List<Address> addresses = null;
        String locality = null;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (Exception e) {
        }
        if (addresses != null && addresses.size() > 0) {
            locality = addresses.get(0).getLocality();
        }
        return locality;
    }
}
