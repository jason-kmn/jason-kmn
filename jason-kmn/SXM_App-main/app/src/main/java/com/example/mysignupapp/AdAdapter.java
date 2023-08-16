package com.example.mysignupapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
    The AdAdapter is used to display all ads in the HomePage. It is necessary for the recycler view of all ads we put in the
    activity_home.xml file
 */

public class AdAdapter extends RecyclerView.Adapter<AdAdapter.ViewHolder>
{
    private List<HashMap<String, Object>> ads; // First we need the all ads from the database
    private Context mcontext; // The context where the adapter will be put (HomeActivity.this / getApplicationContext())
    private AdViewClickListener listener; // Custom Click listener for all ads' views

    // Custom constructor of the adapter
    public AdAdapter(Context context, List<HashMap<String, Object>> ads, AdViewClickListener listener)
    {
        this.ads = ads;
        this.mcontext = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        //This method is used to tell the adapter how each ad will be viewed
        View view = LayoutInflater.from(mcontext).inflate(R.layout.home_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        //This method is used to combine one ad's details with the ViewHolder we have set
        HashMap<String, Object> ad = ads.get(position); // We get one ad from Firebase Ads/

        ArrayList<String> images_views = (ArrayList<String>) ad.get("Images"); // we take all ad's images

        ArrayList<String> switches = (ArrayList<String>) ad.get("Switch"); // as well as the prefferable categories of switching if they exist

 //---------------------------------------------------SET AD'S DETAILS IN THE VIEWHOLDER--------------------------------------------------------

        if(images_views != null && images_views.size() > 0) // we check first if there are any images
        {
            String preview = images_views.get(0); // we get the link of the image
            Picasso.get().load(preview).into(holder.image); // we use the Picasso method to draw the image in the ViewHolder ImageView
        }
        else // if there are not any pictures
        {
            holder.image.setImageResource(R.drawable.no_image_input); // we set a default image to avoid NullPointerException errors
        }

        if(ad.get("Title") != null)
        {
            holder.title.setText(ad.get("Title").toString()); // we set the title in the TextView
        }
        else
        {
            holder.title.setText("No title");
        }

        if(ad.get("Category") != null)
        {
            holder.category.setText(ad.get("Category").toString());
        }
        else
        {
            holder.category.setText("No category");
        }

        if(ad.get("Price") != null)
        {
            holder.price.setText(ad.get("Price").toString());
        }
        else
        {
            holder.price.setText("No price");
        }

        if(switches != null && switches.size() > 0)
        {
            holder.switchable.setVisibility(View.VISIBLE);
        }
//---------------------------------------------------------------------------------------------------------------------------------------------
    }

    @Override
    public int getItemCount() {
        return ads.size(); // This method returns all Ad items inside the Adapter
    }

    public interface AdViewClickListener // Interface used for our click Listener
    {
        void onClick(View v, int position); // method used in case we click
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        //ViewHolder is used to set all widgets the ad's view will have
        //In our case where we use the home_row_item.xml:
        ImageView image; // We have one image where we'll put the first image of the ad
        TextView title; // Then its title
        TextView category; // Its category
        TextView price; // Its price, either Free or with cost

        ImageView switchable; // This image is an small icon which tells if one ad prefers switching or not

        public ViewHolder(@NonNull View itemView) // We use all elements we created inside the home_row_item.xml file
        {
            super(itemView);
            image = itemView.findViewById(R.id.ad_image);
            title = itemView.findViewById(R.id.ad_title_1);
            category = itemView.findViewById(R.id.ad_category);
            price = itemView.findViewById(R.id.ad_price);
            switchable = itemView.findViewById(R.id.ad_switch);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) // if we click one item from the adapter we all the interface we have created
        {
            listener.onClick(v, getAdapterPosition());

        }
    }
}

