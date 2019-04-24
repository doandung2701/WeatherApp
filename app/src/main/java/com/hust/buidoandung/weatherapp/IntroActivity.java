package com.hust.buidoandung.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IntroActivity extends AppCompatActivity {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setStatusBarTrans(true);
        setContentView(R.layout.activity_intro);
        Animation animSunrise = AnimationUtils.loadAnimation(this,R.anim.slide_up);
        animSunrise.setDuration(2000);
        ImageView view = findViewById(R.id.icon_intro);
        view.startAnimation(animSunrise);
        preferences = getSharedPreferences("APP_PRE",MODE_PRIVATE);
        editor = preferences.edit();
//
        if (preferences.getBoolean("IS_FIRST_LAUNCH",true)){
            intent = new Intent(IntroActivity.this, WelcomeActivity.class);
            editor.putBoolean("IS_FIRST_LAUNCH",false);
            editor.commit();
        }else{
            intent = new Intent(IntroActivity.this, MainActivity.class);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        },4000);

    }
    protected void setStatusBarTrans(boolean makeTrans) {
        if (makeTrans) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
