package com.example.mysignupapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;

import com.example.mysignupapp.Utility.NetworkChangeListener;
import com.example.mysignupapp.databinding.ActivityMyAppointmentsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyAppointments extends DrawerBaseActivity
{

    ActivityMyAppointmentsBinding activityMyAppointmentsBinding;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    FirebaseAuth mAuth;
    FirebaseUser me;

    RecyclerView appointment_recyclerView;

    List<Appointment> appointments;

    AppointmentAdapter appointmentAdapter;
    AppointmentAdapter.AppointmentClickListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activityMyAppointmentsBinding = ActivityMyAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(activityMyAppointmentsBinding.getRoot());
        allocateActivityTitle("My Appointments");

        mAuth = FirebaseAuth.getInstance();
        me = mAuth.getCurrentUser();
        setOnClickAppointmentListener();
        appointment_recyclerView = findViewById(R.id.recycler_appointment);
        appointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(getApplicationContext(),appointments, listener);

        DatabaseReference appointment_ref = FirebaseDatabase.getInstance().getReference("Appointments");

        ProgressDialog progressDialog = new ProgressDialog(MyAppointments.this);
        progressDialog.setMessage("Loading appointments...");
        progressDialog.show();

        appointment_ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                appointments.clear();
                for(DataSnapshot appSnapshot : snapshot.getChildren())
                {

                    Appointment app = appSnapshot.getValue(Appointment.class);

                    if(app != null && (app.getSender_id().equals(me.getUid()) || app.getReceiver_id().equals(me.getUid())))
                    {
                        appointments.add(app);
                    }

                    GridLayoutManager gridLayoutManager = new GridLayoutManager(MyAppointments.this, 1, GridLayoutManager.VERTICAL, false);
                    appointment_recyclerView.setLayoutManager(gridLayoutManager);
                    appointment_recyclerView.setAdapter(appointmentAdapter);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

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

    private void setOnClickAppointmentListener()
    {
        listener = new AppointmentAdapter.AppointmentClickListener()
        {
            @Override
            public void onClick(View v, int position)
            {

            }
        };
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