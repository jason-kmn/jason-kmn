package com.example.mysignupapp;

import java.io.Serializable;
import java.util.ArrayList;

/*
    The Ad class is used to save all ads one user creates
    In Firebase, the Ad type objects are put inside Users/"user id"/ads
 */
public class Ad implements Serializable
{
    String title; // Ad's title
    String category; // Ad's category
    String price; // Ad's price. It can be Free(0$) in case the user prefers to switch only
    String description; //Ad's description
    ArrayList<String> categories_for_switching = new ArrayList<>(); // All categories the user prefers to switch their ad
    ArrayList<String> images = new ArrayList<>(); // List of the ad's images. They can be from 1 to 5
    LatLngCustom coordinates;

    Ad(){} // Default constructor of an Ad

    public Ad(String title, String category, String price, ArrayList<String> categories_for_switching, ArrayList<String> images, String description, LatLngCustom coords)
    {
        //custom constructor of the Ad
        this.title = title;
        this.category = category;
        this.price = price;
        this.categories_for_switching = categories_for_switching;
        this.images = images;
        this.coordinates = coords;
        this.description = description;
    }
//------------------------------------------SETTERS AND GETTERS-----------------------------------------------------------
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public ArrayList<String> getCategories_for_switching() {
        return categories_for_switching;
    }

    public String getCategories(){
        String categories = "";
        int size = categories_for_switching.size(), i=0;
        for(String s: categories_for_switching){
            i++;
            if(i!=size){
                categories = categories + s + ", ";
            }
            else{
                categories = categories + s;
            }

        }

        return categories;
    }

    public void setCategories_for_switching(ArrayList<String> categories_for_switching) {
        this.categories_for_switching = categories_for_switching;
    }

    public ArrayList<String> getImages() {
        return images;
    }
    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public LatLngCustom getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLngCustom coordinates) {
        this.coordinates = coordinates;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
//------------------------------------------------------------------------------------------------------------------------
}
