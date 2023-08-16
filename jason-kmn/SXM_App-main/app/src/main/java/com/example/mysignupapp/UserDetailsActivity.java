package com.example.mysignupapp;


import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mysignupapp.Utility.NetworkChangeListener;
import com.example.mysignupapp.databinding.ActivityUserDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserDetailsActivity extends DrawerBaseActivity {
    private FirebaseAuth mAuth; //Reference to the Firebase connected to the project
    private FirebaseUser me; //the running Firebase user of the app
    private FirebaseDatabase db;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    ActivityUserDetailsBinding activityCreateAdBinding;

    TextView fullname;
    TextView username;
    TextView email;
    TextView total_ads;
    ImageView profile_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCreateAdBinding = ActivityUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(activityCreateAdBinding.getRoot());
        allocateActivityTitle("My Account");

        mAuth = FirebaseAuth.getInstance();
        me = mAuth.getCurrentUser();

        fullname = (TextView) findViewById(R.id.user_fullname);
        username = (TextView) findViewById(R.id.user_username);
        email = (TextView) findViewById(R.id.user_email);
        total_ads = (TextView) findViewById(R.id.user_total_ads);
        profile_picture = (ImageView) findViewById(R.id.no_profile_picture);

        String user_id = getIntent().getStringExtra("ID");
        db = FirebaseDatabase.getInstance();
        DatabaseReference user_ref = db.getReference("Users/" + user_id);

        ProgressDialog progressDialog = new ProgressDialog(UserDetailsActivity.this);
        progressDialog.setMessage("Loading profile...");
        progressDialog.show();
        user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Object obj = snapshot.getValue();
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++" + obj + "+++++++++++++++++++++++++++++++++++++++++++++");

                User user_now = snapshot.getValue(User.class);

                String user_fullname = "Fullname: " + user_now.getFirstName() + " " + user_now.getLastName();
                String user_username = "Username: " + user_now.getUsername();
                String user_email = "Email: " + user_now.getEmailAddress();
                String user_ads = "Total Ads: " + String.valueOf(user_now.getAds().size());

                fullname.setText(user_fullname);
                username.setText(user_username);
                email.setText(user_email);
                total_ads.setText(user_ads);

                if((user_now.getProfile_picture() != null))
                {
                    if(!user_now.getProfile_picture().isEmpty())
                    {
                        String user_profile_picture = user_now.getProfile_picture();
                        Picasso.get().load(user_profile_picture).into(profile_picture);
                    }
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDetailsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

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
}