package com.hust.buidoandung.weatherapp.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.hust.buidoandung.weatherapp.utils.DefaultValue;
import com.hust.buidoandung.weatherapp.MainActivity;
import com.hust.buidoandung.weatherapp.model.Weather;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;
public class GetTodayWeatherTask extends AsyncTask<String,String, Weather> {
    ProgressDialog progressDialog;
    Context context;
    MainActivity mainActivity;
    public GetTodayWeatherTask(ProgressDialog progressDialog, Context context, MainActivity activity) {
       this.progressDialog=progressDialog;
       this.context=context;
       this.mainActivity=activity;
    }

    @Override
    protected Weather doInBackground(String... strings) {
        try {
            Weather weather;
            URL url=createURL();
            String response="";
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            int responseCode=connection.getResponseCode();
            if(responseCode==200){
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader r = new BufferedReader(inputStreamReader);
                String line=null;
                while ((line=r.readLine())!=null){
                    response+=line;
                }
                r.close();
                connection.disconnect();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                Calendar now = Calendar.getInstance();
                editor.putLong("lastUpdate", now.getTimeInMillis()).apply();
                weather=parseTodayJson(response);
                return weather;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
        return null;
    }

    @Override
    protected void onPostExecute(final Weather weather) {
        progressDialog.dismiss();
        if(weather==null){
            Snackbar.make(mainActivity.findViewById(android.R.id.content), "Specified city is not found.", Snackbar.LENGTH_LONG).show();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("city", DefaultValue.DEFAULT_CITY);
            editor.commit();
            return ;
        }else{
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.saveLocation(weather.getCity()+", "+weather.getCountry());
                    mainActivity.updateTodayWeatherUI(weather);
                }
            });
        }
    }

    @Override
    protected void onPreExecute() {
        if(!progressDialog.isShowing()){
            progressDialog.setMessage("Waiting...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }




    private Weather parseTodayJson(String response) {
        try {
            JSONObject reader = new JSONObject(response);
            final String code = reader.optString("cod");
            //khong tim thay thanh pho nay
            if(code.equals("404")){
                return null;
            }
            Weather weather=new Weather();
            //prase thong tin ve thanh pho
            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
                weather.setSunrise(countryObj.getString("sunrise"));
                weather.setSunset(countryObj.getString("sunset"));
            }
            weather.setCity(city);
            weather.setCountry(country);
            //lay thong tin ve nhiet do,do am...
            JSONObject main = reader.getJSONObject("main");

            weather.setTemperature(main.getString("temp"));
            weather.setPressure(main.getString("pressure"));
            weather.setHumidity(main.getString("humidity"));
            //mieu ta ve tinh trang thoi tiet
            weather.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            weather.setIcon(reader.getJSONArray("weather").getJSONObject(0).getString("icon"));

            String idString = reader.getJSONArray("weather").getJSONObject(0).getString("id");
            weather.setId(idString);
            //gio va huong cua gio
            JSONObject windObj = reader.getJSONObject("wind");
            weather.setWind(windObj.getString("speed"));

            //xu ly ve gia tri mua
            JSONObject rainObj = reader.optJSONObject("rain");
            String rain;
            if (rainObj != null) {
                rain = MainActivity.getRainString(rainObj);
            } else {
                JSONObject snowObj = reader.optJSONObject("snow");
                if (snowObj != null) {
                    rain = MainActivity.getRainString(snowObj);
                } else {
                    rain = "0";
                }
            }
            weather.setRain(rain);
            return weather;
        }catch (Exception e){
            Log.d("Exception",e.getMessage());
            return null;
        }
    }
    private URL createURL() throws Exception{
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(context);
        String apiKey=sp.getString("apiKey","fce95bdbd820ccf29a68b9574b50fe50");
        StringBuilder stringBuilder=new StringBuilder("http://api.openweathermap.org/data/2.5/");
        stringBuilder.append("weather").append("?");
        final String city = sp.getString("city", DefaultValue.DEFAULT_CITY);
        stringBuilder.append("q=").append(URLEncoder.encode(city, "UTF-8"));
        stringBuilder.append("&lang=").append(Locale.getDefault().getLanguage());
        stringBuilder.append("&mode=json");
        stringBuilder.append("&appid=").append(apiKey);
        return new URL(stringBuilder.toString());
    }

}