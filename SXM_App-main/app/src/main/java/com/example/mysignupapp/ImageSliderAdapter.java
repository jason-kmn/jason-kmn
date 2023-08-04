package com.example.mysignupapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder>
{
    private List<Image_For_Slider> items;
    private ViewPager2 viewPager2;

    public ImageSliderAdapter(List<Image_For_Slider> items, ViewPager2 viewPager2)
    {
        this.items = items;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public ImageSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ImageSliderViewHolder
                (
                LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_item_container, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSliderViewHolder holder, int position)
    {
        holder.setImage(items.get(position));

        if(position == items.size() - 2)
        {
            viewPager2.post(runnable);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ImageSliderViewHolder extends RecyclerView.ViewHolder
    {
        private RoundedImageView imageView;

        public ImageSliderViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }

        void setImage(Image_For_Slider image_item)
        {
            Picasso.get().load(image_item.getFile_path()).into(imageView);
        }
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            items.addAll(items);
            notifyDataSetChanged();
        }
    };
}
