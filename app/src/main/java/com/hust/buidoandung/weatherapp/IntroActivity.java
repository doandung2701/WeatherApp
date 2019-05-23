package com.hust.buidoandung.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
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
        //De cho khong hien thi title cua ung dung
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);
        setStatusBarTrans(true);
        //tao ra Rotate Animation . xoay to 0 toi 360. Toa do diem quay la 50% theo truc X va Y
        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        //thiet lap thoi gian quay
        rotate.setDuration(800);
        //thiet lap so lan lap quay
        rotate.setRepeatCount(Animation.INFINITE);
        ImageView view = findViewById(R.id.icon_intro);
        view.startAnimation(rotate);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
//kiem tra xem day la lan dau vao ung dung hay gi. Neu la lan dau. thi se dieu huong toi man hinh Welcome.
        //Khong thi dieu huong toi man hinh main
        if (preferences.getBoolean("INTRO",true)){
            intent = new Intent(IntroActivity.this, WelcomeActivity.class);
            editor.putBoolean("INTRO",false);
            editor.commit();
        }else{
            intent = new Intent(IntroActivity.this, MainActivity.class);
        }
        //Muc dich de chay 1 luong delay 4 s. sau 4s man hinh se tu dong chuyen sang activity khca
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
