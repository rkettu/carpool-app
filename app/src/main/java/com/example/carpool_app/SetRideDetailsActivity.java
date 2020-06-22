package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class SetRideDetailsActivity extends AppCompatActivity implements View.OnClickListener{


    private CheckBox checkBox_aika, checkBox_matkatavara;
    EditText lahtoaikaSanallinen, matkatavaraSanallinen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ride_details);

        checkBox_aika = (CheckBox) findViewById(R.id.setRideDetails_checkBox_aika);
        checkBox_matkatavara = (CheckBox) findViewById(R.id.setRideDetails_checkBox_matkatavarat);
        checkBox_aika.setOnClickListener(this);
        checkBox_matkatavara.setOnClickListener(this);
        lahtoaikaSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenAika);
        matkatavaraSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenTavaratila);
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
    }
}
