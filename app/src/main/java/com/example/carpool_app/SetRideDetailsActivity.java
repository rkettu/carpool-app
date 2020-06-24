package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class SetRideDetailsActivity extends AppCompatActivity implements Serializable, View.OnClickListener{

    //SetRideActivitystä siirretty data
    private List<String> allPoints;
    private List<String> selectedPoints;
    private String newLahtoOs, newLoppuOs,newMatka, newAika, newLahtoCity, newLoppuCity;

    private Calendar mC;
    private int passengers = 4;
    private float hinta = 0.03f;
    private int range;
    int pickUpDistance;

    private CheckBox checkBox_aika, checkBox_matkatavara;
    private int pickedYear, pickedMonth, pickedDate, pickedHour, pickedMinute;

    TextView hintaTxt, exampleTxt, minRange, rangeValueTextView;
    EditText txtDate, txtTime, lahtoaikaSanallinen, matkatavaraSanallinen, numberPicker;
    SeekBar seekBar, seekBar2, seekBar3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ride_details);

        mC = new GregorianCalendar();

        if(savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
            } else {
                newLahtoOs = extras.getString("ALKUOSOITE");
                newLoppuOs = extras.getString("LOPPUOSOITE");
                newMatka = extras.getString("DISTANCE");
                newAika = extras.getString("DURATION");
                allPoints = (List<String>) extras.getSerializable("ALLPOINTS");
                selectedPoints = (List<String>) extras.getSerializable("POINTS");

                Log.d("MYLOG", "onCreate: " + allPoints);

            }
        }else {
            newLahtoOs = (String) savedInstanceState.getSerializable ("ALKUOSOITE");
            newLoppuOs = (String) savedInstanceState.getSerializable ("LOPPUOSOITE");
            newMatka = (String) savedInstanceState.getSerializable ("DISTANCE");
            newAika = (String) savedInstanceState.getSerializable ("DURATION");
            allPoints = (List<String>) savedInstanceState.getSerializable("ALLPOINTS");
            selectedPoints = (List<String>) savedInstanceState.getSerializable("POINTS");
        }

        checkBox_aika = (CheckBox) findViewById(R.id.setRideDetails_checkBox_aika);
        checkBox_matkatavara = (CheckBox) findViewById(R.id.setRideDetails_checkBox_matkatavarat);
        checkBox_aika.setOnClickListener(this);
        checkBox_matkatavara.setOnClickListener(this);
        lahtoaikaSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenAika);
        matkatavaraSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenTavaratila);

        numberPicker = findViewById(R.id.setRideDetails_editText_matkustajat);
        final Button confirmBtn = (Button) findViewById(R.id.setRideDetails_button_vahvista);
        confirmBtn.setOnClickListener(this);

        txtDate = (EditText) findViewById(R.id.setRideDetails_editText_date);
        txtTime = (EditText) findViewById(R.id.setRideDetails_editText_time);
        txtDate.setOnClickListener(this);
        txtTime.setOnClickListener(this);

        hintaTxt = (TextView) findViewById(R.id.setRideDetails_textView_hinta);
        //exampleTxt = (TextView) findViewById(R.id.setRideDetails_textView);
        minRange = (TextView) findViewById(R.id.setRideDetails_textView_noutoEtaisyys);
        minRange.setText("Nouto etäisyys: " + 5 + "km");
        rangeValueTextView = (TextView) findViewById(R.id.setRideDetails_textView_minmatka);
        //rangeValueTextView.setText("Kuljettava matka: " + intMatka + "km");

        seekBar = (SeekBar) findViewById(R.id.setRideDetails_seekbar_hinta);
        seekBar2 = (SeekBar) findViewById(R.id.setRideDetails_seekbar_matka);
        seekBar3 = (SeekBar) findViewById(R.id.setRideDetails_seekbar_nouto);

        //Seekbarien onBarChangeListenerit
        //Hinta:
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                hinta = ((float) progress / 1000);
                hintaTxt.setText(String.format("Hinta: %.3f", hinta) + " per kilometeri");
                //hintaTxt.setTextColor(Color.WHITE);
                //exampleTxt.setText("Example km: " + newMatka + " km \n" + "Price: " + String.format("%.2f", doubleMatka * hinta) + " eur");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Nouto matka
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //float testi = ((intMatka / 100.00f) * progress);
                //range = progress;
                //DecimalFormat df = new DecimalFormat("#.##");
                //rangeValueTextView.setText("Minimum Passanger trip: " + (int)testi + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Vähimmäis kuljettava matka
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float tempDist = ((50 / 100.00f) * progress);
                pickUpDistance = (int) tempDist;
                range = pickUpDistance;
                //Log.d("####matka3####", range + ", " + intMatka + ", " + progress + ", " + (intMatka / 100.00f) * progress);
                minRange.setText("Nouto etäisyys: " + range + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(checkBox_aika.isChecked())
        {
            lahtoaikaSanallinen.setVisibility(View.VISIBLE);
        }
        else
        {
            lahtoaikaSanallinen.setVisibility(View.GONE);
        }
        if(checkBox_matkatavara.isChecked())
        {
            matkatavaraSanallinen.setVisibility(View.VISIBLE);
        }
        else
        {
            matkatavaraSanallinen.setVisibility(View.GONE);
        }
        if(v.getId() == R.id.setRideDetails_button_vahvista)
        {

        }
    }
}
