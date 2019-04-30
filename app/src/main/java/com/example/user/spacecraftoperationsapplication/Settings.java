package com.example.user.spacecraftoperationsapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    private void initBottomNav()
    {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_settings);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        nightModeActivate();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initBottomNav();

        //Get the switches.
        Switch nightmode = findViewById(R.id.nightmode_sw);
        Switch warning = findViewById(R.id.warning_sw);

        //Stores user's preferences such as warning and nightmode.
        SharedPreferences settingsPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = settingsPref.edit();

        //If the user already enabled warnings, then the switch will be on. Otherwise, off.
        if (settingsPref.getBoolean("warning", true))
        {
            warning.setChecked(true);
        }
        else
        {
            warning.setChecked(false);
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {

            nightmode.setChecked(true);
        }

        //Event listener for nightmode switch.
        nightmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    restartApp();
                }
                else
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    restartApp();
                }
            }
        });

        //Event listener for warning switch.
        //If it is on, then warnings will be displayed to the user.
        warning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    editor.putBoolean("warning", true);
                }
                else
                {
                    editor.remove("warning");
                    editor.putBoolean("warning", false);
                }
                editor.apply();
            }
        });
    }

    public void nightModeActivate()
    {
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES)
        {
            setTheme(R.style.darkTheme);
        }
        else setTheme(R.style.AppTheme);
    }

    public void restartApp()
    {
        Intent i = new Intent (getApplicationContext(),Settings.class);
        startActivity(i);
        finish();
    }
}
