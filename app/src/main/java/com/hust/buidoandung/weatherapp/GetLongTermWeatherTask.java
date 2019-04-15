package com.hust.buidoandung.weatherapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class GetLongTermWeatherTask extends RequestToServer {
    public GetLongTermWeatherTask(ProgressDialog progressDialog, MainActivity context, MainActivity activity) {
        super(progressDialog, context, activity);
    }

    @Override
    protected ApiResult.ServerResult parseResponse(String response) throws Exception {
        return parseLongTermJson(response);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String getAPIName() {
        return "forecast";
    }

    @Override
    protected void updateMainUI() {
        ((MainActivity) context).updateLongTermWeatherUI();
    }

    private ApiResult.ServerResult parseLongTermJson(String response) throws Exception {
        MainActivity mainActivity=(MainActivity)this.context;
        int i;
        try {
            JSONObject reader = new JSONObject(response);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                if (mainActivity.longTermWeather == null) {
                    mainActivity.longTermWeather = new ArrayList<>();
                    mainActivity.longTermTodayWeather = new ArrayList<>();
                    mainActivity.longTermTomorrowWeather = new ArrayList<>();
                }
                return ApiResult.ServerResult.CITY_NOT_FOUND;
            }

            mainActivity.longTermWeather = new ArrayList<>();
            mainActivity.longTermTodayWeather = new ArrayList<>();
            mainActivity.longTermTomorrowWeather = new ArrayList<>();

            JSONArray list = reader.getJSONArray("list");
            Calendar today = Calendar.getInstance();
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
                weather.setIcon(listItem.optJSONArray("weather").getJSONObject(0).getString("icon"));

                weather.setId(idString);
                Calendar cal=Calendar.getInstance();
                cal.setTime(weather.getDate());
                if (cal.get(Calendar.DAY_OF_YEAR) == thisToday) {
                    mainActivity.longTermTodayWeather.add(weather);
                } else if (cal.get(Calendar.DAY_OF_YEAR) == thisToday + 1) {
                    mainActivity.longTermTomorrowWeather.add(weather);
                } else {
                    mainActivity.longTermWeather.add(weather);
                }
            }


        } catch (JSONException e) {
            Log.e("JSONException Data", response);
            e.printStackTrace();
            return ApiResult.ServerResult.JSON_EXCEPTION;
        }

        return ApiResult.ServerResult.OK;
    }
}
