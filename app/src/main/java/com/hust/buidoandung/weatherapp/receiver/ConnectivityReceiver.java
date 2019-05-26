package com.hust.buidoandung.weatherapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.hust.buidoandung.weatherapp.MainActivity;
import com.hust.buidoandung.weatherapp.MyApplication;
//muc dich de kiem tra trang thai cua internet. Nham ban ra su kien cho cac Activity xu ly
public class ConnectivityReceiver extends BroadcastReceiver {

    //mỗi khi nhận được thông báo về tình hình mạng. Hàm này sẽ được gọi
    @Override
    public void onReceive(Context context, Intent intent) {
        //ta lấy dịch vụ CONNECTIVITY_SERVICE để kiểm tra trạng thái internet
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork=cm.getActiveNetworkInfo();
        //lấy trạng thái internet hiện hành
        boolean isConnected=activeNetwork!=null&&activeNetwork.isConnectedOrConnecting();
       if(isConnected){
           //trường hợp có mạng, ta sẽ goi lại hàm load data
           if(context instanceof MainActivity){
               ((MainActivity)context).preloadWeather();
           }
       }
    }
    //function mục đích check có internet hay k bằng tay.
    public static boolean isConnected() {
        ConnectivityManager
                cm = (ConnectivityManager) MyApplication.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }
}
