package com.hust.buidoandung.weatherapp;

import android.app.Application;

import com.hust.buidoandung.weatherapp.receiver.ConnectivityReceiver;
//mục đích nắm giữ 1 đối tượng duy nhất giúp việc lấy được string ở khắp nơi trong app mà k cần trong context
public class MyApplication extends Application {
    private static MyApplication mInstance;
    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

}
