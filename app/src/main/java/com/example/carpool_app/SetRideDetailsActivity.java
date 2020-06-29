package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class SetRideDetailsActivity extends AppCompatActivity implements Serializable, View.OnClickListener{

    //SetRideActivitystä siirretty data
    private List<HashMap<String,String>> selectedPoints;
    private String startAddress, endAddress, duration, distance, startCity, endCity;

    private Calendar mC;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private int pickedYear, pickedMonth, pickedDate, pickedHour, pickedMinute;
    private String strDate, strTime;

    private float price = 0.03f;
    int pickUpDistance = 5;
    int passengers = 1;

    Double doubleDistance;
    int intDistance;

    private CheckBox checkBox_time, checkBox_luggage;

    private HashMap<String,String> bounds;


    TextView priceTxt, exampleTxt, minRange, rangeValueTextView;
    EditText txtDate, txtTime, lahtoaikaSanallinen, matkatavaraSanallinen;
    NumberPicker numberPicker;
    SeekBar seekBar3, seekBar2, seekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ride_details);

        mC = new GregorianCalendar();

        //Reitin tietojen vastaanotto setRideActivitystä
        if(savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
            } else {
                startAddress = extras.getString("ALKUOSOITE");
                endAddress = extras.getString("LOPPUOSOITE");
                startCity = extras.getString("STARTCITY");
                endCity = extras.getString("ENDCITY");
                distance = extras.getString("DISTANCE");
                duration = extras.getString("DURATION");
                selectedPoints = (List<HashMap<String,String>>) extras.getSerializable("POINTS");
                bounds = (HashMap<String,String>) extras.getSerializable("BOUNDS");
            }
        }else {
            startAddress = (String) savedInstanceState.getSerializable ("ALKUOSOITE");
            endAddress = (String) savedInstanceState.getSerializable ("LOPPUOSOITE");
            startCity = (String) savedInstanceState.getSerializable("STARTCITY");
            endCity = (String) savedInstanceState.getSerializable("ENDCITY");
            distance = (String) savedInstanceState.getSerializable ("DISTANCE");
            duration = (String) savedInstanceState.getSerializable ("DURATION");
            selectedPoints = (List<HashMap<String,String>>) savedInstanceState.getSerializable("POINTS");
        }

        //Muuttaa string distancen eri muuttujatyypeiksi
        if(distance != null && !distance.isEmpty()){
            doubleDistance = Double.valueOf(distance);
            intDistance = Integer.valueOf(doubleDistance.intValue());
        }

        checkBox_time = (CheckBox) findViewById(R.id.setRideDetails_checkBox_aika);
        checkBox_luggage = (CheckBox) findViewById(R.id.setRideDetails_checkBox_matkatavarat);
        checkBox_time.setOnClickListener(this);
        checkBox_luggage.setOnClickListener(this);
        lahtoaikaSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenAika);
        matkatavaraSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenTavaratila);


        final Button confirmBtn = (Button) findViewById(R.id.setRideDetails_button_vahvista);
        confirmBtn.setOnClickListener(this);

        txtDate = (EditText) findViewById(R.id.setRideDetails_editText_date);
        txtTime = (EditText) findViewById(R.id.setRideDetails_editText_time);
        txtDate.setOnClickListener(this);
        txtTime.setOnClickListener(this);

        priceTxt = (TextView) findViewById(R.id.setRideDetails_textView_hinta);
        //exampleTxt = (TextView) findViewById(R.id.setRideDetails_textView);
        minRange = (TextView) findViewById(R.id.setRideDetails_textView_noutoEtaisyys);
        minRange.setText("Nouto etäisyys: " + 5 + "km");
        rangeValueTextView = (TextView) findViewById(R.id.setRideDetails_textView_minmatka);
        //rangeValueTextView.setText("Kuljettava matka: " + intMatka + "km");

        //Number Picker matkustajien määritys
        numberPicker = findViewById(R.id.setRideDetails_numberPicker_passengers);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                passengers = newVal;
            }
        });


        //Seekbarien onBarChangeListenerit
        seekBar = (SeekBar) findViewById(R.id.setRideDetails_seekbar_nouto);
        seekBar2 = (SeekBar) findViewById(R.id.setRideDetails_seekbar_matka);
        seekBar3 = (SeekBar) findViewById(R.id.setRideDetails_seekbar_hinta);

        //Nouto etäisyys (pickUpDistance)
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float tempDist = ((50 / 100.00f) * progress);
                pickUpDistance = (int) tempDist;
                //Log.d("####matka3####", range + ", " + intMatka + ", " + progress + ", " + (intMatka / 100.00f) * progress);
                minRange.setText("Nouto etäisyys: " + pickUpDistance + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Kuljettava matka
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float testi = ((intDistance / 100.00f) * progress);
                DecimalFormat df = new DecimalFormat("#.##");
                rangeValueTextView.setText("Kuljettava matka: " + (int)testi + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Hinta:
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                price = ((float) progress / 1000);
                priceTxt.setText(String.format("Hinta: %.3f", price) + " per kilometri");
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
    }

    @Override
    public void onClick(View v) {
        if (v == txtDate) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            strDate = (dayOfMonth + "-" + (month + 1) + "-" + year);
                            txtDate.setText(strDate);
                            pickedYear = year;
                            pickedMonth = month;
                            pickedDate = dayOfMonth;
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }else if (v == txtTime) {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String format = "%1$02d";
                            String estHour = String.format(format, hourOfDay);
                            String estMin = String.format(format, minute);
                            strTime = (estHour + ":" + estMin);
                            txtTime.setText(strTime);
                            pickedHour = hourOfDay;
                            pickedMinute = minute;
                        }
                    }, mHour, mMinute, true);

            timePickerDialog.show();
        }

        if(checkBox_time.isChecked())
        {
            lahtoaikaSanallinen.setVisibility(View.VISIBLE);
        }
        else
        {
            lahtoaikaSanallinen.setVisibility(View.GONE);
        }
        if(checkBox_luggage.isChecked())
        {
            matkatavaraSanallinen.setVisibility(View.VISIBLE);
        }
        else
        {
            matkatavaraSanallinen.setVisibility(View.GONE);
        }
        if(v.getId() == R.id.setRideDetails_button_vahvista)
        {
          
            //mC.set(pickedYear, pickedMonth, pickedDate, pickedHour, pickedMinute);
            //long leaveTime = mC.getTimeInMillis();


            //Log.d("mylog", "onClick VAHVISTA " + " selectedPoints.size: " + selectedPoints.size() + " Duration: " + duration + " Distance: " + distance + " startAdr: " + startAddress + " endAdr: " + endAddress + " startCity: " + startCity + " endCity: " + endCity + " Passengers: " + passengers + " LeaveTime: " + leaveTime + " Hinta: " + price + " Noutomatka: " + pickUpDistance);
            CREATE_RIDE_DEMO();
        }
    }

    // TODO: DELETE THIS after proper implementation
    // now only creates rides with proper points and user but otherwise random values
    public void CREATE_RIDE_DEMO()
    {
        Ride r = new Ride(FirebaseHelper.getUid(), duration, (new GregorianCalendar().getTimeInMillis()+40*Constant.DayInMillis),
                            startAddress, endAddress, passengers, price, doubleDistance,
                            selectedPoints, bounds, new ArrayList<String>(),
                            new ArrayList<String>(), 5, startCity, endCity);
        FirebaseFirestore.getInstance().collection("rides").add(r).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful())
                {
                    // Ride creation succesfull
                    Toast.makeText(SetRideDetailsActivity.this, "RIDE CREATED", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Ride create failed
                    Toast.makeText(SetRideDetailsActivity.this, "RIDE CREATE FAIL", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
