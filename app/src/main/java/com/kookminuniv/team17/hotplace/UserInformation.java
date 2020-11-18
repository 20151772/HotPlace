package com.kookminuniv.team17.hotplace;

import android.location.Geocoder;
import android.location.Location;

import java.io.Serializable;

public class UserInformation implements Serializable {
    private static String user_id, address, goo;
    private static Location userLocation;
    private static boolean login;

    public void setAddress(String address) {
        this.address = address;
    }
    public void setUser_id(String user_id){
        this.user_id = user_id;
    }
    public void setUserLocation(Location userLocation){
        this.userLocation = userLocation;
    }
    public void setLogin(boolean login){
        this.login = login;
    }
    public void setGoo(String goo) {
        this.goo = goo;
    }

    public static String getAddress() {
        return address;
    }
    public static String getUser_id(){
        return user_id;
    }
    public static Location getUserLocation(){
        return userLocation;
    }
    public static boolean isLogin(){
        return login;
    }
    public static String getGoo() {
        return goo;
    }
}
