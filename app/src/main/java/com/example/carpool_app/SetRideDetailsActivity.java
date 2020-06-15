package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class SetRideDetailsActivity extends AppCompatActivity implements View.OnClickListener{


    private CheckBox checkBox;
    EditText lahtoaikaSanallinen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ride_details);

        checkBox = (CheckBox) findViewById(R.id.setRideDetails_checkBox_aika);
        checkBox.setOnClickListener(this);
        lahtoaikaSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenAika);
    }

    @Override
    public void onClick(View v) {
        if(checkBox.isChecked())
        {
            lahtoaikaSanallinen.setVisibility(View.VISIBLE);
        }
        else
        {
            lahtoaikaSanallinen.setVisibility(View.GONE);
        }
    }
}
