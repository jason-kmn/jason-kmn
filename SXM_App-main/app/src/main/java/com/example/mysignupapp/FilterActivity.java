package com.example.mysignupapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mysignupapp.Utility.NetworkChangeListener;
import com.example.mysignupapp.databinding.ActivityFilterBinding;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;

public class FilterActivity extends DrawerBaseActivity
{
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String[] criteria ={"User rank", "Increasing price", "Decreasing price", "Item trade off", "Price Only", "Trade off only"};

    AutoCompleteTextView choices;

    ArrayAdapter<String> adapter_items;

    String[] prices_range ={"0-100", "100-500", "500-1000", "1000-10000", "10000+"};

    AutoCompleteTextView prices;

    ArrayAdapter<String> adapter_prices;

    TextView multiple_selections;
    boolean[] selectedDay;

    ArrayList<Integer> daylist = new ArrayList<>();
    String[] dayArray = {"Vehicles", "Men Clothing", "Women Clothing", "Music",
            "Sports", "Office", "Books", "Electronics", "Toys", "Movies", "Collectibles"};

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    ActivityFilterBinding activityFilterBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activityFilterBinding = ActivityFilterBinding.inflate(getLayoutInflater());
        setContentView(activityFilterBinding.getRoot());
        allocateActivityTitle("Choose Filters");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            Toast.makeText(FilterActivity.this, "YOU EXIST", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(FilterActivity.this, "WHO ARE YOU", Toast.LENGTH_LONG).show();
        }

        choices  = findViewById(R.id.autocomplete_text);

        adapter_items = new ArrayAdapter<String>(this, R.layout.sort_collection, criteria);

        choices.setAdapter(adapter_items);

        choices.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = parent.getItemAtPosition(position).toString();
            }
        });

        prices  = findViewById(R.id.autocomplete_text2);

        adapter_prices = new ArrayAdapter<String>(this, R.layout.sort_collection, prices_range);

        prices.setAdapter(adapter_prices);

        prices.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = parent.getItemAtPosition(position).toString();
            }
        });

        multiple_selections = findViewById(R.id.category_multiple_selector);
        selectedDay = new boolean[dayArray.length];

        multiple_selections.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                builder.setTitle("Category");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(dayArray, selectedDay, new DialogInterface.OnMultiChoiceClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        if(isChecked)
                        {
                            daylist.add(which);
                            Collections.sort(daylist);
                        }
                        else
                        {
                            daylist.remove(Integer.valueOf(which));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        StringBuilder stringBuilder = new StringBuilder();

                        for(int j = 0; j < daylist.size(); j++)
                        {
                            stringBuilder.append(dayArray[daylist.get(j)]);

                            if(j != daylist.size() -1)
                            {
                                stringBuilder.append(", ");
                            }
                        }

                        multiple_selections.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

                builder.setNeutralButton("Clear all", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        for(int j = 0; j <selectedDay.length; j++)
                        {
                            selectedDay[j] = false;
                            daylist.clear();
                            multiple_selections.setText("");
                        }

                    }
                });

                builder.show();
            }
        });

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