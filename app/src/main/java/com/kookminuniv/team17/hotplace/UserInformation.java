package com.kookminuniv.team17.hotplace;

import android.location.Geocoder;
import android.location.Location;

import java.io.Serializable;

public class UserInformation implements Serializable {
    private static String user_id;
    private static Location userLocation;
    private static boolean login;

    public void setUser_id(String user_id){
        this.user_id = user_id;
    }
    public void setUserLocation(Location userLocation){
        this.userLocation = userLocation;
    }
    public void setLogin(boolean login){
        this.login = login;
    }

    public String getUser_id(){
        return user_id;
    }
    public Location getUserLocation(){
        return userLocation;
    }
    public boolean isLogin(){
        return login;
    }
}
