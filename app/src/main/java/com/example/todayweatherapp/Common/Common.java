package com.example.todayweatherapp.Common;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {
    public static final String APP_ID = "8d5778d68c1e85d76dbe7c604b60134d";
    public static Location CURRENT_LOCATION = null;

    public static String convertUnixToDate(int dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EE dd-MM-yyyy");
        String formatted = sdf.format(date);
        return formatted;
    }
    public static String convertUnixToDateTime(int dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm EE dd-MM-yyyy");
        String formatted = sdf.format(date);
        return formatted;
    }
    public static String convertUnixToHour(int time) {
        Date date = new Date(time*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formatted = sdf.format(date);
        return formatted;
    }
}
