package com.example.user.spacecraftoperationsapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class HomeActivity extends AppCompatActivity {

    public ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        nightModeActivate();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        changeLogo();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
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

    public void openAboutUs(View view)
    {
        Intent aboutus = new Intent(getApplicationContext(),AboutUs.class);

        startActivity(aboutus);
    }

    public void openSubscribe(View view)
    {
        Intent subscribe = new Intent(getApplicationContext(),RoverTelemetry.class);

        startActivity(subscribe);
    }

    public void openCredits(View view)
    {
        Intent credits = new Intent(getApplicationContext(),Credits.class);

        startActivity(credits);
    }

    public void nightModeActivate()
    {
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES)
        {
            setTheme(R.style.darkTheme);
        }
        else setTheme(R.style.AppTheme);
    }

    public void changeLogo()
    {
        logo = (ImageView) findViewById(R.id.logo);
        logo.setTag(1);

        if(AppCompatDelegate.getDefaultNightMode()== AppCompatDelegate.MODE_NIGHT_YES)
        {
            logo.setImageResource(R.drawable.logowhite);
            logo.setTag(2);
        }
        else
        {
            logo.setImageResource(R.drawable.logo);
            logo.setTag(1);
        }
    }
}
