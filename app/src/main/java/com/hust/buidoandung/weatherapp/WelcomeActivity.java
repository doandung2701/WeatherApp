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
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {

    WelcomePagerAdapter pagerAdapter;
    SharedPreferences preferences;
    ViewPager welcomePager;
    SharedPreferences.Editor editor;
    int currentStep = 1;
    Timer timer;

    public void pageSwitcher(int seconds) {
        timer = new Timer(); // Một luồng mới được sinh ra .
        //sau mỗi khoảng t.g* 1000, và delay 1s, RemindTask sẽ được thực hiện
        timer.scheduleAtFixedRate(new RemindTask(), 1000, seconds * 1000);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ẩn đi title của ứng dụng
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Thêm flag để hiên thị full màn hình
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        //thiết lập Status trong suốt
        setStatusBarTrans(true);
        //Để add fragment vào giao diện, ta cần lấy ra đối tượng FragmentManager
        FragmentManager fm = getSupportFragmentManager();
        //Tạo ra 5 Fragment giới thiệu ứng dụng.
        ArrayList<WelcomeFragment> fragments = new ArrayList<>();
        for (int i=0;i<5;i++){
            fragments.add(WelcomeFragment.newInstance(i+1));
        }
        //tạo ra pager Adapter cho welcome Pager
        welcomePager = findViewById(R.id.welcomePager);
        pagerAdapter = new WelcomePagerAdapter(fm,fragments);
        //thiết lập Adapter cho pager
        welcomePager.setAdapter(pagerAdapter);
        final TextView welcomeStep = findViewById(R.id.txtWelcomeStep);
        final Button buttonNext = findViewById(R.id.btn_next);
        //Thêm các action khi bấm vào nút
        buttonNext.setOnClickListener(btnNextClicked);
        //Nut Skip
        Button buttonSkip = findViewById(R.id.btn_skip);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        //Khi bấm nút Skip. Lưu INTRO bằng false và chuyển sang  MainActivity
        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                //cập nhật giá trị INTRO
                editor.putBoolean("INTRO",false);
                editor.commit();
                //chuyển sang MainAcitivity
                startActivity(intent);
                finish();
            }
        });
        //Thêm sự kiện lắng nghe khi pager adapter thay đổi
        welcomePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                //do page bắt đầu từ 0.
                currentStep = i+1;
                //cập nhật giá trị text hiển thị page hiện tại
                welcomeStep.setText(Integer.toString(i+1)+"/5");
                //Trường hợp là trang cuối. Đổi text trên buttonNext .Sửa đổi action khi bấm vào buttonNext chuyển sang màn hình của MainActivity
                if (i==4){
                    buttonNext.setText("Let's start");
                    //thay đổi hành động khi bấm nút
                    buttonNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //intent chuyển sang MainAcitivy
                            Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                           //update trạng thái
                            editor.putBoolean("INTRO",false);
                            editor.commit();
                            //chuyển Activity
                            startActivity(intent);
                            finish();
                        }
                    });
                }else{
                    //set về hành động Next nếu không phải là trang cuối
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
    protected void onResume() {
        super.onResume();
        pageSwitcher(2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //nếu chưa phải là trang cuối, cập nhật là true
        if (currentStep!=5){
            editor.putBoolean("INTRO",true);
            editor.commit();
        }
    }

        //khi bấm button Next, đổi trang
    public View.OnClickListener btnNextClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //chuyển về trang cần
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
    class RemindTask extends TimerTask{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //nếu là trang cuối
                    if(currentStep>4){
                        timer.cancel();
                    }else{
                        ///không thì sẽ next trang
                        welcomePager.setCurrentItem(currentStep++);
                    }
                }
            });
        }
    }
}

