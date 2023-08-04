package com.example.mysignupapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mysignupapp.databinding.ActivityRequestDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestDetailsActivity extends DrawerBaseActivity
{
    ActivityRequestDetailsBinding activityRequestDetailsBinding;
    String sender_id;
    String receiver_id;
    String key_for_request;
    String name_of_ad;
    String sender_username;
    String receiver_username;

    FirebaseAuth mAuth;
    FirebaseUser me;

    DatabaseReference users;
    DatabaseReference sender_user;
    DatabaseReference receiver_user;

    DatabaseReference requests;

    TextView offer_message;
    TextView offer_title;
    ViewPager2 offer_viewpager;
    TextView offer_category;
    TextView offer_price;
    TextView offer_description;
    Button accept;
    Button decline;

    List<Image_For_Slider> slider_items;

    String what_time;
    String what_date;

    private Handler handler_for_images = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityRequestDetailsBinding = ActivityRequestDetailsBinding.inflate(getLayoutInflater());
        setContentView(activityRequestDetailsBinding.getRoot());
        allocateActivityTitle("Request Details");

        mAuth = FirebaseAuth.getInstance();
        me = mAuth.getCurrentUser();

        offer_message = findViewById(R.id.who_sent_message);
        offer_title = findViewById(R.id.request_title);
        offer_viewpager = findViewById(R.id.request_viewpager);
        offer_category = findViewById(R.id.request_category);
        offer_price = findViewById(R.id.request_price);
        offer_description = findViewById(R.id.request_description);
        accept = findViewById(R.id.accept_button);
        decline = findViewById(R.id.decline_button);

        sender_id = getIntent().getStringExtra("S_ID");
        receiver_id = getIntent().getStringExtra("R_ID");
        name_of_ad = getIntent().getStringExtra("A_ID");

        users = FirebaseDatabase.getInstance().getReference("Users");
        sender_user = users.child(sender_id);
        receiver_user = users.child(receiver_id);

        slider_items = new ArrayList<>();
        key_for_request = name_of_ad + ": ";

        ProgressDialog progressDialog = new ProgressDialog(RequestDetailsActivity.this);
        progressDialog.setMessage("Loading request...");
        progressDialog.show();

        sender_user.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                User sender = snapshot.getValue(User.class);
                sender_username = sender.getUsername();

                if(sender != null && sender_username != null)
                {
                    progressDialog.dismiss();

                    if (receiver_username != null)
                    {
                        key_for_request = key_for_request + sender_username + "to " + receiver_username;
                        useCompleteKey();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

        receiver_user.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                User receiver = snapshot.getValue(User.class);
                receiver_username = receiver.getUsername();

                if(receiver != null && receiver_username != null)
                {
                    progressDialog.dismiss();

                    if (sender_username != null)
                    {
                        key_for_request = key_for_request + sender_username + "to " + receiver_username;
                        useCompleteKey();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

    }

    private void useCompleteKey()
    {
        requests = FirebaseDatabase.getInstance().getReference("Requests/" + key_for_request);

        requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Request req = snapshot.getValue(Request.class);
                what_time = req.getAppointment_hour();
                what_date = req.getAppointment_date();

                if(req.getPrice_offer() != null)
                {
                    offer_title.setVisibility(View.GONE);
                    offer_viewpager.setVisibility(View.GONE);
                    offer_category.setVisibility(View.GONE);
                    offer_description.setVisibility(View.GONE);

                    String price_message = sender_username +
                            " has sent you a money offer!" +
                            "If you wish to make an appointment to realize the purchase press the Accept button, otherwise press the Decline button.";
                    offer_message.setText(price_message);

                    String what_price = "Price Offer: " + req.getPrice_offer();
                    offer_price.setText(what_price);
                }
                else if(req.getTrade() != null)
                {
                    offer_price.setVisibility(View.GONE);

                    String price_message = sender_username +
                            " has sent you a switch offer!" +
                            "If you wish to make an appointment to realize the trade press the Accept button, otherwise press the Decline button.";
                    offer_message.setText(price_message);

                    HashMap<String, Object> trade = req.getTrade();
                    String trade_title = "Title: " + (String) trade.get("Title");
                    String trade_category = "Category: " + (String) trade.get("Category");
                    String trade_description = "Description: " +(String) trade.get("Description");
                    ArrayList<String> images = (ArrayList<String>) trade.get("Images");

                    offer_title.setText(trade_title);
                    offer_category.setText(trade_category);
                    offer_description.setText(trade_description);

                    for(String path: images)
                    {
                        slider_items.add(new Image_For_Slider(path));
                    }

                    offer_viewpager.setAdapter(new ImageSliderAdapter(slider_items, offer_viewpager));
                    offer_viewpager.setClipToPadding(false);
                    offer_viewpager.setClipChildren(false);
                    offer_viewpager.setOffscreenPageLimit(3);
                    offer_viewpager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

                    CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
                    compositePageTransformer.addTransformer(new MarginPageTransformer(40));
                    compositePageTransformer.addTransformer(new ViewPager2.PageTransformer()
                    {
                        @Override
                        public void transformPage(@NonNull View page, float position)
                        {
                            float r = 1 - Math.abs(position);
                            page.setScaleY(0.85f + r * 0.15f);
                        }
                    });
                    offer_viewpager.setPageTransformer(compositePageTransformer);

                    offer_viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
                    {
                        @Override
                        public void onPageSelected(int position)
                        {
                            super.onPageSelected(position);
                            handler_for_images.removeCallbacks(slider_runnable);
                            handler_for_images.postDelayed(slider_runnable, 3000);
                        }
                    });


                }

                accept.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        req.accepted = true;
                        DatabaseReference appointment_reference = FirebaseDatabase.getInstance().getReference("Appointments");
                        String appointmentId = appointment_reference.push().getKey();
                        Appointment new_appointment = new Appointment(req, sender_id, sender_username, receiver_id, receiver_username, what_time, what_date);
                        String title_for_appointment = appointmentId + ": " + receiver_username + "-" + sender_username;
                        appointment_reference.child(title_for_appointment).setValue(new_appointment);

                        DatabaseReference req_ref = FirebaseDatabase.getInstance().getReference("Requests");
                        req_ref.child(key_for_request).removeValue();
                        startActivity(new Intent(RequestDetailsActivity.this, HomeActivity.class));
                    }
                });

                decline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        DatabaseReference req_ref = FirebaseDatabase.getInstance().getReference("Requests");
                        req_ref.child(key_for_request).removeValue();
                        startActivity(new Intent(RequestDetailsActivity.this, HomeActivity.class));
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    private Runnable slider_runnable = new Runnable()
    {
        @Override
        public void run() {
            offer_viewpager.setCurrentItem(offer_viewpager.getCurrentItem() + 1);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        handler_for_images.removeCallbacks(slider_runnable);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        handler_for_images.postDelayed(slider_runnable, 3000);
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