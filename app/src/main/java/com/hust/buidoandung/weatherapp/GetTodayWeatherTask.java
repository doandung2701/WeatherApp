package com.hust.buidoandung.weatherapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;

import static com.hust.buidoandung.weatherapp.ApiResult.ServerResult.JSON_EXCEPTION;
import static com.hust.buidoandung.weatherapp.ApiResult.ServerResult.OK;

public class GetTodayWeatherTask extends RequestToServer{
    public GetTodayWeatherTask(ProgressDialog progressDialog, MainActivity context, MainActivity activity) {
        super(progressDialog, context, activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ApiResult apiResult) {
        super.onPostExecute(apiResult);
    }

    @Override
    protected ApiResult.ServerResult parseResponse(String response) {
        return parseTodayJson(response);
    }

    @Override
    protected String getAPIName() {
        return "weather";
    }

    @Override
    protected void updateMainUI() {
        ((MainActivity)context).updateTodayWeatherUI();
        ((MainActivity)context).updateLastUpdateTime();
    }
    private ApiResult.ServerResult parseTodayJson(String response) {
//        {
//            "coord": {
//            "lon": 105.85,
//                    "lat": 21.03
//        },
//            "weather": [
//            {
//                "id": 803,
//                    "main": "Clouds",
//                    "description": "broken clouds",
//                    "icon": "04d"
//            }
//],
//            "base": "stations",
//                "main": {
//            "temp": 297.15,
//                    "pressure": 1013,
//                    "humidity": 100,
//                    "temp_min": 297.15,
//                    "temp_max": 297.15
//        },
//            "visibility": 10000,
//                "wind": {
//            "speed": 4.1,
//                    "deg": 60
//        },
//            "clouds": {
//            "all": 75
//        },
//            "dt": 1555291800,
//                "sys": {
//            "type": 1,
//                    "id": 9308,
//                    "message": 0.0033,
//                    "country": "VN",
//                    "sunrise": 1555281486,
//                    "sunset": 1555326902
//        },
//            "id": 1581130,
//                "name": "Hanoi",
//                "cod": 200
//        }
        Log.d("MainActivity", "parseTodayJson: "+response);
        try {
            JSONObject reader = new JSONObject(response);
            final String code = reader.optString("cod");
            //khong tim thay thanh pho nay
            if(code.equals("404")){
                return ApiResult.ServerResult.CITY_NOT_FOUND;
            }
            //prase thong tin ve thanh pho
            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            MainActivity mainActivity=((MainActivity)context);
            if (countryObj != null) {
                country = countryObj.getString("country");
                mainActivity.todayWeather.setSunrise(countryObj.getString("sunrise"));
                ((MainActivity)context).todayWeather.setSunset(countryObj.getString("sunset"));
            }
            mainActivity.todayWeather.setCity(city);
            mainActivity.todayWeather.setCountry(country);
            JSONObject coordinates = reader.getJSONObject("coord");
            if(coordinates!=null){
                double lat=coordinates.getDouble("lat");
                double lon=coordinates.getDouble("lon");
                SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(mainActivity);
                sp.edit().putFloat("latitude",(float)lat).commit();
                sp.edit().putFloat("longitude",(float)lon).commit();

            }
            //lay thong tin ve nhiet do,do am...
            JSONObject main = reader.getJSONObject("main");

            mainActivity.todayWeather.setTemperature(main.getString("temp"));
            mainActivity.todayWeather.setPressure(main.getString("pressure"));
            mainActivity.todayWeather.setHumidity(main.getString("humidity"));
            //mieu ta ve tinh trang thoi tiet
            mainActivity.todayWeather.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            mainActivity.todayWeather.setIcon(reader.getJSONArray("weather").getJSONObject(0).getString("icon"));

            String idString = reader.getJSONArray("weather").getJSONObject(0).getString("id");
            mainActivity.todayWeather.setId(idString);
            //gio va huong cua gio
            JSONObject windObj = reader.getJSONObject("wind");
            mainActivity.todayWeather.setWind(windObj.getString("speed"));
            if (windObj.has("deg")) {
                mainActivity.todayWeather.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                Log.e("parseTodayJson", "No wind direction available");
                mainActivity.todayWeather.setWindDirectionDegree(null);
            }

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
            mainActivity.todayWeather.setRain(rain);


        }catch (Exception e){
            return JSON_EXCEPTION;

        }
        return OK;
    }
}