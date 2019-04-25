package com.example.user.spacecraftoperationsapplication;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
private ImageView logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logo = (ImageView) findViewById(R.id.logoSplash);
        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.mytransition);
        logo.startAnimation(myanim);

        //splash screen
        new CountDownTimer(5000,2000)
        {

            @Override
            public void onTick(long millisUntilFinished)
            {
            }

            @Override
            public void onFinish()
            {
                //after a couple of seconds go to next activity using intent
                Intent login = new Intent(getApplicationContext(), HomeActivity.class);

                startActivity(login);
            }
        }.start();

    }
}
