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
import com.hust.buidoandung.weatherapp.utils.UnitConvertor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class GetLongTermWeatherTask extends AsyncTask<String,String, List<List<Weather>>> {
//    forecast
ProgressDialog progressDialog;
    MainActivity mainActivity;
    public GetLongTermWeatherTask(ProgressDialog progressDialog, MainActivity activity) {
        this.progressDialog=progressDialog;
        this.mainActivity=activity;
    }


    private List<List<Weather>> parseTodayJson(String response) throws Exception {
        int i;
        try {
            JSONObject reader = new JSONObject(response);
            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return null;
            }
            List<List<Weather>> data=new ArrayList<List<Weather>>();
            List<Weather> longTermWeather=new ArrayList<>();
            List<Weather> longTermTodayWeather=new ArrayList<>();
            List<Weather> longTermTomorrowWeather=new ArrayList<>();
            JSONArray list = reader.getJSONArray("list");
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            int thisToday=today.get(Calendar.DAY_OF_YEAR);
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");
                weather.setTemperature(main.getString("temp"));
                weather.setPressure(main.getString("pressure"));
                weather.setHumidity(main.getString("humidity"));
                //date
                weather.setDate(listItem.getString("dt_txt"));

                weather.setDescription(listItem.optJSONArray("weather").getJSONObject(0).getString("description"));
                JSONObject windObj = listItem.optJSONObject("wind");
                if (windObj != null) {
                    weather.setWind(windObj.getString("speed"));
                    weather.setWindDirectionDegree(windObj.getDouble("deg"));

                }

            //rain
                JSONObject rainObj = listItem.optJSONObject("rain");
                String rain = "";
                if (rainObj != null) {
                    rain = MainActivity.getRainString(rainObj);
                } else {
                    JSONObject snowObj = listItem.optJSONObject("snow");
                    if (snowObj != null) {
                        rain = MainActivity.getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                weather.setRain(rain);

                final String idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id");
                weather.setIcon(UnitConvertor.setWeatherIcon(Integer.parseInt(idString),today.get(Calendar.HOUR_OF_DAY)));
                weather.setId(idString);
                Calendar cal=Calendar.getInstance();
                cal.setTime(weather.getDate());
                if (cal.get(Calendar.DAY_OF_YEAR) == thisToday) {
                    longTermTodayWeather.add(weather);
                } else if (cal.get(Calendar.DAY_OF_YEAR) == thisToday + 1) {
                    longTermTomorrowWeather.add(weather);
                } else {
                   longTermWeather.add(weather);
                }
            }
            data.add(longTermTodayWeather);
            data.add(longTermTomorrowWeather);
            data.add(longTermWeather);
            return data;
        } catch (JSONException e) {
            Log.e("JSONException Data", response);
            e.printStackTrace();
            return null;
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

    @Override
    protected void onPostExecute(final List<List<Weather>> lists) {
        progressDialog.dismiss();
        if(lists==null){
            Snackbar.make(mainActivity.findViewById(android.R.id.content), "Specified city is not found.", Snackbar.LENGTH_LONG).show();
        }else{
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.updateLongTermWeatherUI(lists);

                }
            });
        }
    }

    @Override
    protected List<List<Weather>> doInBackground(String... strings) {
        try {
            List<List<Weather>> data;

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
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity).edit();
                Calendar now = Calendar.getInstance();
                editor.putLong("lastUpdate", now.getTimeInMillis()).apply();
                data=parseTodayJson(response);
                return data;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return  null;

        }
        return  null;
    }
    private URL createURL() throws Exception{
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String apiKey=sp.getString("apiKey","fce95bdbd820ccf29a68b9574b50fe50");
        StringBuilder stringBuilder=new StringBuilder("http://api.openweathermap.org/data/2.5/");
        stringBuilder.append("forecast").append("?");
        final String city = sp.getString("city", DefaultValue.DEFAULT_CITY);
        stringBuilder.append("q=").append(URLEncoder.encode(city, "UTF-8"));
        stringBuilder.append("&lang=").append(Locale.getDefault().getLanguage());
        stringBuilder.append("&mode=json");
        stringBuilder.append("&appid=").append(apiKey);
        return new URL(stringBuilder.toString());
    }
}
