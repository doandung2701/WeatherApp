package com.hust.buidoandung.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hust.buidoandung.weatherapp.adapter.WelcomePagerAdapter;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {

    WelcomePagerAdapter pagerAdapter;
    SharedPreferences preferences;
    ViewPager welcomePager;
    SharedPreferences.Editor editor;
    int currentStep = 1;
    protected void setStatusBarTrans(boolean makeTrans) {
        if (makeTrans) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setStatusBarTrans(true);
        setContentView(R.layout.activity_welcome);
        FragmentManager fm = getSupportFragmentManager();
        ArrayList<WelcomeFragment> fragments = new ArrayList<>();
        for (int i=0;i<5;i++){
            fragments.add(WelcomeFragment.newInstance(i+1));
        }
        welcomePager = findViewById(R.id.welcomePager);
        pagerAdapter = new WelcomePagerAdapter(fm,fragments);
        welcomePager.setAdapter(pagerAdapter);
        final TextView welcomeStep = findViewById(R.id.txtWelcomeStep);
        final Button buttonNext = findViewById(R.id.btn_next);
        buttonNext.setOnClickListener(btnNextClicked);
        Button buttonSkip = findViewById(R.id.btn_skip);
        preferences = getSharedPreferences("APP_PRE",MODE_PRIVATE);
        editor = preferences.edit();
        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                editor.putBoolean("IS_FIRST_LAUNCH",false);
                editor.commit();
                startActivity(intent);
                finish();
            }
        });
        welcomePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                currentStep = i+1;
                welcomeStep.setText(Integer.toString(i+1)+"/5");
                if (i==4){
                    buttonNext.setText("Let's start");
                    buttonNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                            editor.putBoolean("IS_FIRST_LAUNCH",false);
                            editor.commit();
                            startActivity(intent);
                            finish();
                        }
                    });
                }else{
                    buttonNext.setText("Next");
                    buttonNext.setOnClickListener(btnNextClicked);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (currentStep!=5){
            editor.putBoolean("IS_FIRST_LAUNCH",true);
            editor.commit();
        }
    }

    public View.OnClickListener btnNextClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentStep==5){
                currentStep = 0;
            }
            welcomePager.setCurrentItem(currentStep++);
        }
    };
}
