package com.hust.buidoandung.weatherapp.utils;

import android.content.SharedPreferences;

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



}
