package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
    int pickUpDistance = 10;
    int passengers = 1;
    int minRangeInt = 20;

    Double doubleDistance;
    int intDistance;

    private CheckBox checkBox_time, checkBox_luggage, checkBox_pets;
    private Boolean pets;

    private HashMap<String,String> bounds;


    TextView priceTxt, examplePriceTxt, fetchRange, rangeValueTextView;
    EditText txtDate, txtTime, lahtoaikaSanallinen, matkatavaraSanallinen;
    private String departureTimeTxt, luggageTxt;
    NumberPicker numberPicker;
    SeekBar seekBar3, seekBar2, seekBar;
    Button confirmBtn;
    ImageButton timeBtn, luggageBtn, picUpBtn, rangeBtn, priceBtn;

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
            bounds = (HashMap<String, String>) savedInstanceState.getSerializable("BOUNDS");
        }

        //Muuttaa string distancen eri muuttujatyypeiksi
        if(distance != null && !distance.isEmpty()){
            doubleDistance = Double.valueOf(distance);
            intDistance = Integer.valueOf(doubleDistance.intValue());
        }

        //CheckBoxit
        checkBox_time = (CheckBox) findViewById(R.id.setRideDetails_checkBox_aika);
        checkBox_luggage = (CheckBox) findViewById(R.id.setRideDetails_checkBox_matkatavarat);
        checkBox_pets = (CheckBox) findViewById(R.id.setRideDetails_checkBox_lemmikit);
        checkBox_time.setOnClickListener(this);
        checkBox_luggage.setOnClickListener(this);
        checkBox_pets.setOnClickListener(this);

        //Buttonit
        confirmBtn = (Button) findViewById(R.id.setRideDetails_button_vahvista);
        confirmBtn.setOnClickListener(this);
        confirmBtn.setEnabled(false);

        //ImageButtonit
        timeBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_lahtoaika);
        luggageBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_tavaratila);
        picUpBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_nouto);
        rangeBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_matka);
        priceBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_hinta);
        timeBtn.setOnClickListener(this);
        luggageBtn.setOnClickListener(this);
        picUpBtn.setOnClickListener(this);
        rangeBtn.setOnClickListener(this);
        priceBtn.setOnClickListener(this);

        //Editorit
        txtDate = (EditText) findViewById(R.id.setRideDetails_editText_date);
        txtTime = (EditText) findViewById(R.id.setRideDetails_editText_time);
        lahtoaikaSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenAika);
        matkatavaraSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenTavaratila);
        txtDate.setOnClickListener(this);
        txtTime.setOnClickListener(this);

        //TextWatcherit varmistaa että lähtöpäivä ja -aika on määritetty
        txtDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!txtTime.getText().toString().equals(""))
                {
                    confirmBtn.setEnabled(true);
                }else{
                    confirmBtn.setEnabled(false);
                }
            }
        });
        txtTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!txtDate.getText().toString().equals(""))
                {
                    confirmBtn.setEnabled(true);
                }else{
                    confirmBtn.setEnabled(false);
                }
            }
        });

        //TextViewit
        priceTxt = (TextView) findViewById(R.id.setRideDetails_textView_hinta);
        examplePriceTxt = (TextView) findViewById(R.id.setRideDetails_textView_examplePrice);
        fetchRange = (TextView) findViewById(R.id.setRideDetails_textView_noutoEtaisyys);
        rangeValueTextView = (TextView) findViewById(R.id.setRideDetails_textView_minmatka);
        examplePriceTxt.setText("Example km: " + distance + " km \n" + "Price: " + String.format("%.2f", doubleDistance * 0.03) + " euroa");
        rangeValueTextView.setText("Kuljettava matka: " + minRangeInt + "km");
        fetchRange.setText("Nouto etäisyys: " + pickUpDistance + "km");
        priceTxt.setText(String.format("Hinta: %.3f", price) + " per kilometri");

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
                fetchRange.setText("Nouto etäisyys: " + pickUpDistance + "km");
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
                minRangeInt = (int) ((intDistance / 100.00f) * progress);
                DecimalFormat df = new DecimalFormat("#.##");
                rangeValueTextView.setText("Kuljettava matka: " + minRangeInt + " km");
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
                examplePriceTxt.setText("Esimerkki km: " + distance + " km \n" + "Hinta: " + String.format("%.2f", doubleDistance * price) + " euroa");

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
        else if (v == timeBtn)
        {
            AlertDialog.Builder time = new AlertDialog.Builder(SetRideDetailsActivity.this);
            time.setTitle("Lähtöaikani voi muuttua:");
            time.setMessage("Etkö tiedä lähtöaikaasi, ei hätää! Voit merkata lähtöaika kenttään karkean arvion lähtöajastasi ja kirjoittaa huomio kenttään viestin, mikä näkyy matkustajille kyytejä etsiessä");
            time.setCancelable(true);

            time.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = time.create();
            alert11.show();
        }
        else if (v == luggageBtn)
        {
            AlertDialog.Builder luggage = new AlertDialog.Builder(SetRideDetailsActivity.this);
            luggage.setTitle("Rajallinen tavaratila:");
            luggage.setMessage("Mikäli tavaratilasi on rajallinen, voit kirjoittaa kenttään viestin kyytejä varaaville, esimerkiksi: Kyytiin mahtuu vain sylissä kuljetettavat matkatavarat");
            luggage.setCancelable(true);

            luggage.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = luggage.create();
            alert11.show();
        }
        else if (v == picUpBtn)
        {
            AlertDialog.Builder time = new AlertDialog.Builder(SetRideDetailsActivity.this);
            time.setTitle("Nouto etäisyys:");
            time.setMessage("Valitse kuinka paljon voit poiketa reitiltä noutaessasi matkustajaa.");
            time.setCancelable(true);

            time.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = time.create();
            alert11.show();
        }
        else if (v == rangeBtn)
        {
            AlertDialog.Builder range = new AlertDialog.Builder(SetRideDetailsActivity.this);
            range.setTitle("Kuljettava matka:");
            range.setMessage("Voit asettaa kilometrirajan minkä kyytiläisen tulee vähintään matkustaa kyydissäsi.");
            range.setCancelable(true);

            range.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = range.create();
            alert11.show();
        }
        else if (v == priceBtn)
        {
            AlertDialog.Builder price = new AlertDialog.Builder(SetRideDetailsActivity.this);
            price.setTitle("Hinta per kilometri");
            price.setMessage("Määritä haluamasi kilometrihinta. Matkan kokonaishinta määräytyy kyytiläisen matkan pituuden ja kilometrihinnan mukaan. Esimerkki laatikosta näet paljonko yhden kyytiläisen matka maksaisi koko ilmoittamasi matkan pituudella. HUOM! Määritäthän hinnan vain polttoaineiden jakamista varten.");
            price.setCancelable(true);

            price.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = price.create();
            alert11.show();
        }

        if(checkBox_time.isChecked()) {
            lahtoaikaSanallinen.setVisibility(View.VISIBLE);
        } else {
            lahtoaikaSanallinen.setVisibility(View.GONE);
            lahtoaikaSanallinen.setText("");
            departureTimeTxt = "";
        }
        if(checkBox_luggage.isChecked()) {
            matkatavaraSanallinen.setVisibility(View.VISIBLE);
        } else {
            matkatavaraSanallinen.setVisibility(View.GONE);
            matkatavaraSanallinen.setText("");
            luggageTxt = "";
        }
        if(checkBox_pets.isChecked()) {
            pets = true;
        } else {
            pets = false;
        }
        if(v.getId() == R.id.setRideDetails_button_vahvista)
        {
          
            //mC.set(pickedYear, pickedMonth, pickedDate, pickedHour, pickedMinute);
            //long leaveTime = mC.getTimeInMillis();

            departureTimeTxt = lahtoaikaSanallinen.getText().toString();
            luggageTxt = matkatavaraSanallinen.getText().toString();

            String departure = "";
            String luggage = "";
            String sPets = "";
            if(departureTimeTxt.length() == 0){ departureTimeTxt = null; }else {
                departure = "Lähtöaika viesti: " + departureTimeTxt;
            }
            if(luggageTxt.length() == 0) { luggageTxt = null; }else {
                luggage = "Matkatavara viesti: " + luggageTxt;
            }
            if(pets == true){
                sPets = "Sallittu";
            }else{
                sPets = "Ei sallittu";
            }

            if(FirebaseHelper.loggedIn)
            {
                AlertDialog.Builder confirm = new AlertDialog.Builder(SetRideDetailsActivity.this);
                confirm.setTitle("Tarkista tiedot ja vahvista");
                confirm.setMessage("Lähöpäivä: " + strDate + "\nLähtöaika: " + strTime + "\nVapaat paikat: " + passengers + "\nNouto etäisyys: " + pickUpDistance + " km" +
                "\nKuljettava matka: " + minRangeInt + " km" + "\nHinta per km: " + price + "\nLemmikit: " + sPets + "\n" + departure + "\n" + luggage + "\n\nOvatko tiedot oikein? " );
                confirm.setCancelable(true);

                confirm.setPositiveButton(
                        "Kyllä",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CREATE_RIDE_DEMO();
                            }
                        }
                );
                confirm.setNegativeButton(
                        "Ei",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }
                );
                AlertDialog alert11 = confirm.create();
                alert11.show();
            }
            else
            {
                FirebaseHelper.GoToLogin(getApplicationContext());
            }
        }
    }

    // TODO: DELETE THIS after proper implementation
    // now only creates rides with proper points and user but otherwise random values
    public void CREATE_RIDE_DEMO()
    {

        Ride r = new Ride(FirebaseHelper.getUid(), duration, (new GregorianCalendar().getTimeInMillis()+40*Constant.DayInMillis),
                            startAddress, endAddress, passengers, price, doubleDistance,
                            selectedPoints, bounds, new ArrayList<String>(),
                            new ArrayList<String>(), 5, startCity, endCity, departureTimeTxt, luggageTxt, pets);
        FirebaseFirestore.getInstance().collection("rides").add(r).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful())
                {
                    // Ride creation succesfull
                    showCustomDialogSuccessful();
                }
                else {
                    // Ride create failed
                    showCustomDialogFailed();
                }
            }
        });

    }

    private void showCustomDialogSuccessful() {
        ViewGroup viewGroup =  findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.my_alertdialog, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Button okBtn = (Button) dialogView.findViewById(R.id.my_alertdialog_buttonOk);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        alertDialog.show();
    }

    private void showCustomDialogFailed() {
        ViewGroup viewGroup2 =  findViewById(android.R.id.content);
        View dialogView2 = LayoutInflater.from(this).inflate(R.layout.my_alertdialog2, viewGroup2, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView2);
        AlertDialog alertDialog2 = builder.create();
        Button okBtn2 = (Button) dialogView2.findViewById(R.id.my_alertdialog2_buttonOk);
        okBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        alertDialog2.show();
    }
}
