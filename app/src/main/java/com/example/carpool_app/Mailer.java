package com.example.carpool_app;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class Mailer {
    private static String baseUrl = "https://carpooling-emails.herokuapp.com/";

    // "We regret to inform you that your ride " + rideStartDestination + " on " + rideDate + " has been canceled"
    public static void SendRideCanceledEmail(Context context, String targetEmail, String rideStartDestination, String rideDate)
    {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = baseUrl + "send-ride-canceled-email";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", targetEmail);
            jsonBody.put("rideStartDestination", rideStartDestination);
            jsonBody.put("rideDate", rideDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Send(queue, jsonBody, url);
    }


    // "User " + fname + " has joined your ride " + rideStartDestination + " on " + rideDate;
    public static void SendUserLeftEmail(Context context, String targetEmail, String fname, String rideStartDestination, String rideDate)
    {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = baseUrl + "send-user-left-email";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", targetEmail);
            jsonBody.put("fname", fname);
            jsonBody.put("rideStartDestination", rideStartDestination);
            jsonBody.put("rideDate", rideDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Send(queue, jsonBody, url);
    }

    // "User " + fname + " has left your ride " + rideStartDestination + " on " + rideDate;
    public static void SendUserJoinedEmail(Context context, String targetEmail, String fname, String rideStartDestination, String rideDate)
    {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = baseUrl + "send-user-joined-email";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", targetEmail);
            jsonBody.put("fname", fname);
            jsonBody.put("rideStartDestination", rideStartDestination);
            jsonBody.put("rideDate", rideDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Send(queue, jsonBody, url);
    }


    private static void Send(RequestQueue queue, JSONObject jsonBody, String url)
    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("VOLLEY", "success:" + response.toString().substring(0,100));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VOLLEY", "error");
            }
        });
        queue.add(jsonObjectRequest);
    }
}
