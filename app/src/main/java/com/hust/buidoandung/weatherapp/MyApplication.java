package com.hust.buidoandung.weatherapp;

import android.app.Application;

import com.hust.buidoandung.weatherapp.receiver.ConnectivityReceiver;
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
