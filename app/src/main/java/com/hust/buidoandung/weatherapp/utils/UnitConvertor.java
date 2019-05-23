package com.hust.buidoandung.weatherapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.hust.buidoandung.weatherapp.MyApplication;
import com.hust.buidoandung.weatherapp.R;

import java.text.DecimalFormat;
import java.util.Locale;
//su dung de chuyen doi don vi
public class UnitConvertor {
    //convert nhiet do
    public static float convertTemperature(float temperature, SharedPreferences sp) {
        if (sp.getString("unit", "°C").equals("°C")) {
            return UnitConvertor.kelvinToCelsius(temperature);
        } else if (sp.getString("unit", "°C").equals("°F")) {
            return UnitConvertor.kelvinToFahrenheit(temperature);
        } else {
            return temperature;
        }
    }
    //Cong thuc doi do K ra do C
    public static float kelvinToCelsius(float kelvinTemp) {
        return kelvinTemp - 273.15f;
    }
    ///Cong thuc doi do K ra do F
    public static float kelvinToFahrenheit(float kelvinTemp) {
        return ((9*kelvinTemp) / 5) -459.67f;
    }
    //xu ly luong mua. doi don vi
    public static String getRainString(double rain, SharedPreferences sp) {
        if (rain > 0) {
            if (!sp.getString("lengthUnit", "mm").equals("mm")) {
                rain = rain / 25.4;
            }
            return String.format(Locale.ENGLISH, " (%f %s)", rain, sp.getString("lengthUnit", "mm"));
        }
        return "";
    }

    //xu ly ap suat
    public static float convertPressure(float pressure, SharedPreferences sp) {
        String unit=sp.getString("pressureUnit","hPa");
        switch (unit){
            case "kPa":
                 return pressure/10;
            case "mm Hg":
                return (float) (pressure*0.75006157584566);
            case "in Hg":
                return (float)(pressure*0.029529983071445);
             default:
                 return pressure;
        }
    }
    ///xu ly toc do gio. doi don vi
    public static double convertWind(double wind, SharedPreferences sp) {
        String unit=sp.getString("speedUnit","m/s");
        switch (unit){
            case "kph":
                return wind * 3.6;
            case "mph":
             return wind *2.23694;
            default:
                return wind;
        }
    }
    //format so theo chuan
    public static String format(float data,DecimalFormat format){
        return format.format(data);
    }
        //Thiet lap icon cho thoi tiet tung thoi diem
    //moi Weather condition id  trong object weather cua du lieu tra ve bieu thi thoi tiet khu vuc do.
    public static  String setWeatherIcon(int actualId, int hourOfDay) {
        String icon = "";
        Context context= MyApplication.getInstance();
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
                icon = context.getString(R.string.weather_sunny);
            } else {
                icon = context.getString(R.string.weather_clear_night);
            }
        } else {
            switch (actualId/100) {
                case 2:
                    icon = context.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = context.getString(R.string.weather_drizzle);
                    break;
                case 5:
                    icon = context.getString(R.string.weather_rainy);
                    break;
                case 6:
                    icon = context.getString(R.string.weather_snowy);
                    break;
                case 7:
                    icon = context.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = context.getString(R.string.weather_cloudy);
                    break;

            }
        }
        return icon;
    }
    //convert deg to string
    public static String getWindDirectionString(Double deg){
        if(((0<=deg&&deg<=11.25)||(deg>348.75&&deg<=360))){
            return "North";
        }else if (deg>11.25&&deg<=33.75){
            return "N-N-E";
        }else if (deg>33.75&&deg<=56.25){
            return "North East";
        }else if (deg>56.25&&deg<=78.75){
            return "E-N-E";
        }else if (deg>78.75&&deg<=101.25){
            return "East";
        }else if (deg>101.25&&deg<=123.75){
            return "E-S-E";
        }else if (deg>123.75&&deg<=146.25){
            return "South East";
        }else if (deg>146.25&&deg<=168.75){
            return "S-S-E";
        }else if (deg>168.75&&deg<=191.25){
            return "South";
        }else if (deg>191.25&&deg<=213.75){
            return "S-S-W";
        }else if (deg>213.75&&deg<=236.25){
            return "South West";
        }else if (deg>236.25&&deg<=258.75){
            return "W-S-W";
        }else if (deg>258.75&&deg<=281.25){
            return "West";
        }else if (deg>281.25&&deg<=303.75){
            return "W-N-W";
        }else if (deg>303.75&&deg<=326.25){
            return "North West";
        }else {
            return "N-N-W";
        }
    }

}
