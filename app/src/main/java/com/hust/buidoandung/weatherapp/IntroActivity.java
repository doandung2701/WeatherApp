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
        //Ẩn đi tile của ứng dụng
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //thiết lập giao diện full màn hình khi đã bỏ title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);
        setStatusBarTrans(true);
        //tạo ra Rotate Animation . xoay từ 0 tới 360. Tọa độ điểm quay 50% theo trục X và Y
        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        //thiết lập thời gian quay của 1 lần quay
        rotate.setDuration(800);
        //thiết lập số lần lặp quay
        rotate.setRepeatCount(Animation.INFINITE);
        ImageView view = findViewById(R.id.icon_intro);
        //chạy animation trên imageView chứa icon
        view.startAnimation(rotate);
        //lấy tham chiếu tới SharedPreference
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
       //Kiểm tra ứng dụng được truy cập lần đầu hay không.
        if (preferences.getBoolean("INTRO",true)){
            //Nếu là lần đầu hoặc là bỏ qua welcome. ta khởi chạy intent tới WelcomeActivity
            intent = new Intent(IntroActivity.this, WelcomeActivity.class);
            //cập nhật trạng thái biến INTRO trong SharedPreference
            editor.putBoolean("INTRO",false);
            editor.commit();
        }else{
            //cồn không ta điều hướng tới MainActivity
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
    //Dùng để làm trong suốt thanh statusbar
    protected void setStatusBarTrans(boolean makeTrans) {
        if (makeTrans) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
