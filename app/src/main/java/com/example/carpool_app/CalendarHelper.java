package com.example.carpool_app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarHelper {

    // DD/MM/YY HH:MM
    public static String getFullTimeString(long timeInMillis)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(timeInMillis);
        String minuteString = c.get(Calendar.MINUTE) < 10 ? ("0" + c.get(Calendar.MINUTE)) : Integer.toString(c.get(Calendar.MINUTE));
        String timeString = c.get(Calendar.DAY_OF_MONTH)+"."+(c.get(Calendar.MONTH)+1)+"."+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR_OF_DAY)+":"+minuteString;
        return timeString;
    }

    // DD/MM/YYYY
    public static String getDateTimeString(long timeInMillis)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(timeInMillis);
        String timeString = c.get(Calendar.DAY_OF_MONTH)+"."+(c.get(Calendar.MONTH)+1)+"."+c.get(Calendar.YEAR);
        return timeString;
    }

    // HH:MM
    public static String getHHMMString(long timeInMillis)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(timeInMillis);
        String minuteString = c.get(Calendar.MINUTE) < 10 ? ("0" + c.get(Calendar.MINUTE)) : Integer.toString(c.get(Calendar.MINUTE));
        String timeString = c.get(Calendar.HOUR_OF_DAY)+":"+minuteString;
        return timeString;
    }

    public static int getDayString(long timeInMillis)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(timeInMillis);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    public static int getMonthString(long timeInMillis)
    {
        Calendar c= new GregorianCalendar();
        c.setTimeInMillis(timeInMillis);
        int month = c.get(Calendar.MONTH + 1);
        return month;
    }

    public static int getYearString(long timeInMillis)
    {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(timeInMillis);
        int year = c.get(Calendar.YEAR);
        return year;
    }

    // Time in millis
    public static String getTimeInMillis()
    {
        return getTimeInMillis();
    }
}
