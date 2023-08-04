package com.example.mysignupapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.mysignupapp.Utility.NetworkChangeListener;
import com.example.mysignupapp.databinding.ActivityMakeOfferBinding;
import com.example.mysignupapp.ml.ModelUnquant;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MakeOffer extends DrawerBaseActivity
{
    ActivityMakeOfferBinding activityMakeOfferBinding;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    boolean offer_money_choice;
    boolean offer_ad_choice;
    String offer_sender; //String which carries the id of the sender(current user)
    String offer_receiver;//String which carries the id of the ad's publisher
    String offer_about_ad_id;//String which carries the id of the ad
    private static final int PICK_REQUEST_IMAGES_CODE = 1000; //code for pick image intent
    int position = 0; //int to keep track of the images' indexes
    TextInputLayout SELECT_CATEGORY_TEXT_INPUT; //"Select Category" for offer's category
    TextInputLayout TITLE_TEXT_INPUT;//Title of Ad Offer
    TextInputLayout PRICE_TEXT_INPUT;//Price to offer
    TextInputLayout DESCRIPTION_TEXT_INPUT;//Description of offer
    RelativeLayout RELATIVE_LAYOUT_REQUEST;
    TextView image_number_text;
    ImageSwitcher image_switcher_offer;
    private ArrayList<Uri> imageUris;
    private ArrayList<String> image_url_paths;
    Button previous_image_button;
    Button next_image_button;
    ImageButton delete_image_button;
    Button pick_image_button;
    Button send_request_button;
    Button smart_check;
    String[] categories_to_choose = {"Vehicles", "Clothing", "Book","Toy","Music",
            "Sports", "Office"};
    AutoCompleteTextView text_of_chosen_category;
    ArrayAdapter<String> adapter_for_categories;
    String chosen_category_input;
    FirebaseDatabase db;
    HashMap<String, Object> which_ad_i_am_interested;
    String sender_username;
    String receiver_username;
    StorageTask uploadTask2;
    StorageReference storageReference;
    String offer_title_input;
    String offer_description_input;

    int images_size_for_recognition = 224;
    boolean smart_check_at_least_once = false;
    boolean smart_check_complete = false;
    ArrayList<Bitmap> image_bitmaps;
    ArrayList<Boolean> image_matches;
    AutoCompleteTextView hour_of_appointment;
    String appointment_from;
    String appointment_to;
    LocalTime appointment_from_date;
    LocalTime appointment_to_date;
    LocalTime time_of_appointment;
    ArrayAdapter<String> AvailableHoursItems;
    ArrayList<String> available_hours;

    Button calendar_editText;
    String chosen_date;
    int cal_year;
    int cal_month;
    int cal_day;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_offer);
        activityMakeOfferBinding = ActivityMakeOfferBinding.inflate(getLayoutInflater());
        setContentView(activityMakeOfferBinding.getRoot());
        allocateActivityTitle("Make Offer");

        offer_money_choice = getIntent().getBooleanExtra("OFFER_MONEY", false);
        offer_ad_choice = getIntent().getBooleanExtra("OFFER_AD", false);
        offer_sender = getIntent().getStringExtra("OFFER_FROM");
        offer_receiver = getIntent().getStringExtra("OFFER_TO");
        offer_about_ad_id = getIntent().getStringExtra("OFFER_ABOUT");

        previous_image_button = (Button) findViewById(R.id.request_previousBtn);
        next_image_button = (Button) findViewById(R.id.request_nextBtn);
        delete_image_button = (ImageButton) findViewById(R.id.request_deleteBtn);
        pick_image_button = (Button) findViewById(R.id.request_pickImagesBtn);
        send_request_button = (Button) findViewById(R.id.create_request);
        smart_check = (Button) findViewById(R.id.request_smart_check);
        calendar_editText = (Button) findViewById(R.id.calendar_editText);

        SELECT_CATEGORY_TEXT_INPUT = findViewById(R.id.request_category_box);
        text_of_chosen_category = findViewById(R.id.request_category);
        TITLE_TEXT_INPUT = findViewById(R.id.request_title_textfield);
        PRICE_TEXT_INPUT = findViewById(R.id.request_price_textfield);
        DESCRIPTION_TEXT_INPUT = findViewById(R.id.request_description_textfield);
        image_number_text = findViewById(R.id.request_image_number);
        RELATIVE_LAYOUT_REQUEST = findViewById(R.id.relative_for_request_image);
        image_switcher_offer = findViewById(R.id.request_imagesIs);
        adapter_for_categories = new ArrayAdapter<String>(this, R.layout.list_item, categories_to_choose);
        text_of_chosen_category.setAdapter(adapter_for_categories);

        if (offer_money_choice)
        {
            SELECT_CATEGORY_TEXT_INPUT.setVisibility(View.GONE);
            TITLE_TEXT_INPUT.setVisibility(View.GONE);
            DESCRIPTION_TEXT_INPUT.setVisibility(View.GONE);
            image_number_text.setVisibility(View.GONE);
            pick_image_button.setVisibility(View.GONE);
            previous_image_button.setVisibility(View.GONE);
            delete_image_button.setVisibility(View.GONE);
            next_image_button.setVisibility(View.GONE);
            smart_check.setVisibility(View.GONE);
            image_switcher_offer.setVisibility(View.GONE);
            RELATIVE_LAYOUT_REQUEST.setVisibility(View.GONE);
        }
        else if (offer_ad_choice)
        {
            PRICE_TEXT_INPUT.setVisibility(View.GONE);
        }

        db = FirebaseDatabase.getInstance();
        DatabaseReference ads_ref = db.getReference("Ads");
        DatabaseReference user_ref = db.getReference("Users");

        storageReference = FirebaseStorage.getInstance().getReference("Requests");

        user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot user_child : snapshot.getChildren()) {
                    User user_now = user_child.getValue(User.class);

                    if (user_now.getId().equals(offer_sender)) {
                        sender_username = user_now.getUsername();
                    }

                    if (user_now.getId().equals(offer_receiver)) {
                        receiver_username = user_now.getUsername();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading Ad Details");
        progressDialog.show();
        ads_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren())
                {
                    HashMap<String, Object> child_hashmap = (HashMap<String, Object>) child.getValue();
                    if (child_hashmap.get("ID").equals(offer_about_ad_id))
                    {
                        System.out.println("------------------------FOUND AD FROM DATABASE-------------------------------------------");
                        which_ad_i_am_interested = child_hashmap;
                        appointment_from = (String) which_ad_i_am_interested.get("Appointment_From");
                        System.out.println("Available From: " + appointment_from);
                        appointment_to = (String) which_ad_i_am_interested.get("Appointment_To");
                        System.out.println("Available To: " + appointment_to);
                        appointment_from_date = LocalTime.parse(appointment_from);
                        System.out.println("LocalTime From: " + appointment_from_date.toString());
                        appointment_to_date = LocalTime.parse(appointment_to);
                        System.out.println("LocalTime To: " + appointment_to_date.toString());
                        System.out.println("------------------------------Available times--------------------------------------------");

                        available_hours = new ArrayList<>();
                        LocalTime current_time = appointment_from_date;
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

                        while (current_time.isBefore(appointment_to_date) || current_time.equals(appointment_to_date)) {

                            String current_time_string = current_time.format(formatter);
                            available_hours.add(current_time_string);
                            current_time = current_time.plusMinutes(30);
                        }
                        for(String t: available_hours)
                        {
                            System.out.println("Choice: " + t);
                        }
                        System.out.println("-----------------------------------------------------------------------------------------");

                        hour_of_appointment = findViewById(R.id.select_appointment_hour);
                        AvailableHoursItems = new ArrayAdapter<String>(MakeOffer.this, R.layout.list_item, available_hours);
                        hour_of_appointment.setAdapter(AvailableHoursItems);
                        break;
                    }

                }
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        calendar_editText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Calendar cal = Calendar.getInstance();
                cal_day = cal.get(Calendar.DATE);
                cal_month = cal.get(Calendar.MONTH);
                cal_year = cal.get(Calendar.YEAR);

                // Set the minimum date to one week after today
                Calendar minDate = Calendar.getInstance();
                minDate.add(Calendar.DAY_OF_MONTH, 7);

                // Set the maximum date to one month after today
                Calendar maxDate = Calendar.getInstance();
                maxDate.add(Calendar.MONTH, 1);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MakeOffer.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date)
                    {
                        chosen_date = date + "/" + month + "/" + year;
                        calendar_editText.setText(chosen_date);
                    }
                }, cal_year, cal_month, cal_day);

                datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
                datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
                datePickerDialog.show();
            }
        });

        hour_of_appointment = findViewById(R.id.select_appointment_hour);
        hour_of_appointment.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String time_from_input = (String) parent.getItemAtPosition(position);

                try {
                    time_of_appointment = LocalTime.parse(time_from_input);
                    // Use the LocalTime object
                } catch (DateTimeParseException e) {
                    // Handle the exception
                    System.out.println("Invalid time format: " + time_from_input);
                }
                System.out.println("----------------------------------APPOINTMENT------------------------------------------------");
                System.out.println("Hour selected: " + time_from_input);
                System.out.println("LocalTime from selection: " + time_of_appointment);
                System.out.println("---------------------------------------------------------------------------------------------");
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("Requests");
        text_of_chosen_category.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                chosen_category_input = item;
            }
        });

        imageUris = new ArrayList<>();
        image_url_paths = new ArrayList<>();
        image_bitmaps = new ArrayList<>();
        image_matches = new ArrayList<>();

        String image_number_so_far = "Images: " + imageUris.size() + "/5";
        image_number_text.setText(image_number_so_far);
        image_switcher_offer.setFactory(new ViewSwitcher.ViewFactory() {
            @Override

            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                return imageView;
            }
        });
        previous_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    position--;
                    image_switcher_offer.setImageURI(imageUris.get(position));
                } else {
                    Toast.makeText(MakeOffer.this, "No Previous images", Toast.LENGTH_SHORT).show();
                }
            }
        });

        next_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < imageUris.size() - 1) {
                    position++;
                    image_switcher_offer.setImageURI(imageUris.get(position));
                } else {
                    Toast.makeText(MakeOffer.this, "No More images", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (imageUris.isEmpty())
                {
                    Toast.makeText(MakeOffer.this, "No images to delete", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    image_switcher_offer.setImageURI(null);
                    imageUris.clear();
                    image_bitmaps.clear();
                    image_matches.clear();
                    String image_number_now = "Images: " + imageUris.size() + "/5";
                    image_number_text.setText(image_number_now);
                    position = 0;
                    smart_check_complete = false;
                    smart_check_at_least_once = false;
                }
            }
        });

        pick_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUris.size() >= 5) {
                    Toast.makeText(MakeOffer.this, "Max image input reached!", Toast.LENGTH_SHORT).show();
                } else {
                    pickImagesIntent2();
                }
            }
        });
        smart_check.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(chosen_category_input == null)
                {
                    showPop(getWindow().getDecorView().getRootView(), "Your ad must have a category");
                }
                else if(image_bitmaps.size() == 0)
                {
                    showPop(getWindow().getDecorView().getRootView(), "No image input to check");
                }
                else if(chosen_category_input != null && image_bitmaps.size() > 0)
                {
                    smart_check_at_least_once = true;
                    System.out.println("Smart Check Button clicked");
                    System.out.println("Total images converted to bitmaps: " + image_bitmaps.size());
                    int counter = 1;
                    for(Bitmap image_sample: image_bitmaps)
                    {
                        System.out.println("Image Sample Number " + counter);
                        classifyImage(image_sample);
                        counter++;
                    }
                }
            }
        });


        send_request_button.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v)
            {
                offer_title_input = TITLE_TEXT_INPUT.getEditText().getText().toString();
                offer_description_input = DESCRIPTION_TEXT_INPUT.getEditText().getText().toString();

                if(time_of_appointment == null)
                {
                    showPop(getWindow().getDecorView().getRootView(), "Choose what time you wish for an appointment");
                }

                if(chosen_date == null)
                {
                    showPop(getWindow().getDecorView().getRootView(), "Choose what date you wish for an appointment");
                }

                if(offer_ad_choice)
                {
                    if(!smart_check_at_least_once)
                    {
                        androidx.appcompat.app.AlertDialog.Builder dlgAlert1  = new androidx.appcompat.app.AlertDialog.Builder(MakeOffer.this);
                        dlgAlert1.setMessage("You must Smart Check your ad at least once.");
                        dlgAlert1.setTitle("Not so fast");
                        dlgAlert1.setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        });
                        dlgAlert1.setCancelable(true);
                        dlgAlert1.create().show();
                    }

                    String error_for_images = "Your Smart Check detected problem: Your images don't match the selected category\n";
                    boolean found_problem = false;
                    for(int j = 0; j < image_matches.size(); j++)
                    {
                        if(!image_matches.get(j))
                        {
                            found_problem = true;
                            error_for_images += "Picture number: " + (j + 1) + "\n";
                        }
                    }

                    smart_check_complete = !found_problem;

                    if(chosen_category_input != null && imageUris.size() == 0 && image_bitmaps.size() > 0 && image_matches.size() > 0)
                    {
                        showPop(getWindow().getDecorView().getRootView(), "Your ad doesn't have pictures");
                    }

                    if(smart_check_at_least_once && !smart_check_complete)
                    {
                        androidx.appcompat.app.AlertDialog.Builder dlgAlert1  = new androidx.appcompat.app.AlertDialog.Builder(MakeOffer.this);
                        dlgAlert1.setMessage(error_for_images);
                        dlgAlert1.setTitle("Not so fast");
                        dlgAlert1.setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        });
                        dlgAlert1.setCancelable(true);
                        dlgAlert1.create().show();
                    }
                }

                if(offer_ad_choice && smart_check_at_least_once && smart_check_complete && imageUris.size() > 0
                        && image_bitmaps.size() > 0 && image_matches.size() > 0 && time_of_appointment != null && chosen_date != null)
                {
                    sendRequestToUser();
                }
                else if(offer_money_choice && time_of_appointment != null && chosen_date != null)
                {
                    sendRequestToUser();
                }
            }
        });

    }

    public void classifyImage(Bitmap image)
    {
        try
        {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * images_size_for_recognition * images_size_for_recognition * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[images_size_for_recognition * images_size_for_recognition];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;


            for(int i = 0; i < images_size_for_recognition; i++)
            {
                for(int j = 0; j < images_size_for_recognition; j++)
                {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat((val & 0xFF)*(1.f/255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] percentages = outputFeature0.getFloatArray();
            int maxPos = 0;
            float max_percentage = 0;

            for(int i = 0; i < percentages.length; i++)
            {
                if(percentages[i] > max_percentage)
                {
                    max_percentage = percentages[i];
                    maxPos = i;
                }
            }

            String[] classes = {"Vehicles", "Clothing", "Book","Toy","Music",
                    "Sports", "Office"};

            boolean val_boo;
            val_boo = chosen_category_input.equals(classes[maxPos]);
            image_matches.add(val_boo);

            String result = "CLASSIFIED AS: " + classes[maxPos];
            String s = "";
            for(int i = 0; i < classes.length; i++)
            {
                s+= String.format("%s: %.1f%%\n", classes[i], percentages[i] * 100);
            }

            System.out.println("-------------------------Classification------------------------");
            System.out.println(result);
            System.out.println("---------------------------------------------------------------");
            System.out.println("---------------------------Statistics--------------------------");
            System.out.println(s);
            System.out.println("---------------------------------------------------------------");

            // Releases model resources if no longer used.
            model.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendRequestToUser()
    {
        if (offer_money_choice)
        {
            String price_input = PRICE_TEXT_INPUT.getEditText().getText().toString();

            boolean isDouble = false;
            boolean isDoublePositive = false;

            try {
                double number = Double.parseDouble(price_input);
                if (number > 0) {
                    isDouble = true;
                    isDoublePositive = true;
                } else {
                    isDouble = true;
                }
            } catch (NumberFormatException e) {
                showPop(getWindow().getDecorView().getRootView(), "Not a number");
            }

            if (isDouble && isDoublePositive) {
                ProgressDialog progressDialog = new ProgressDialog(MakeOffer.this);
                progressDialog.setMessage("Sending Request...");
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Requests");
                String request_id = reference.push().getKey();

                Date now = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                String date_now = dateFormat.format(now);
                DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");

                Request send_request = new Request(request_id, offer_sender, offer_receiver, date_now,
                        which_ad_i_am_interested, price_input + "$", null, false, time_of_appointment.format(format), chosen_date);

                String title_for_request = which_ad_i_am_interested.get("Title") + ": " + sender_username + "to " + receiver_username;
                reference.child(title_for_request).setValue(send_request);

                progressDialog.dismiss();

                startActivity(new Intent(MakeOffer.this, HomeActivity.class));

            } else if (isDouble) {
                showPop(getWindow().getDecorView().getRootView(), "You must put positive number for money");
            }
        }
        else if (offer_ad_choice)
        {
            ProgressDialog progressDialog = new ProgressDialog(MakeOffer.this);
            progressDialog.setMessage("Sending request...");
            progressDialog.show();

            offer_title_input = TITLE_TEXT_INPUT.getEditText().getText().toString();
            offer_description_input = DESCRIPTION_TEXT_INPUT.getEditText().getText().toString();

            if (imageUris.size() > 0) {
                ArrayList<String> my_urls = new ArrayList<>();

                for (int i = 0; i < imageUris.size(); i++) {
                    StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                            + "." + getFileExtension(imageUris.get(i)));

                    uploadTask2 = fileReference.putFile(imageUris.get(i));
                    uploadTask2.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return fileReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener((OnCompleteListener<Uri>) task ->
                    {
                        if (task.isSuccessful()) {
                            Uri image = task.getResult();
                            String myurl = image.toString();
                            my_urls.add(myurl);

                            if (my_urls.size() == imageUris.size()) {
                                try {
                                    HashMap<String, Object> request_offer = new HashMap<>();
                                    db = FirebaseDatabase.getInstance();
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Requests");
                                    String requestId = reference.push().getKey();
                                    DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");

                                    request_offer.put("Title", offer_title_input);
                                    request_offer.put("Description", offer_description_input);
                                    request_offer.put("Category", chosen_category_input);
                                    request_offer.put("Images", my_urls);

                                    Date now = new Date();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                                    String date_now = dateFormat.format(now);

                                    Request send_request = new Request(requestId, offer_sender, offer_receiver, date_now,
                                            which_ad_i_am_interested, null, request_offer, false, time_of_appointment.format(format), chosen_date);

                                    String title_for_request = which_ad_i_am_interested.get("Title") + ": " + sender_username + "to " + receiver_username;
                                    reference.child(title_for_request).setValue(send_request);

                                    progressDialog.dismiss();

                                    startActivity(new Intent(MakeOffer.this, HomeActivity.class));

                                } catch (NullPointerException e) {
                                    Toast.makeText(MakeOffer.this, "!!!!!!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(MakeOffer.this, "Failed to post!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MakeOffer.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(MakeOffer.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void pickImagesIntent2()
    {
        Intent intent_for_images = new Intent();
        intent_for_images.setType("image/*");
        intent_for_images.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent_for_images.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent_for_images, "Select Image(s)"), PICK_REQUEST_IMAGES_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_REQUEST_IMAGES_CODE)
        {

            if (resultCode == Activity.RESULT_OK)
            {

                if(data.getClipData() != null)
                {

                    int count = data.getClipData().getItemCount();

                    if(count > 5)
                    {
                        count = 5;
                    }
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUris.add(imageUri);
                        Bitmap bitmap_of_image = null;
                        try
                        {
                            bitmap_of_image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        bitmap_of_image = Bitmap.createScaledBitmap(bitmap_of_image, images_size_for_recognition, images_size_for_recognition, false);
                        image_bitmaps.add(bitmap_of_image);
                    }

                    image_switcher_offer.setImageURI(imageUris.get(0));
                    position = 0;
                }
                else
                {
                    Uri imageUri = data.getData();
                    imageUris.add(imageUri);
                    Bitmap bitmap_of_image = null;
                    try
                    {
                        bitmap_of_image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    bitmap_of_image = Bitmap.createScaledBitmap(bitmap_of_image, images_size_for_recognition, images_size_for_recognition, false);
                    image_bitmaps.add(bitmap_of_image);
                    image_switcher_offer.setImageURI(imageUris.get(0));
                    position = 0;
                }
                String image_number_so_far = "Images: " + imageUris.size() + "/5";
                image_number_text.setText(image_number_so_far);
            }
        }
    }

    @Override
    public void onBackPressed() //if we press back in LoginActivity we can choose to close the app
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.drawable.logo_for_appeal);
        builder.setMessage("Cancel offer?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
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
    }

    public void showPop(View view, String error_message)
    {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.wrong_input_popup, null);
        builder.setView(customLayout);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        Button OK = customLayout.findViewById(R.id.OK);
        TextView errorMsg = customLayout.findViewById(R.id.error_message);
        errorMsg.setText(error_message);
        OK.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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