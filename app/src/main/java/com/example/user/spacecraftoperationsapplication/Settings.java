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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Settings extends AppCompatActivity {
    //Stores user's preferences such as warning and nightmode.
    SharedPreferences settingsPref;
    SharedPreferences.Editor editor;

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

        //Get the edittext.
        EditText etIP = findViewById(R.id.etIP);
        EditText etPort = findViewById(R.id.etPort);

        //Initliaze sharedprefences and editor for reading and storing user preferences.
        settingsPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = settingsPref.edit();

        //Inserts the saved IP and port to the edit texts.
        etIP.setText(settingsPref.getString("ip", "192.168.0.2"));
        etPort.setText(settingsPref.getString("port", "3739"));

        //If the user already enabled warnings, then the switch will be on. Otherwise, off.
        if (settingsPref.getBoolean("warning", true))
        {
            warning.setChecked(true);
        }
        else
        {
            warning.setChecked(false);
        }

        //If the user already enabled night mode, then the switch will be on. Otherwise, off.
        if (settingsPref.getBoolean("nightmode", false)) {

            nightmode.setChecked(true);
        }
        else
        {
            nightmode.setChecked(false);
        }

        //Event listener for nightmode switch.
        nightmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean("nightmode", true);
                }
                else
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.remove("nightmode");
                    editor.putBoolean("nightmode", false);
                }
                editor.commit();
                restartApp();
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
                editor.commit();
            }
        });
    }

    private void nightModeActivate()
    {
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES)
        {
            setTheme(R.style.darkTheme);
        }
        else setTheme(R.style.AppTheme);
    }

    private void restartApp()
    {
        Intent i = new Intent (getApplicationContext(),Settings.class);
        startActivity(i);
        finish();
    }

    private void displayToast(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    //Method for the save network config button.
    public void saveNetworkConfig(View v)
    {
        //Boolean used for validation. If it is true, then user has entered correct data. Else is correct.
        boolean valid = true;

        //Read the IP.
        EditText etIP = findViewById(R.id.etIP);
        String ip = etIP.getText().toString();

        //Read the port.
        EditText etPort = findViewById(R.id.etPort);
        String port = etPort.getText().toString();

        //If port is empty, display error.
        if (port.matches(""))
        {
            etPort.setError("Please enter a valid port.");
            valid = false;
        }

        //Convert port string to port number if the user has entered a number..
        if (valid) {
            int portNum = Integer.parseInt(port);
            if (portNum < 0 || portNum > 65535) {
                etPort.setError("Port must be between 0 and 65535.");
                valid = false;
            }
        }

        //If it is not matching the IP pattern, then display error.
        if (!ip.matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b"))
        {
            etIP.setError("Please enter a valid IP.");
            valid = false;
        }

        //If data is not valid, then it will not be saved.
        if (!valid)
        {
            return;
        }
        else
        {
            //Adds the IP and port to shared preferences.
            editor.putString("ip", ip);
            editor.putString("port", port); //Should be string.
            editor.commit();
            displayToast("Network config saved!");
        }
    }
}
