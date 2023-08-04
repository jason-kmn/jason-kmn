package com.example.mysignupapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class DrawerBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;

    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_base, null);
        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);

        Toolbar toolbar = drawerLayout.findViewById(R.id.toolBar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_dehaze_24);
        setSupportActionBar(toolbar);

        NavigationView navigationView = drawerLayout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Drawer_open, R.string.Drawer_close);
        drawerLayout.addDrawerListener(toogle);
        toogle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        switch (item.getItemId()){
            case R.id.nav_home:
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_my_ads:
                startActivity(new Intent(this, MyAds.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_my_requests:
                startActivity(new Intent(this, MyRequests.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_my_appointments:
                startActivity(new Intent(this, MyAppointments.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_account:
                Intent my_account = new Intent(this, UserDetailsActivity.class);
                my_account.putExtra("ID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(my_account);
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_logout:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_name);
                builder.setIcon(R.drawable.logo_for_appeal);
                builder.setMessage("Log out from SXM?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                mAuth.signOut();
                                startActivity(new Intent(DrawerBaseActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
                break;
        }

        return false;
    }

    protected void allocateActivityTitle(String titleString){
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(titleString);
        }
    }

    public abstract void onLocationChanged(Location location);

    public abstract void onStatusChanged(String provider, int status, Bundle extras);

    public abstract void onProviderEnabled(String provider);

    public abstract void onProviderDisabled(String provider);
}