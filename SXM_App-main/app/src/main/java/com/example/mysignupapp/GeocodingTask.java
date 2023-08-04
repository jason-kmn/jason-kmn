package com.example.mysignupapp;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodingTask extends AsyncTask<String, Void, LatLng> {

    private GeocodingListener listener;

    public GeocodingTask(GeocodingListener listener) {
        this.listener = listener;
    }

    @Override
    protected LatLng doInBackground(String... strings) {
        Geocoder geocoder = new Geocoder(listener.getContext(), Locale.getDefault());
        String address = strings[0];

        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address firstAddress = addresses.get(0);
                double latitude = firstAddress.getLatitude();
                double longitude = firstAddress.getLongitude();
                return new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            Log.e("GeocodingTask", "Error geocoding address", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(LatLng latLng) {
        if (latLng != null) {
            listener.onGeocodingSuccess(latLng);
        } else {
            listener.onGeocodingFailure();
        }
    }

    public interface GeocodingListener {
        void onGeocodingSuccess(LatLng latLng);
        void onGeocodingFailure();
        CreateAdActivity getContext();
    }
}