package com.hust.buidoandung.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;

public class MainActivity extends AppCompatActivity implements LocationListener {
    public static final int REQUEST_LOCATION=1;
    public String recentCity = "";
    Weather todayWeather=new Weather();
    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    TextView lastUpdate;
    ImageView todayIcon;
    ViewPager viewPager;
    TabLayout tabLayout;
    LocationManager locationManager;
    ProgressDialog progressDialog;
    public List<Weather> longTermWeather = new ArrayList<>();
    public List<Weather> longTermTodayWeather = new ArrayList<>();
    public List<Weather> longTermTomorrowWeather = new ArrayList<>();
    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    public String downloadServerUri="http://openweathermap.org/img/w/%s.png";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        todayTemperature =  findViewById(R.id.todayTemp);
        todayDescription =  findViewById(R.id.todayDes);
        todayWind =  findViewById(R.id.todayWind);
        todayPressure =  findViewById(R.id.todayPress);
        todayHumidity =  findViewById(R.id.todayHumi);
        todaySunrise =  findViewById(R.id.todaySunr);
        todaySunset =  findViewById(R.id.todaySuns);
        lastUpdate =  findViewById(R.id.lastUpdate);
        todayIcon =  findViewById(R.id.todayIcon);
        viewPager=findViewById(R.id.viewPager);
        tabLayout=findViewById(R.id.tabs);
        progressDialog = new ProgressDialog(MainActivity.this);
        speedUnits.put("m/s", R.string.speed_unit_mps);
        speedUnits.put("kph", R.string.speed_unit_kph);
        speedUnits.put("mph", R.string.speed_unit_mph);
        speedUnits.put("kn", R.string.speed_unit_kn);

        pressUnits.put("hPa", R.string.pressure_unit_hpa);
        pressUnits.put("kPa", R.string.pressure_unit_kpa);
        pressUnits.put("mm Hg", R.string.pressure_unit_mmhg);
        preloadWeather();
        updateLastUpdateTime();

    }
    public void updateLongTermWeatherUI() {

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 0);
        WeatherFragment recyclerViewFragmentToday = new WeatherFragment();
        recyclerViewFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, "Today");

        Bundle bundleTomorrow = new Bundle();
        bundleTomorrow.putInt("day", 1);
        WeatherFragment recyclerViewFragmentTomorrow = new WeatherFragment();
        recyclerViewFragmentTomorrow.setArguments(bundleTomorrow);
        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, "Tomorrow");

        Bundle bundle = new Bundle();
        bundle.putInt("day", 2);
        WeatherFragment recyclerViewFragment = new WeatherFragment();
        recyclerViewFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(recyclerViewFragment,"Later");

        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (currentPage == 0 && longTermTodayWeather.isEmpty()) {
            currentPage = 1;
        }
        viewPager.setCurrentItem(currentPage, false);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                searchCity();
                return true;
            case R.id.action_settings:
                Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_location:
                return true;
            case R.id.action_refresh:
                getTodayWeather();
                getLongTermWeather();

            default:
                 return super.onOptionsItemSelected(item);
        }
    }


    private void getTodayWeather() {
        new GetTodayWeatherTask(progressDialog,this,this).execute();
    }

    @SuppressLint("RestrictedApi")
    private void searchCity() {
        AlertDialog.Builder alert=new AlertDialog.Builder(this)
                .setTitle("Search for city");
        final EditText searchText=new EditText(this);
        searchText.setInputType(InputType.TYPE_CLASS_TEXT);
        searchText.setMaxLines(1);
        searchText.setSingleLine(true);
        alert.setView(searchText,32,0,32,3);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String result=searchText.getText().toString();
                if(!result.isEmpty()){
                    saveLocation(result);
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.show();
    }

    private void saveLocation(String result) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        recentCity = preferences.getString("city", DefaultValue.DEFAULT_CITY);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", result);
        editor.putBoolean("cityChanged",true);
        editor.commit();

        if (!recentCity.equals(result)) {
            // New location, update weather
            getTodayWeather();
            getLongTermWeather();
        }
    }



    public WeatherAdapter getAdapter(int id) {
        WeatherAdapter weatherFragment;
        if (id == 0) {
            weatherFragment = new WeatherAdapter(this, longTermTodayWeather);
        } else if (id == 1) {
            weatherFragment = new WeatherAdapter(this, longTermTomorrowWeather);
        } else {
            weatherFragment = new WeatherAdapter(this, longTermWeather);
        }
        return weatherFragment;
    }



    public void updateLastUpdateTime() {
        updateLastUpdateTime(
                PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
        );
    }
    private void getLongTermWeather() {
        new GetLongTermWeatherTask(progressDialog,this, this).execute();
    }
    private void updateLastUpdateTime(long timeInMillis) {
        if (timeInMillis < 0) {
            lastUpdate.setText("");
        } else {
            Date lastCheckedDate = new Date(timeInMillis);
            String timeFormat = getTimeFormat(this).format(lastCheckedDate);
            String dateTime=getDateFormat(this).format(lastCheckedDate) + " " + timeFormat;
            lastUpdate.setText(getString(R.string.last_update,dateTime));
        }
    }

    public void updateTodayWeatherUI() {
        try {
            if (todayWeather.getCountry().isEmpty()) {
                preloadWeather();
                return;
            }
        } catch (Exception e) {
            preloadWeather();
            return;
        }
        DateFormat timeFormat =getTimeFormat(getApplicationContext());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String city = todayWeather.getCity();
        String country = todayWeather.getCountry();
        //cap nhat actionbar
        getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));

        // Temperature
        float temperature = UnitConvertor.convertTemperature(Float.parseFloat(todayWeather.getTemperature()), sp);
              temperature = Math.round(temperature * 10) / 10;

        // Rain
        double rain = Double.parseDouble(todayWeather.getRain());
        String rainString = UnitConvertor.getRainString(rain, sp);

        // Wind
        double wind;
        try {
            wind = Double.parseDouble(todayWeather.getWind());
        } catch (Exception e) {
            e.printStackTrace();
            wind = 0;
        }
        wind = UnitConvertor.convertWind(wind, sp);

        // Ap suat
        double pressure = UnitConvertor.convertPressure((float) Double.parseDouble(todayWeather.getPressure()), sp);
        DecimalFormat formatOne=new DecimalFormat("0.#");
        DecimalFormat windFormat=new DecimalFormat("#.0");
        todayTemperature.setText(String.format( UnitConvertor.format(temperature,formatOne)+ " " + sp.getString("unit", "°C")));
        todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() +
                todayWeather.getDescription().substring(1) + rainString);
        todayWind.setText(getString(R.string.wind) + ": " + UnitConvertor.format((float) wind,windFormat) + " " +
                    localize(sp, "speedUnit", "m/s"));
        todayPressure.setText(getString(R.string.pressure) + ": " +UnitConvertor.format((float)pressure,windFormat) + " " +
                localize(sp, "pressureUnit", "hPa"));
        todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
        todaySunrise.setText(getString(R.string.sunrise) + ": " + timeFormat.format(todayWeather.getSunrise()));
        todaySunset.setText(getString(R.string.sunset) + ": " + timeFormat.format(todayWeather.getSunset()));
        Glide
                .with(this)
                .load("http://openweathermap.org/img/w/"+todayWeather.getIcon()+".png")
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .onlyRetrieveFromCache(true)
                .centerCrop()
                .error(R.drawable.baseline_error_outline_24)
                .fallback(new ColorDrawable(Color.GRAY))
                .placeholder(R.drawable.progress_animation)
                .into(todayIcon);
    }

    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return localize(sp, this, preferenceKey, defaultValueKey);
    }
    public static String localize(SharedPreferences sp, Context context, String preferenceKey, String defaultValueKey) {
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
    }

    private void preloadWeather() {
            new GetTodayWeatherTask(progressDialog, this, this).execute();
            new GetLongTermWeatherTask(progressDialog,this,this).execute();
    }



    public static String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
