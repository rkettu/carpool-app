package com.example.carpool_app;

import android.os.AsyncTask;

public class FindRideASync extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... strings) {
        String startPoint = strings[0];
        String destination = strings[1];
        return null;
    }
}
