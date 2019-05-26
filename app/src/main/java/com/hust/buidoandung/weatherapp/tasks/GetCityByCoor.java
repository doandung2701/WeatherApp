package com.hust.buidoandung.weatherapp.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.hust.buidoandung.weatherapp.MainActivity;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
//Mục tiêu: lấy tên thành phố thông qua tọa độ
public class GetCityByCoor extends AsyncTask<String,String,String> {
//    weather
    ProgressDialog progressDialog;
    //activity
    MainActivity mainActivity;
    public GetCityByCoor(ProgressDialog progressDialog, MainActivity activity) {
       this.progressDialog=progressDialog;
       this.mainActivity=activity;
    }

    @Override
    protected String doInBackground(String... strings) {
        String weather ="";
        try {
            //tạo 1 đối tượng URL kết nối tới server
                URL url=createURL(strings[0],strings[1]);
                String response="";
                //mở kết nối
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            int responseCode=connection.getResponseCode();
            //nếu lấy được dữ liệu thành công
            if (responseCode == 200) {
                //Lấy ra luồng đọc dữ liệu, sử dụng buffer để đọc
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line=null;
                //lấy hết dữ liệu đọc được gán vào responsê
                while ((line=bufferedReader.readLine())!=null){
                    response+=line;
                }
                //close các luồng
                bufferedReader.close();
                connection.disconnect();
                //convert về đối tượng weather
                weather=parseTodayJson(response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return weather;
    }

    @Override
    protected void onPreExecute(){
        //thiết lập progess để hiển thị progess. và cài đặt k cho bấm ra ngoài là tắt progess
        if(!progressDialog.isShowing()){
            progressDialog.setMessage("Waiting...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(final String s) {
        //tắt progress
        progressDialog.dismiss();
        //trường hợp call lỗi
        if(s==null){
            Snackbar.make(mainActivity.findViewById(android.R.id.content),"Cant file city",Snackbar.LENGTH_LONG).show();
        }else
        //lưu lại địa chỉ
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.saveLocation(s);
            }
        });
    }
    /*
    Mục tiêu: convert string về đối tượng weather
    đầu vào: response String
    đầu ra: object đọc được hoặc null
     */
    private String parseTodayJson(String response) {

        try {
            JSONObject reader = new JSONObject(response);
            //đọc trạng thái của ressponse
            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                //trường hợp lỗi
                Log.e("Geolocation", "No city found");
                return null;
            }
            //đọc tên thành phố
            String city = reader.getString("name");
            String country = "";
            //đọc ra tên quốc gia
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                //nối vào thành phố
                country = ", " + countryObj.getString("country");
            }
            //trả về string dạng : tên thành phố, tên quốc gia. Mục đích giúp ta tìm kiếm chính xác hơn
        return city + country;


        }catch (Exception e){
            Log.d("Exception",e.getMessage());

        }
        return null;
    }
    //Sỉnh ra chuỗi URL để call api
    private URL createURL(String lat,String lon) throws Exception{
        //lấy ra api lưu trong SharedPreferences
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String apiKey=sp.getString("apiKey","fce95bdbd820ccf29a68b9574b50fe50");
        //nối chuỗi tạo ra url api
        StringBuilder stringBuilder=new StringBuilder("http://api.openweathermap.org/data/2.5/");
        stringBuilder.append("weather").append("?");
        //lấy ra api lưu trong SharedPreferences
        stringBuilder.append("lat=").append(lat).append("&lon=").append(lon);
        //lấy ra api lưu trong SharedPreferences
        stringBuilder.append("&lang=").append(Locale.getDefault().getLanguage());
        stringBuilder.append("&mode=json");
        //lấy ra api lưu trong SharedPreferences
        stringBuilder.append("&appid=").append(apiKey);
        return new URL(stringBuilder.toString());
    }
}
