package com.example.mysignupapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/*
    AppointmentAdapter is used for viewing the saved appointments a user has
 */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder>
{
    Context context; // Context where the view will be visible
    List<Appointment> appointments; // List of all appointments
    FirebaseDatabase db; // Firebase database(necessary to find profile picture and details)
    FirebaseAuth mAuth; // Firebase authentication
    String firebase_link; // Link of the profile image
    private AppointmentAdapter.AppointmentClickListener listener; // Custom click listener

    public AppointmentAdapter(Context context, List<Appointment> appointments, AppointmentAdapter.AppointmentClickListener listener)
    {
        this.context = context;
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.appointment_item, parent, false);
        return new AppointmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        Appointment chosen_appointment = appointments.get(position);

        String current_id = mAuth.getCurrentUser().getUid();
        String sender_id = chosen_appointment.getSender_id();
        String receiver_id = chosen_appointment.getReceiver_id();

        // The appointment concerns two users
        DatabaseReference user_ref1 = db.getReference("Users/" + sender_id);
        DatabaseReference user_ref2 = db.getReference("Users/" + receiver_id);

        // Depending of who is the current user, it is logical in "My Appointments" view to show the other user's details as well as
        // what the user switches with them and what they give back
        if(current_id.equals(sender_id))
        {
            user_ref2.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    User user_now = snapshot.getValue(User.class);

                    if(user_now.getProfile_picture() != null)
                    {
                        if(!user_now.getProfile_picture().isEmpty())
                        {
                            firebase_link = user_now.getProfile_picture();
                            Picasso.get().load(firebase_link).into(holder.profile_picture_switcher);
                        }
                    }

                    String date_of_meeting = "Date: " + chosen_appointment.getAppointment_date();
                    holder.date.setText(date_of_meeting);
                    String hour_of_meeting = "Hour: " + chosen_appointment.getAppointment_hour();
                    holder.hour.setText(hour_of_meeting);
                    String person_of_meeting = "Switcher: " + user_now.getUsername();
                    holder.switcher.setText(person_of_meeting);
                    String ad_of_interest = "Interested for: " + chosen_appointment.getRequest().getAbout().get("Title").toString();
                    holder.interested.setText(ad_of_interest);

                    if(chosen_appointment.getRequest().getPrice_offer() != null)
                    {
                        String price_trade = "Trade with: " + chosen_appointment.getRequest().getPrice_offer();
                        holder.trade.setText(price_trade);
                    }
                    else if(chosen_appointment.getRequest().getTrade() != null)
                    {
                        String ad_trade =  "Trade with: " + chosen_appointment.getRequest().getTrade().get("Title").toString();
                        holder.trade.setText(ad_trade);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });
        }
        else if(current_id.equals(receiver_id))
        {
            user_ref1.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    User user_now = snapshot.getValue(User.class);

                    if(user_now.getProfile_picture() != null)
                    {
                        if(!user_now.getProfile_picture().isEmpty())
                        {
                            firebase_link = user_now.getProfile_picture();
                            Picasso.get().load(firebase_link).into(holder.profile_picture_switcher);
                        }
                    }

                    String date_of_meeting =  "Date: " + chosen_appointment.getAppointment_date();
                    holder.date.setText(date_of_meeting);
                    String hour_of_meeting =  "Hour: " + chosen_appointment.getAppointment_hour();
                    holder.hour.setText(hour_of_meeting);
                    String person_of_meeting = "Switcher: " + user_now.getUsername();
                    holder.switcher.setText(person_of_meeting);
                    String ad_of_interest = "Interested for: " + chosen_appointment.getRequest().getAbout().get("Title").toString();
                    holder.trade.setText(ad_of_interest);

                    if(chosen_appointment.getRequest().getPrice_offer() != null)
                    {
                        String price_trade = "Trade with: " + chosen_appointment.getRequest().getPrice_offer();
                        holder.interested.setText(price_trade);
                    }
                    else if(chosen_appointment.getRequest().getTrade() != null)
                    {
                        String ad_trade = "Trade with: " + chosen_appointment.getRequest().getTrade().get("Title").toString();
                        holder.interested.setText(ad_trade);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });
        }

    }

    @Override
    public int getItemCount()
    {
        return appointments.size();
    }


    public interface AppointmentClickListener
    {
        void onClick(View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        ImageView profile_picture_switcher;
        TextView date;
        TextView hour;
        TextView switcher;
        TextView interested;
        TextView trade;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            profile_picture_switcher = itemView.findViewById(R.id.switcher_profile_pic);
            date = itemView.findViewById(R.id.what_date);
            hour = itemView.findViewById(R.id.what_time);
            switcher = itemView.findViewById(R.id.switcher_name);
            interested = itemView.findViewById(R.id.interested_for);
            trade = itemView.findViewById(R.id.trade_with);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            listener.onClick(v, getAdapterPosition());
        }
    }
}
