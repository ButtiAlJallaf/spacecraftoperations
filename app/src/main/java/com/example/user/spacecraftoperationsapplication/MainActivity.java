package com.example.user.spacecraftoperationsapplication;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES)
        {
            setTheme(R.style.darkTheme);
        }
        else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logo = findViewById(R.id.logoSplash);

        //Animation code.
        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.mytransition);
        logo.startAnimation(myanim);

        //splash screen
        new CountDownTimer(5000,2000)
        {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish()
            {
                //after a couple of seconds go to next activity using intent
                Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(i);
            }
        }.start();
    }
}
