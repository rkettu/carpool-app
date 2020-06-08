package com.example.carpool_app;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarHelper {

    // Takes UNIX timestamp
    // Returns time string in the following format:
    // DD/MM/YY HH:MM
    public static String getFullTimeString(long timeInMillis)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(timeInMillis);
        String minuteString = c.get(Calendar.MINUTE) < 10 ? ("0" + c.get(Calendar.MINUTE)) : Integer.toString(c.get(Calendar.MINUTE));
        String timeString = c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR_OF_DAY)+":"+minuteString;
        return timeString;
    }

    // Takes UNIX timestamp
    // Returns time string in the following format:
    // DD/MM/YY HH:MM
    public static String getDateTimeString(long timeInMillis)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(timeInMillis);
        String timeString = c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR);
        return timeString;
    }

    // Takes UNIX timestamp
    // Returns time string in the following format:
    // DD/MM/YY HH:MM
    public static String getHHMMString(long timeInMillis)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(timeInMillis);
        String minuteString = c.get(Calendar.MINUTE) < 10 ? ("0" + c.get(Calendar.MINUTE)) : Integer.toString(c.get(Calendar.MINUTE));
        String timeString = c.get(Calendar.HOUR_OF_DAY)+":"+minuteString;
        return timeString;
    }

    // Takes UNIX timestamp
    // Returns remaining time from current time to timestamp in format:
    // days and hours OR hours and minutes
    // Depending on if there's less or more than 24 hours to timestamp
    public static String getTimeUntil(long timeInMillis)
    {
        Calendar c = new GregorianCalendar();
        long currentTime = c.getTimeInMillis();
        long timeDifference = timeInMillis - currentTime;
        String retStr = "Error";

        // if time has already passed
        if(timeDifference < 0) return retStr;

        // return string changes format based on time difference
        if(timeDifference < Constant.DayInMillis) {
            // Less than day remaining
            long minutes = timeDifference / Constant.MinuteInMillis;
            long hours = minutes / 60;
            retStr = hours + "h " + (minutes%60) + "min";
        } else {
            // More than day remaining
            long hours = timeDifference / Constant.HourInMillis;
            long days = hours / 24;
            retStr = days + "d " + (hours%24) + "h";
        }

        return retStr;
    }

}
