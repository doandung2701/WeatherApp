package com.hust.buidoandung.weatherapp;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static com.hust.buidoandung.weatherapp.ApiResult.ServerResult.JSON_EXCEPTION;
import static com.hust.buidoandung.weatherapp.ApiResult.ServerResult.OK;

public class GetWeatherByCoor extends RequestToServer{
    public GetWeatherByCoor(ProgressDialog progressDialog, MainActivity context, MainActivity activity) {
        super(progressDialog, context, activity);
    }

    @Override
    protected void onPreExecute(){
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

    private ApiResult.ServerResult parseTodayJson(String response) {

        try {
            JSONObject reader = new JSONObject(response);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                Log.e("Geolocation", "No city found");
                return ApiResult.ServerResult.CITY_NOT_FOUND;
            }

            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = ", " + countryObj.getString("country");
            }

            ((MainActivity)context).saveLocation(city + country);


        }catch (Exception e){
            return JSON_EXCEPTION;

        }
        return OK;
    }
}
