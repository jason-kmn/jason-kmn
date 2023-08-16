package com.example.mysignupapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mysignupapp.Utility.NetworkChangeListener;
import com.example.mysignupapp.databinding.ActivityMapBinding;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapActivity extends DrawerBaseActivity implements OnMapReadyCallback {

    GoogleMap map;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private HashMap<String, Ad> ads;

    ActivityMapBinding activityMapBinding;
    private LatLng latLng = new LatLng(38.00237292320933, 23.735086083815727);
    private HashMap<String, Ad> mapsAds;
    private Button marker_btn;
    private View infoWindowView;
    private Ad selectedAd;
    private String ID_ofSelectedAd, str_la, str_lo, ad_id = "", publisher;
    LatLngCustom currentLocation;
    private boolean boolean_location;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private double la = 0.0, lo = 0.0;
    private FirebaseDatabase db;
    private String title, category, id;
    private Ad ad_from_adsDetails = new Ad();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMapBinding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(activityMapBinding.getRoot());
        allocateActivityTitle("Find Ads");

        ad_id = getIntent().getStringExtra("Ad_id");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Toast.makeText(MapActivity.this, "YOU EXIST", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MapActivity.this, "WHO ARE YOU", Toast.LENGTH_LONG).show();
        }

        db = FirebaseDatabase.getInstance();
        DatabaseReference ad_ref = db.getReference("Ads/" + ad_id);

        ad_ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(ad_id != null){
                    if(!ad_id.equals("")){
                        HashMap<String, Object> map_of_ad = (HashMap<String, Object>) snapshot.getValue();
                        title = (String) map_of_ad.get("Title");
                        category = (String) map_of_ad.get("Category");
                        id = (String) map_of_ad.get("ID");
                        publisher = (String) map_of_ad.get("Publisher");
                        ArrayList<String> images = (ArrayList<String>) map_of_ad.get("Images");
                        ArrayList<String> switches = (ArrayList<String>) map_of_ad.get("Switch");
                        String description = (String) map_of_ad.get("Description");
                        String price = (String) map_of_ad.get("Price");

                        HashMap<String, Object> coords = (HashMap<String, Object>) map_of_ad.get("Coordinates");

                        String str_la = (String) coords.get("latitude").toString();
                        String str_lo = (String) coords.get("longitude").toString();
                        if (str_la.equals("0") || str_lo.equals("0")) {
                            la = 0.0;
                            lo = 0.0;
                        } else {
                            la = (double) coords.get("latitude");
                            lo = (double) coords.get("longitude");
                        }

                        ad_from_adsDetails.setTitle(title);
                        ad_from_adsDetails.setCategory(category);
                        ad_from_adsDetails.setImages(images);
                        ad_from_adsDetails.setCategories_for_switching(switches);
                        ad_from_adsDetails.setDescription(description);
                        ad_from_adsDetails.setPrice(price);
                        ad_from_adsDetails.setCoordinates(new LatLngCustom(la, lo));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();

            }


        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
                map = googleMap;
                googleMap.setIndoorEnabled(false);

                getLocationCoordinates();

                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setMessage("We are already here");
                AlertDialog dialog = builder.create();
                dialog.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        LatLngCustom athens = null;
                        if(ad_id != null){
                            if(!ad_id.equals("")){

                                double lat = ad_from_adsDetails.getCoordinates().getLat() + getRandom(),
                                    lon = ad_from_adsDetails.getCoordinates().getLon() + getRandom();
                                athens = new LatLngCustom(lat, lon);
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, ad_from_adsDetails.getCoordinates().getLon()), 14));

                                CircleOptions circleOptions = new CircleOptions()
                                        .center(new LatLng(ad_from_adsDetails.getCoordinates().getLat(), lon))
                                        .radius(550) // Set the radius of the circle in meters
                                        .strokeWidth(2)
                                        .strokeColor(Color.YELLOW)
                                        .fillColor(Color.argb(70, 255, 255, 0)); // Transparent red fill color
                                map.addCircle(circleOptions);

                            }
                        }
                        else{
                            double current_lat = currentLocation.getLat() + getRandom(), current_lon = currentLocation.getLon() + getRandom();
                            athens = new LatLngCustom(current_lat, current_lon);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current_lat, current_lon), 14));
                        }


                        map.getUiSettings().setMyLocationButtonEnabled(false);

                        // Remove the default location icon
                        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        map.setMyLocationEnabled(false);

                    }
                }, 2500); // 3000 milliseconds = 3 seconds


//                TODO o kiklos pou eixa valei ston xarth, isos na mhn xreiazetai telika
                /*CircleOptions circleOptions = new CircleOptions()
                        .center(latLng)
                        .radius(750) // Set the radius of the circle in meters
                        .strokeWidth(2)
                        .strokeColor(Color.YELLOW)
                        .fillColor(Color.argb(70, 255, 255, 0)); // Transparent red fill color
                map.addCircle(circleOptions);*/ // o kiklos pou eixa valei ston xarth, isos na mhn xreiazetai telika

                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        marker_btn.setVisibility(View.GONE);
                    }
                });


                DatabaseReference ads_ref = FirebaseDatabase.getInstance().getReference("Ads");

                mapsAds = new HashMap<>();

                ads_ref.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        int count = 1;
                        mapsAds.clear();
                        for(DataSnapshot adSnapshot : snapshot.getChildren())
                        {
                            HashMap<String, Object> ad_from_Ads = (HashMap<String, Object>) adSnapshot.getValue();
                            if(!(ad_from_Ads != null && ad_from_Ads.get("Publisher").equals(currentUser.getUid())))
                            {
                                if(ad_from_Ads.get("Coordinates") != null){ // uparxoun oi sintetagmenes os oros sth vash

                                    HashMap<String, Object> coords = (HashMap<String, Object>) ad_from_Ads.get("Coordinates");
                                    String la = (String) coords.get("latitude").toString();
                                    String lo = (String) coords.get("longitude").toString();
                                    double lat = (double) Double.parseDouble(la);
                                    double lon = (double) Double.parseDouble(lo);
                                    Ad ad = new Ad();

                                    if(lat != 0){ // sintetagmenes != 0 => tha mpoun ston xarth
                                        ad.setCoordinates(new LatLngCustom(lat, lon));

                                        String category = (String) ad_from_Ads.get("Category");
                                        ad.setCategory(category);

                                        String price = (String) ad_from_Ads.get("Price");
                                        ad.setPrice(price);

                                        String title = (String) ad_from_Ads.get("Title");
                                        ad.setTitle(title);

                                        ArrayList<String> switch_items = (ArrayList<String>) ad_from_Ads.get("Switch");
                                        ad.setCategories_for_switching(switch_items);

                                        ArrayList<String> images = (ArrayList<String>) ad_from_Ads.get("Images");
                                        ad.setImages(images);

                                        String adID = (String) ad_from_Ads.get("ID");
                                        mapsAds.put(adID, ad);

                                        addMarker(new LatLng(lat, lon), ad, adID);

                                    }
                                    else{
                                        System.out.println("?????????");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });
    }

    private void openOtherActivity() {
        Intent intent = new Intent(MapActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    private void addMarker(LatLng latLng, Ad ad, String adID) {

        marker_btn = findViewById(R.id.button_viewAdDetails);
        marker_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapActivity.this, getSelectedAd().getTitle(), Toast.LENGTH_LONG).show();
                Intent ad_details_intent = new Intent(MapActivity.this, AdDetailsActivity.class);
                ad_details_intent.putExtra("Ad_id", selectedAd.getCategory() + " " + selectedAd.getTitle());
                startActivity(ad_details_intent);

            }
        });

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(adID)
                .icon(createCustomMarkerIcon(120, 120));
        Marker marker = map.addMarker(markerOptions);

        marker.setTag(ad);


        if(ad_id != null) {
            if (!ad_id.equals("")) {
                }
        }

        // Set a click listener for the marker
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker clickedMarker) {
                if (clickedMarker.equals(marker)) {
                }
                // Return 'false' to allow the default marker click behavior (info window display, etc.)
                return false;
            }
        });

        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                marker_btn.setVisibility(View.VISIBLE);

                Ad ad = (Ad) marker.getTag();
                setSelectedAd(ad);
                setID_ofSelectedAd(adID);
                Toast.makeText(getApplicationContext(), ad.getTitle(), Toast.LENGTH_SHORT).show();

                setSelectedAd(ad);

                // Inflate your custom info window layout
                infoWindowView = getLayoutInflater().inflate(R.layout.activity_marker_layout, null);

                // Customize the content of your info window
                TextView titleTextView = infoWindowView.findViewById(R.id.titleTextView);
                TextView categoryTextView = infoWindowView.findViewById(R.id.categoryTextView);
                TextView priceTextView = infoWindowView.findViewById(R.id.priceTextView);
                TextView switchingItemsTextView = infoWindowView.findViewById(R.id.switchingItemsTextView);

                titleTextView.setText("Title: " + ad.getTitle());
                categoryTextView.setText("Category: " + ad.getCategory());
                priceTextView.setText("Price: " + ad.getPrice());
                switchingItemsTextView.setText("Switching items: " + ad.getCategories());

                infoWindowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                return infoWindowView;
            }
        });
    }

    private BitmapDescriptor createCustomMarkerIcon(int width, int height) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.logo);
        Bitmap originalBitmap = bitmapDrawable.getBitmap();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    @Override
    protected void onStart()
    {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    private double getRandom(){
        double rangeMin = 0.001, rangeMax = 0.003;
        Random r = new Random();
        int num = getInt();
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();

        if(num==1){
            return randomValue * -1;
        }
        return randomValue;
    }

    private int getInt(){
        Random rand = new Random();
        int min = 0, max = 1;
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
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

    public Ad getSelectedAd() {
        return selectedAd;
    }

    public void setSelectedAd(Ad selectedAd) {
        this.selectedAd = selectedAd;
    }

    public String getID_ofSelectedAd() {
        return ID_ofSelectedAd;
    }

    public void setID_ofSelectedAd(String ID_ofSelectedAd) {
        this.ID_ofSelectedAd = ID_ofSelectedAd;
    }

    public boolean isBoolean_location() {
        return boolean_location;
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
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                setBoolean_location(false);

                if (e instanceof ResolvableApiException){
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(MapActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }

    public void setBoolean_location(boolean boolean_location) {
        this.boolean_location = boolean_location;
    }

    private void getLocationCoordinates() {

        if(isBoolean_location()){
        } else{
            setGPS_ON();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Handle permissions if not granted
            return;
        }

        LocationServices.getFusedLocationProviderClient(this)
                .getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            setCurrentLocation(latitude + getRandom(), longitude + getRandom());

                        } else {
                            setBoolean_location(false);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    public void setCurrentLocation(double lat, double lon){
        this.currentLocation = new LatLngCustom(lat, lon);
    }

}