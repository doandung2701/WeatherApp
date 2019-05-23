package com.hust.buidoandung.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hust.buidoandung.weatherapp.adapter.ViewPagerAdapter;
import com.hust.buidoandung.weatherapp.adapter.WeatherAdapter;
import com.hust.buidoandung.weatherapp.adapter.WeatherFragment;
import com.hust.buidoandung.weatherapp.model.Weather;
import com.hust.buidoandung.weatherapp.receiver.ConnectivityReceiver;
import com.hust.buidoandung.weatherapp.tasks.GetCityByCoor;
import com.hust.buidoandung.weatherapp.tasks.GetLongTermWeatherTask;
import com.hust.buidoandung.weatherapp.tasks.GetTodayWeatherTask;
import com.hust.buidoandung.weatherapp.utils.DefaultValue;
import com.hust.buidoandung.weatherapp.utils.UnitConvertor;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;

public class MainActivity extends AppCompatActivity implements LocationListener{
    private static int SETTINGCODE=1;
    BroadcastReceiver broadcastReceiver;
    public String recentCity = "";
    Weather todayWeather;
    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    TextView lastUpdate;
    TextView todayIcon;
    ViewPager viewPager;
    TabLayout tabLayout;
    Typeface weatherFont;
    ImageView winddirection;
    LocationManager locationManager;
    ProgressDialog progressDialog;
    public List<Weather> longTermWeather = new ArrayList<>();
    public List<Weather> longTermTodayWeather = new ArrayList<>();
    public List<Weather> longTermTomorrowWeather = new ArrayList<>();
    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);

    String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.INTERNET};
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setStatusBarTrans(true);
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
        winddirection=findViewById(R.id.winddirection);
        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");
        todayIcon.setTypeface(weatherFont);

        progressDialog = new ProgressDialog(MainActivity.this);
        speedUnits.put("m/s", R.string.speed_unit_mps);
        speedUnits.put("kph", R.string.speed_unit_kph);
        speedUnits.put("mph", R.string.speed_unit_mph);
        speedUnits.put("kn", R.string.speed_unit_kn);

        pressUnits.put("hPa", R.string.pressure_unit_hpa);
        pressUnits.put("kPa", R.string.pressure_unit_kpa);
        pressUnits.put("mm Hg", R.string.pressure_unit_mmhg);
        preloadWeather();
        if(!arePermissionsEnabled()){
            requestMultiplePermissions();

        }
        setStatusBarTrans(true);
        broadcastReceiver=new ConnectivityReceiver();
    }


    public void setStatusBarTrans(boolean makeTrans) {
        if (makeTrans) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
    //update data in viewPage
    public void updateLongTermWeatherUI(List<List<Weather>> data) {
        if(data!=null){
            this.longTermTodayWeather=data.get(0);
            this.longTermTomorrowWeather=data.get(1);
            this.longTermWeather=data.get(2);
            if(longTermTodayWeather!=null&&longTermTomorrowWeather!=null&&longTermWeather!=null){
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

        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationManager!=null){
            locationManager.removeUpdates(MainActivity.this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                searchCity();
                return true;
            case R.id.action_settings:
                Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                startActivityForResult(intent,SETTINGCODE);
                return true;
            case R.id.action_location:
                getWeatherByUsingLocationService();
                return true;
            case R.id.action_refresh:
                if(checkConnection()){
                    getTodayWeather();
                    getLongTermWeather();
                }
             return true;
            case R.id.action_map:
                Intent intent1=new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent1);
                return true;
            default:
                 return super.onOptionsItemSelected(item);
        }
    }
    //cap nhat lai view khi setting thay doi
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                updateTodayWeatherUI(todayWeather);
        List<List<Weather>> dataWeather=new ArrayList<List<Weather>>();
        dataWeather.add(longTermTodayWeather);
        dataWeather.add(longTermTomorrowWeather);
        dataWeather.add(longTermWeather);
        updateLongTermWeatherUI(dataWeather);

    }
    //Su dung LocationService de lay du lieu dia chi
    @SuppressLint("MissingPermission")
    private void getWeatherByUsingLocationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(arePermissionsEnabled()){
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Gettting your location...");
                progressDialog.setCancelable(false);
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            locationManager.removeUpdates(MainActivity.this);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                });
                progressDialog.show();
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                }
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
            }else{
                requestMultiplePermissions();
            }
        }
    }
    //xu ly viec lay quyen tu nguoi dung
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions(){
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsEnabled(){
        for(String permission : permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 101){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    if(shouldShowRequestPermissionRationale(permissions[i])){
                        new AlertDialog.Builder(this)
                                .setMessage("You need to allow permission")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MainActivity.this.requestMultiplePermissions();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                    return;
                }
            }
            getWeatherByUsingLocationService();
            //all is good, continue flow
        }
    }

    private void getTodayWeather() {
        new GetTodayWeatherTask(progressDialog,this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //muc dich de lang nghe viec network state change
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver,intentFilter);
        //preloadWeather();
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
                if(!result.isEmpty()&&checkConnection()){
                    saveLocation(result);
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = alert.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSearchStyle;
        dialog.show();

    }

    public void saveLocation(String result) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        recentCity = preferences.getString("city", DefaultValue.DEFAULT_CITY);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", result);
        editor.commit();
        if (!recentCity.equals(result)) {
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
    public void getLongTermWeather() {
        new GetLongTermWeatherTask(progressDialog, this).execute();
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

    public void updateTodayWeatherUI(Weather weather) {
       if(weather!=null){
           todayWeather=weather;
           updateLastUpdateTime();
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
                   localize(sp, "speedUnit", "m/s")+
                   (todayWeather.getWindDirectionDegree()!=null?" "+getWindDirectionString(sp,this,todayWeather):""));
           todayPressure.setText(getString(R.string.pressure) + ": " +UnitConvertor.format((float)pressure,windFormat) + " " +
                   localize(sp, "pressureUnit", "hPa"));
           todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
           todaySunrise.setText(getString(R.string.sunrise) + ": " + timeFormat.format(todayWeather.getSunrise()));
           todaySunset.setText(getString(R.string.sunset) + ": " + timeFormat.format(todayWeather.getSunset()));
           todayIcon.setText(todayWeather.getIcon());
       }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);

    }

    public  String getWindDirectionString(SharedPreferences sp, Context context, Weather weather) {
        try {
            if (Double.parseDouble(weather.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                if ("arrow".equals(pref)) {
                    this.winddirection.setImageResource(R.drawable.up_arrow);
                    this.winddirection.setRotation(weather.getWindDirectionDegree().floatValue());
                    return "";
                } else if ("abbr".equals(pref)) {
                    this.winddirection.setImageResource(0);
                    return UnitConvertor.getWindDirectionString(weather.getWindDirectionDegree());
                }else{
                    this.winddirection.setImageResource(0);
                    return "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return "";
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

    public void preloadWeather() {
            new GetTodayWeatherTask(progressDialog,  this).execute();
            new GetLongTermWeatherTask(progressDialog,this).execute();
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
        progressDialog.dismiss();
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("LocationManager", "permissions issue", e);
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("lat", (float) latitude);
        editor.putFloat("long", (float) longitude);
        editor.commit();
        new GetCityByCoor(progressDialog,this).execute(Double.toString(latitude),Double.toString(longitude));
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

    private boolean checkConnection(){
        return ConnectivityReceiver.isConnected();
    }
}
