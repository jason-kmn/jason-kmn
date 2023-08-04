package com.example.mysignupapp;

public class LatLngCustom {
    double lat, lon;

    public LatLngCustom()
    {
    }

    public LatLngCustom(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
