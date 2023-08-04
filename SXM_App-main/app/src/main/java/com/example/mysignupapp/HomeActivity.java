package com.example.mysignupapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.pm.PackageManager;

import com.example.mysignupapp.Utility.NetworkChangeListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.mysignupapp.databinding.ActivityHomeBinding;

public class HomeActivity extends DrawerBaseActivity implements LocationListener {

    ImageButton to_map_button;
    ImageButton to_create_Ad_button;

    ImageButton to_filters;

//    Button button_location; del
//    TextView textView_location; del
    LocationManager locationManager;
//    LatLng currentLocation;
    RecyclerView adList;
    AdAdapter.AdViewClickListener listener;
    AdAdapter adapter;
    List<HashMap<String, Object>> all_ads;
    FirebaseAuth mAuth;
    FirebaseUser me;

    ActivityHomeBinding activityHomeBinding;

    Button btn_addr_coords;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(activityHomeBinding.getRoot());
        allocateActivityTitle("Home");

        mAuth = FirebaseAuth.getInstance();
        me = mAuth.getCurrentUser();

        if(me != null)
        {
            Toast.makeText(HomeActivity.this, "YOU EXIST", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(HomeActivity.this, "WHO ARE YOU", Toast.LENGTH_LONG).show();
        }

        adList = findViewById(R.id.adList);

        ProgressDialog progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setMessage("Loading ads...");
        progressDialog.show();

        DatabaseReference ads_ref = FirebaseDatabase.getInstance().getReference("Ads");

        all_ads = new ArrayList<>();
        setOnClickAdListener();
        adapter = new AdAdapter(this, all_ads, listener);
        ads_ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                int count = 1;
                all_ads.clear();
                for(DataSnapshot adSnapshot : snapshot.getChildren())
                {
                    HashMap<String, Object> ad_from_Ads = (HashMap<String, Object>) adSnapshot.getValue();
                    Log.d("A", "Ad number " + count);
                    count++;

                    if(!(ad_from_Ads != null && ad_from_Ads.get("Publisher").equals(me.getUid())))
                    {
                        all_ads.add(ad_from_Ads);
                    }
                }
                GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeActivity.this, 2, GridLayoutManager.VERTICAL, false);
                adList.setLayoutManager(gridLayoutManager);
                adList.setAdapter(adapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(HomeActivity.this, "Failed to load ads", Toast.LENGTH_LONG).show();
            }
        });



        to_map_button = findViewById(R.id.map_mode);
        to_create_Ad_button = findViewById(R.id.create_ad_mode);

        if(ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        to_map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                setGPS_ON();
            }
        });

        to_filters = findViewById(R.id.filter_mode);

        to_filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent to_filter = new Intent(HomeActivity.this, FilterActivity.class);
                startActivity(to_filter);
            }
        });

        to_create_Ad_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent to_map = new Intent(HomeActivity.this, CreateAdActivity.class);
                startActivity(to_map);
            }
        });
    }

    private void setOnClickAdListener()
    {
        listener = new AdAdapter.AdViewClickListener() {
            @Override
            public void onClick(View v, int position)
            {
                Toast.makeText(HomeActivity.this, "Clicked Ad: " + all_ads.get(position).get("Title"), Toast.LENGTH_LONG).show();
                Intent ad_details_intent = new Intent(HomeActivity.this, AdDetailsActivity.class);
                String key_for_ad = all_ads.get(position).get("Category") + " " + all_ads.get(position).get("Title");
                ad_details_intent.putExtra("Ad_id", key_for_ad);
                startActivity(ad_details_intent);
            }
        };
    }

    public void setGPS_ON(){

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000/2);

        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();

        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
        locationSettingsRequestBuilder.setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Intent to_map = new Intent(HomeActivity.this, MapActivity.class);
                startActivity(to_map);
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException){
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(HomeActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
//        System.out.println("getLocation() ----------------------------------------------------------------------------------------------------------------------------------------"); del

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5,HomeActivity.this);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {

//        setCurrentLocation(location.getLatitude(), location.getLongitude());

        try {
            Geocoder geocoder = new Geocoder(HomeActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            String address = addresses.get(0).getAddressLine(0);

//            textView_location.setText(address); del

        }catch (Exception e){
            e.printStackTrace();
        }

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

//    public void setCurrentLocation(double lat, double lon){
//        this.currentLocation = new LatLng(lat, lon);
//    }


    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
//    public void setCurrentLocation(double lat, double lon){
//        this.currentLocation = new LatLng(lat, lon);
//    }
}