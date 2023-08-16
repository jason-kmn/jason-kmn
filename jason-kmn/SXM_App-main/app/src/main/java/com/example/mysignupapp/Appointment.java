package com.example.mysignupapp;

public class Appointment
{
    /*
        The Appointment class is used to display all user's appointments in case
        they came in a agreement with another user for switching
        These objects are saved in Firebase Appointments/
     */
    Request request; // The request which was accepted for the appointment's creation
    String sender_id; // the id of the request's sender
    String sender_username; // the username of the request's sender
    String receiver_id; // the id of the request's receiver
    String receiver_username; // the username of the request's receiver
    String appointment_hour; // the hour the users agreed to meet
    String appointment_date; // the day the users agreed to meet

    public Appointment(){} // default constructor of the appointment

    public Appointment(Request request, String sender_id, String sender_username, String receiver_id, String receiver_username,
                       String appointment_hour, String appointment_date) // custom constructor of the appointment
    {
        this.request = request;
        this.sender_id = sender_id;
        this.sender_username = sender_username;
        this.receiver_id = receiver_id;
        this.receiver_username = receiver_username;
        this.appointment_hour = appointment_hour;
        this.appointment_date = appointment_date;
    }

//------------------------------------------SETTERS AND GETTERS-----------------------------------------------------------
    public Request getRequest()
    {
        return request;
    }

    public void setRequest(Request request)
    {
        this.request = request;
    }

    public String getSender_id()
    {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getSender_username() {
        return sender_username;
    }

    public void setSender_username(String sender_username) {
        this.sender_username = sender_username;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getReceiver_username() {
        return receiver_username;
    }


    public void setReceiver_username(String receiver_username) {
        this.receiver_username = receiver_username;
    }

    public String getAppointment_hour() {
        return appointment_hour;
    }

    public void setAppointment_hour(String appointment_hour) {
        this.appointment_hour = appointment_hour;
    }

    public String getAppointment_date() {
        return appointment_date;
    }

    public void setAppointment_date(String appointment_date) {
        this.appointment_date = appointment_date;
    }
//------------------------------------------------------------------------------------------------------------------------
}
