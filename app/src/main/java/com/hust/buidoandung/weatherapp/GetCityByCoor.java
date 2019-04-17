package com.hust.buidoandung.weatherapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
public class GetCityByCoor extends AsyncTask<String,String,String> {
//    weather
    ProgressDialog progressDialog;
    Context context;
    MainActivity mainActivity;
    public GetCityByCoor(ProgressDialog progressDialog, Context context, MainActivity activity) {
       this.progressDialog=progressDialog;
       this.context=context;
       this.mainActivity=activity;
    }

    @Override
    protected String doInBackground(String... strings) {
        String weather ="";
        try {
                URL url=createURL(strings[0],strings[1]);
                String response="";
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            int responseCode=connection.getResponseCode();
            if (responseCode == 200) {
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line=null;
                while ((line=bufferedReader.readLine())!=null){
                    response+=line;
                }
                bufferedReader.close();
                connection.disconnect();
                weather=parseTodayJson(response);
            }
        }catch (Exception e){
            e.printStackTrace();

        }
        return weather;
    }

    @Override
    protected void onPreExecute(){
        if(!progressDialog.isShowing()){
            progressDialog.setMessage("Waiting...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(final String s) {
        progressDialog.dismiss();
        if(s==null){
            Snackbar.make(mainActivity.findViewById(android.R.id.content),"Has error when collect data by using GPS",Snackbar.LENGTH_LONG).show();
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.saveLocation(s);
            }
        });
    }

    private String parseTodayJson(String response) {

        try {
            JSONObject reader = new JSONObject(response);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                Log.e("Geolocation", "No city found");
                return null;
            }
            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = ", " + countryObj.getString("country");
            }

        return city + country;


        }catch (Exception e){
            Log.d("Exception",e.getMessage());

        }
        return null;
    }
    private URL createURL(String lat,String lon) throws Exception{
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(context);
        String apiKey=sp.getString("apiKey","fce95bdbd820ccf29a68b9574b50fe50");
        StringBuilder stringBuilder=new StringBuilder("http://api.openweathermap.org/data/2.5/");
        stringBuilder.append("weather").append("?");
        stringBuilder.append("lat=").append(lat).append("&lon=").append(lon);
        stringBuilder.append("&lang=").append(Locale.getDefault().getLanguage());
        stringBuilder.append("&mode=json");
        stringBuilder.append("&appid=").append(apiKey);
        return new URL(stringBuilder.toString());
    }
}
