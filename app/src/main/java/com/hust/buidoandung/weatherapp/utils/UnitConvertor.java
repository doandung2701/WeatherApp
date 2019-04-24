package com.hust.buidoandung.weatherapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.hust.buidoandung.weatherapp.MyApplication;
import com.hust.buidoandung.weatherapp.R;

import java.text.DecimalFormat;
import java.util.Locale;

public class UnitConvertor {
    public static float convertTemperature(float temperature, SharedPreferences sp) {
        if (sp.getString("unit", "°C").equals("°C")) {
            return UnitConvertor.kelvinToCelsius(temperature);
        } else if (sp.getString("unit", "°C").equals("°F")) {
            return UnitConvertor.kelvinToFahrenheit(temperature);
        } else {
            return temperature;
        }
    }

    public static float kelvinToCelsius(float kelvinTemp) {
        return kelvinTemp - 273.15f;
    }

    public static float kelvinToFahrenheit(float kelvinTemp) {
        return ((9*kelvinTemp) / 5) -459.67f;
    }

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
    public static String format(float data,DecimalFormat format){
        return format.format(data);
    }

    public static  String setWeatherIcon(int actualId, int hourOfDay) {
        int id = actualId / 100;
        String icon = "";
        Context context= MyApplication.getInstance();
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
                icon = context.getString(R.string.weather_sunny);
            } else {
                icon = context.getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = context.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = context.getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = context.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = context.getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = context.getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = context.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

}
