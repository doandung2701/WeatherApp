package com.hust.buidoandung.weatherapp.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.hust.buidoandung.weatherapp.utils.UnitConvertor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Weather implements Parcelable {
    private String city;
    //tên đất nc
    private String country;
    //thời điểm của thời tiết
    private Date date;
    //nhiệt độ
    private String temperature;
    //miêu tả thời tiết
    private String description;
    //gió
    private String wind;
    //áp suất
    private String pressure;
    //độ ẩm
    private String humidity;
    //lượng mưa
    private String rain;
    //id
    private String id;
    //icon
    private String icon;
    //t.g mặt trời mọc
    private Date sunrise;
    //t.g mặt trời lặn
    private Date sunset;
    //tọa độ
    private float lat;
    private float log;
    //hướng gió
    private Double windDirectionDegree;

    public Weather() {
    }

    protected Weather(Parcel in) {
        city = in.readString();
        country = in.readString();
        temperature = in.readString();
        description = in.readString();
        wind = in.readString();
        pressure = in.readString();
        humidity = in.readString();
        rain = in.readString();
        id = in.readString();
        icon = in.readString();
        lat = in.readFloat();
        log = in.readFloat();
        if (in.readByte() == 0) {
            windDirectionDegree = null;
        } else {
            windDirectionDegree = in.readDouble();
        }
    }

    public static final Creator<Weather> CREATOR = new Creator<Weather>() {
        @Override
        public Weather createFromParcel(Parcel in) {
            return new Weather(in);
        }

        @Override
        public Weather[] newArray(int size) {
            return new Weather[size];
        }
    };

    public Double getWindDirectionDegree() {
        return windDirectionDegree;
    }

    public void setWindDirectionDegree(Double windDirectionDegree) {
        this.windDirectionDegree = windDirectionDegree;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLog() {
        return log;
    }

    public void setLog(float log) {
        this.log = log;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }




    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public Date getSunrise(){
        return this.sunrise;
    }
    //convert thời gian được trả về thành kiểu date và thiết lập cho sunrise
    public void setSunrise(String dateString) {
        try {
            //nếu kiiểu trả về là long
            setSunrise(new Date(Long.parseLong(dateString) * 1000));
        }
        catch (Exception e) {
            //còn lại format theo đúng định dạng trả veè
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                setSunrise(inputFormat.parse(dateString));
            }
            catch (ParseException e2) {
                setSunrise(new Date());
                e2.printStackTrace();
            }
        }
    }

    public void setSunrise(Date date) {
        this.sunrise = date;
    }

    public Date getSunset(){
        return this.sunset;
    }
    //tương tự như sunrise
    public void setSunset(String dateString) {
        try {
            setSunset(new Date(Long.parseLong(dateString) * 1000));
        }
        catch (Exception e) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                setSunrise(inputFormat.parse(dateString));
            }
            catch (ParseException e2) {
                setSunset(new Date());
                e2.printStackTrace();
            }
        }
    }

    public void setSunset(Date date) {
        this.sunset = date;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Date getDate(){
        return this.date;
    }
    //thiết lập thời gian. tương tự sunrise,sunset
    public void setDate(String dateString) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                setDate(inputFormat.parse(dateString));
            }
            catch (ParseException e2) {
                setDate(new Date());
                e2.printStackTrace();
            }
        }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRain() {
        return rain;
    }

    public void setRain(String rain) {
        this.rain = rain;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(city);
        dest.writeString(country);
        dest.writeString(temperature);
        dest.writeString(description);
        dest.writeString(wind);
        dest.writeString(pressure);
        dest.writeString(humidity);
        dest.writeString(rain);
        dest.writeString(id);
        dest.writeString(icon);
        dest.writeFloat(lat);
        dest.writeFloat(log);
        if (windDirectionDegree == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(windDirectionDegree);
        }
    }
}
