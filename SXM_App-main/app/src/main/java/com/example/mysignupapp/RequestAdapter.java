package com.example.mysignupapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
    Context context;
    List<Request> requests;
    FirebaseDatabase db;
    String firebase_link;

    private RequestAdapter.RequestClickListener listener;

    public RequestAdapter(Context context, List<Request> requests, RequestAdapter.RequestClickListener listener) {
        this.context = context;
        this.requests = requests;
        this.listener = listener;
    }

    public interface RequestClickListener {
        void onClick(View v, int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_item, parent, false);
        return new RequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        db = FirebaseDatabase.getInstance();
        DatabaseReference user_ref = db.getReference("Users/" + requests.get(position).sender_id);

        user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user_now = snapshot.getValue(User.class);

                if (user_now.getProfile_picture() != null) {
                    if (!user_now.getProfile_picture().isEmpty()) {
                        firebase_link = user_now.getProfile_picture();
                        Picasso.get().load(firebase_link).into(holder.profile_picture);
                    }
                }
                holder.sender_name.setText(user_now.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        String when = "Date: " + requests.get(position).getAppointment_date();
        holder.date.setText(when);
        String time = "Time: " + requests.get(position).getAppointment_hour();
        holder.time.setText(time);
        holder.interested_at.setText(requests.get(position).getAbout().get("Title").toString());
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView date;
        TextView time;
        ImageView profile_picture;
        TextView sender_name;
        TextView interested_at;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.chosen_date);
            time = itemView.findViewById(R.id.chosen_hour);
            profile_picture = itemView.findViewById(R.id.sender_profile_pic);
            sender_name = itemView.findViewById(R.id.actual_name);
            interested_at = itemView.findViewById(R.id.this_one);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }
}
