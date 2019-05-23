package com.hust.buidoandung.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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
    Thread  thread1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // man hinh k co ten ung dung
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //full man hinh
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        setStatusBarTrans(true);
        //Lay doi duong FragmentManager de co the add fragment vao view
        FragmentManager fm = getSupportFragmentManager();
        //tao ra 5 fragment de hien thi thong tin gioi thieu ve ung dung
        ArrayList<WelcomeFragment> fragments = new ArrayList<>();
        for (int i=0;i<5;i++){
            fragments.add(WelcomeFragment.newInstance(i+1));
        }
        //tao ra pager Adapter de thiet lap cho welcome pager
        welcomePager = findViewById(R.id.welcomePager);
        pagerAdapter = new WelcomePagerAdapter(fm,fragments);
        welcomePager.setAdapter(pagerAdapter);
        final TextView welcomeStep = findViewById(R.id.txtWelcomeStep);
        final Button buttonNext = findViewById(R.id.btn_next);
        //xu ly cac hanh dong bam nut
        buttonNext.setOnClickListener(btnNextClicked);
        Button buttonSkip = findViewById(R.id.btn_skip);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        //khi bam vao skip. Luu trang thai la false va dieu huong toi MainActivity
        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                editor.putBoolean("INTRO",false);
                editor.commit();
                startActivity(intent);
                finish();
            }
        });
        //xu ly khi thay doi page.
        welcomePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                currentStep = i+1;
                welcomeStep.setText(Integer.toString(i+1)+"/5");
                //truong hop toi trang cuoi. Thi button next doi thanh Let start. sau do bam vao buttonNext se dieu huong toi MainActivity
                if (i==4){
                    buttonNext.setText("Let's start");
                    buttonNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                            editor.putBoolean("INTRO",false);
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
        //thread nay de tu duong chuyen page trong pager/
        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (currentStep!=5){
                    try{
                        Thread.sleep(2000);
                        mHandlerThread.sendEmptyMessage(0);
                    }
                    catch (InterruptedException ex){
                        ex.printStackTrace();
                    }

                }
            }
        });
        thread1.start();
    }
    //day la Hanlder de xu ly khi chuyen trang boi thread
    Handler mHandlerThread = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            welcomePager.setCurrentItem(currentStep++);
            //neu la trang thu 5. ngat luong. De nguoi dung su dung nut
            if(currentStep==5){
                thread1.interrupt();
            }

        }
    };
    @Override
    protected void onPause() {
        super.onPause();
        //neu step chua la 5.. cap nhat Pre
        if (currentStep!=5){
            editor.putBoolean("INTRO",true);
            editor.commit();
        }
    }

        //hanh dong next trang khi bam nut
    public View.OnClickListener btnNextClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentStep==5){
                currentStep = 0;
            }
            welcomePager.setCurrentItem(currentStep++);
        }
    };
    //lam trong suot thanh statusbar
    protected void setStatusBarTrans(boolean makeTrans) {
        if (makeTrans) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

}
