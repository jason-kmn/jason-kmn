package com.example.mysignupapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mysignupapp.Utility.NetworkChangeListener;
import com.example.mysignupapp.databinding.ActivityMyRequestsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyRequests extends DrawerBaseActivity {

    ActivityMyRequestsBinding activityMyRequestsBinding;

    FirebaseAuth mAuth;
    FirebaseUser me;

    RecyclerView request_recyclerView;

    RequestAdapter requestAdapter;

    RequestAdapter.RequestClickListener listener;

    List<Request> requests;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activityMyRequestsBinding = ActivityMyRequestsBinding.inflate(getLayoutInflater());
        setContentView(activityMyRequestsBinding.getRoot());
        allocateActivityTitle("My Requests");

        mAuth = FirebaseAuth.getInstance();
        me = mAuth.getCurrentUser();
        setOnClickRequestListener();

        request_recyclerView = findViewById(R.id.recycler_request);

        requests = new ArrayList<>();
        requestAdapter = new RequestAdapter(getApplicationContext(),requests, listener);
        DatabaseReference req_ref = FirebaseDatabase.getInstance().getReference("Requests");

        ProgressDialog progressDialog = new ProgressDialog(MyRequests.this);
        progressDialog.setMessage("Loading requests...");
        progressDialog.show();
        req_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                requests.clear();
                for(DataSnapshot reqSnapshot : snapshot.getChildren())
                {

                    Request req = reqSnapshot.getValue(Request.class);

                    if(req != null && req.getReceiver_id().equals(me.getUid()))
                    {
                        requests.add(req);
                    }

                    GridLayoutManager gridLayoutManager = new GridLayoutManager(MyRequests.this, 1, GridLayoutManager.VERTICAL, false);
                    request_recyclerView.setLayoutManager(gridLayoutManager);
                    request_recyclerView.setAdapter(requestAdapter);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setOnClickRequestListener()
    {
        listener = new RequestAdapter.RequestClickListener()
        {
            @Override
            public void onClick(View v, int position)
            {
                Intent request_details_intent = new Intent(MyRequests.this, RequestDetailsActivity.class);
                Request clicked = requests.get(position);
                request_details_intent.putExtra("S_ID", clicked.getSender_id());
                request_details_intent.putExtra("R_ID", clicked.getReceiver_id());
                request_details_intent.putExtra("A_ID", clicked.getAbout().get("Title").toString());
                startActivity(request_details_intent);
            }
        };
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