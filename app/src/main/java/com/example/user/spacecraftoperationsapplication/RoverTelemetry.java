package com.example.user.spacecraftoperationsapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class RoverTelemetry extends AppCompatActivity {
    //Global variables
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rover_telemetry);

        initBottomNav(); //Code by Butti to initialize the bottom navigation.
        sharedPref = getSharedPreferences("subscriptions", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        //Initliazes the toggle buttons by adding a listener, and setting corresponding key.
        initToggleButtons(R.id.ack_msg);
        initToggleButtons(R.id.power_telemetry_battery_I);
        initToggleButtons(R.id.power_telemetry_battery_V);
        initToggleButtons(R.id.power_telemetry_motor_lf_I);
        initToggleButtons(R.id.power_telemetry_motor_lr_I);
        initToggleButtons(R.id.power_telemetry_motor_rf_I);
        initToggleButtons(R.id.power_telemetry_motor_rr_I);

        checkSubscriptions();
    }

    private void checkSubscriptions()
    {
        SharedPreferences sharedPref = getSharedPreferences("subscriptions", Context.MODE_PRIVATE);
        if (sharedPref.getBoolean("ack.msg", false))
        {
            ToggleButton tb = findViewById(R.id.ack_msg);
            tb.setChecked(true);
        }
        if (sharedPref.getBoolean("power_telemetry.battery_I", false))
        {
            ToggleButton tb = findViewById(R.id.power_telemetry_battery_I);
            tb.setChecked(true);
        }
        if (sharedPref.getBoolean("power_telemetry.battery_V", false))
        {
            ToggleButton tb = findViewById(R.id.power_telemetry_battery_V);
            tb.setChecked(true);
        }
    }

    //This function takes the resource id and subscription key as parameters. It adds a listener to the toggle buttons.
    private void initToggleButtons(final int myToggleButton)
    {
        ToggleButton toggle = findViewById(myToggleButton);
        final String buttonName = getResources().getResourceEntryName(myToggleButton);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean(buttonName, true);
                } else {
                    editor.remove(buttonName);
                    editor.putBoolean(buttonName, false);
                }
                editor.commit();
            }
        });
    }

    private void initBottomNav()
    {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_rover);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.action_home:
                        Intent home = new Intent(getApplicationContext(),HomeActivity.class);

                        startActivity(home);
                        break;
                    case R.id.action_rover:
                        Intent rover = new Intent(getApplicationContext(),Rover.class);

                        startActivity(rover);
                        break;
                    case R.id.action_settings:
                        Intent settings = new Intent(getApplicationContext(),Settings.class);

                        startActivity(settings);
                        break;
                }
                return true;
            }
        });
    }
}
