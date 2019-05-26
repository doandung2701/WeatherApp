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
import android.os.Parcelable;
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
    //dùng để chuyển sang settingactivity
    private static int SETTINGCODE=1;
    //broadcast internet
    BroadcastReceiver broadcastReceiver;
    //text search
    public String recentCity = "";
    //object giữ data todayWeather
    Weather todayWeather;
    // phần hiển thị của todayWeather
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
    //Quản lý việc lắng nghe location change
    LocationManager locationManager;
    //progess cho activity
    ProgressDialog progressDialog;
    //dữ liệu phần pager
    public List<Weather> longTermWeather = new ArrayList<>();
    public List<Weather> longTermTodayWeather = new ArrayList<>();
    public List<Weather> longTermTomorrowWeather = new ArrayList<>();
    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    //danh sach quyền
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
        //tìm các đối tượng trên vieww
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
        //thiết lập progess dialog
        progressDialog = new ProgressDialog(MainActivity.this);
        //khởi tạo các biến đơn vị cơ bản
        speedUnits.put("m/s", R.string.speed_unit_mps);
        speedUnits.put("kph", R.string.speed_unit_kph);
        speedUnits.put("mph", R.string.speed_unit_mph);

        pressUnits.put("hPa", R.string.pressure_unit_hpa);
        pressUnits.put("kPa", R.string.pressure_unit_kpa);
        pressUnits.put("mm Hg", R.string.pressure_unit_mmhg);
        //load thời tiết
        preloadWeather();
        //check quyền để lấy quyền từ user
        if(!arePermissionsEnabled()){
            requestMultiplePermissions();

        }
        setStatusBarTrans(true);
        //khởi tạo broadcast
        broadcastReceiver=new ConnectivityReceiver();
    }

        //thiết lập trong suốt statusbar
    public void setStatusBarTrans(boolean makeTrans) {
        if (makeTrans) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
    //update data in viewPage
    public void updateLongTermWeatherUI(List<List<Weather>> data) {
        //kiểm tra xem có lây dược dữ liệu k
        if(data!=null){
            //lấy ra các dữ liệu cơ bản
            this.longTermTodayWeather=data.get(0);
            this.longTermTomorrowWeather=data.get(1);
            this.longTermWeather=data.get(2);
            //trường hợp có dữ liệu
            if(longTermTodayWeather!=null&&longTermTomorrowWeather!=null&&longTermWeather!=null){
                //tạo Adapter
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

                Bundle bundle = new Bundle();

                //Fragment today
                bundle.putInt("day", 0);
                WeatherFragment recyclerViewFragmentToday = new WeatherFragment();
                recyclerViewFragmentToday.setArguments(bundle);
                viewPagerAdapter.addFragment(recyclerViewFragmentToday, "Today");

                //fragment tomorrow
                bundle.putInt("day", 1);
                WeatherFragment recyclerViewFragmentTomorrow = new WeatherFragment();
                recyclerViewFragmentTomorrow.setArguments(bundle);
                viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, "Tomorrow");
                //fragment Later
                bundle.putInt("day", 2);
                WeatherFragment recyclerViewFragment = new WeatherFragment();
                recyclerViewFragment.setArguments(bundle);
                viewPagerAdapter.addFragment(recyclerViewFragment,"Later");

                //update data
                viewPagerAdapter.notifyDataSetChanged();
                viewPager.setAdapter(viewPagerAdapter);
                //thiết lập tab với viewpager
                tabLayout.setupWithViewPager(viewPager);
                //thiết lập trang hiện hành
                int currentPage = viewPager.getCurrentItem();
                viewPager.setCurrentItem(currentPage, false);
            }

        }

    }
    //tạo setting option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //huy việc lắng nghe update vị trí
        if(locationManager!=null){
            locationManager.removeUpdates(MainActivity.this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //xử lý việc tìm kiếm
            case R.id.action_search:
                searchCity();
                return true;
                //xử lý sang SettingActivity
            case R.id.action_settings:
                Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                startActivityForResult(intent,SETTINGCODE);
                return true;
                //update thời tiết bằng location GPS hoặc internet
            case R.id.action_location:
                getWeatherByUsingLocationService();
                return true;
                //updatelại dữ liệu
            case R.id.action_refresh:
                if(checkConnection()){
                    getTodayWeather();
                    getLongTermWeather();
                }
             return true;
                //sang mapactivity
            case R.id.action_map:
                Intent intent1=new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent1);
                return true;
            case R.id.action_graphs:
                Intent intent2=new Intent(MainActivity.this,ChartActivity.class);
                intent2.putParcelableArrayListExtra("TemperatureHourly", (ArrayList<? extends Parcelable>) longTermTodayWeather);
                intent2.putParcelableArrayListExtra("TomorrowHourly", (ArrayList<? extends Parcelable>) longTermTomorrowWeather);
                startActivity(intent2);
            default:
                 return super.onOptionsItemSelected(item);
        }
    }
    //cap nhat lai view khi setting thay doi
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //update lại dữ liệu theo các setting mới
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
            //trương hợp phiên bản cao ơn M. viết theo kiểu mới
            if(arePermissionsEnabled()){
                //lấy dịch vụ LOCATION_SERVICE ra
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                progressDialog = new ProgressDialog(this);
                //cho hiển thị progess
                progressDialog.setMessage("Gettting your location...");
                progressDialog.setCancelable(false);
                //set hành động Cancel.Hủy update
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
                //nguồn cập nhật từ mạng
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                }
                //hoặc từ GPS
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
            }else{
                //trường hpự k có đủ quyền. cần yêu cầu quyền từ ng dùng
                requestMultiplePermissions();
            }
        }
    }
    //xu ly viec lay quyen tu nguoi dung
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions(){
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            //Kiểm tra trạng thái quyền
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        //yêu cầu quyền
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsEnabled(){
        //check xem các quyền đã được permit hết chưa
        for(String permission : permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
    //xử lý kéet quả cấp quyền
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 101){
            for(int i=0;i<grantResults.length;i++){
                //kiểm tra từng quyền. nếu chưa được cấp. yêu cầu cấp ngay
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
            //nếu đã đủ quyền. chạy dịch vụ lấy thời tiết qua location
            getWeatherByUsingLocationService();
            //all is good, continue flow
        }
    }
    //return AsyncTask todayWeather
    private void getTodayWeather() {
        new GetTodayWeatherTask(progressDialog,this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //lắng nghe network state change
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        //đăng kí sự kiện
        registerReceiver(broadcastReceiver,intentFilter);
        //preloadWeather();
    }

    @SuppressLint("RestrictedApi")
    private void searchCity() {
        //tạo ra đối tượng AlertDialog
        AlertDialog.Builder alert=new AlertDialog.Builder(this)
                .setTitle("Search for city");
        final EditText searchText=new EditText(this);
        //thiết lập kiểu nhập cho input là text.
        searchText.setInputType(InputType.TYPE_CLASS_TEXT);
        //số dòng tối đa là 1, và chỉ dùng 1 dòng
        searchText.setMaxLines(1);
        searchText.setSingleLine(true);
        //add search Text vào AlertDialog
        alert.setView(searchText,32,0,32,3);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String result=searchText.getText().toString();
                //nếu có text nhập và internet đang hiện hữu, hệ thống lưu lại tên vị trí và cập nhật dữ liệu
                if(!result.isEmpty()&&checkConnection()){
                    saveLocation(result);
                }
            }
        });
        //nếu bấm vào cancel. k thực hiện gì
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //build ra dialog. sau đó có gán thêm animation khi open và close dialog
        AlertDialog dialog = alert.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSearchStyle;
        dialog.show();

    }
        //update vị trí tìm được nếu lấy dữ liệu thành công
    public void saveLocation(String result) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        //lấy ra thành phố gần nhất được tìm kiếm
        recentCity = preferences.getString("city", DefaultValue.DEFAULT_CITY);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", result);
        //update
        editor.commit();
        //kiểm tra.nếu là thành phố mới. láy dữ liệu
        if (!recentCity.equals(result)) {
            getTodayWeather();
            getLongTermWeather();
        }
    }


    //Singh ra các adapter cho 3 Page của Viewpager
    public WeatherAdapter getAdapter(int id) {
        WeatherAdapter weatherFragment;
        if (id == 0) {
            //today
            weatherFragment = new WeatherAdapter(this, longTermTodayWeather);
        } else if (id == 1) {
            //tomorrow
            weatherFragment = new WeatherAdapter(this, longTermTomorrowWeather);
        } else {
            //later
            weatherFragment = new WeatherAdapter(this, longTermWeather);
        }
        return weatherFragment;
    }


        //udpate thời gian cập nhật
    public void updateLastUpdateTime() {
        updateLastUpdateTime(
                PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
        );
    }
    //return AsyncTask GetLongTermWeather
    public void getLongTermWeather() {
        new GetLongTermWeatherTask(progressDialog, this).execute();
    }
    //cập nhật thời gian
    private void updateLastUpdateTime(long timeInMillis) {
        if (timeInMillis < 0) {
            //nếu nhỏ hơn  0. thì set text về rỗng
            lastUpdate.setText("");
        } else {
            //convert thời gian về định dạng chuẩn. rồi hiển thị
            Date lastCheckedDate = new Date(timeInMillis);
            //format ngày tháng năm theo chuẩn
            String timeFormat = getTimeFormat(this).format(lastCheckedDate);
            String dateTime=getDateFormat(this).format(lastCheckedDate) + " " + timeFormat;
            //hiển thị kiểu Last update:\n%s
            lastUpdate.setText(getString(R.string.last_update,dateTime));
        }
    }
    //cập nhật giao diện todayWeather
    public void updateTodayWeatherUI(Weather weather) {
        //nếu có dữ liệu
       if(weather!=null){

           todayWeather=weather;
           //iupdate thời gian cập nhật
           updateLastUpdateTime();
           //lấy format
           DateFormat timeFormat =getTimeFormat(getApplicationContext());
           SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            //lấy thông tin thành phố, quốc gia
           String city = todayWeather.getCity();
           String country = todayWeather.getCountry();
           //cap nhat actionbar
           //update ActionBar tên thành phố, quoóc gia
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
           //convert wind theo đúng chuẩn
           wind = UnitConvertor.convertWind(wind, sp);

           // Ap suat
           double pressure = UnitConvertor.convertPressure((float) Double.parseDouble(todayWeather.getPressure()), sp);
           //sử dụng 2 format .
           //định dạng phần thập phân chỉ có 1 chữ số
           DecimalFormat formatOne=new DecimalFormat("0.#");
           //định dạng phần nguyên chỉ có 1 cs.
           DecimalFormat windFormat=new DecimalFormat("#.0");
           //thiet lap data cho view
           todayTemperature.setText(String.format( UnitConvertor.format(temperature,formatOne)+ " " + sp.getString("unit", "°C")));
           //giá trị miêu tả tình trạng thời tiết. Nếu có mưa, sẽ chèn thêm text mưa vào
           todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() +
                   todayWeather.getDescription().substring(1) + rainString);
           //set giá trị gió. trong đó có thiết lập.Hướng gió hiển thị chữ hay Hình.
           todayWind.setText(getString(R.string.wind) + ": " + UnitConvertor.format((float) wind,windFormat) + " " +
                   localize(sp, "speedUnit", "m/s")+
                   (todayWeather.getWindDirectionDegree()!=null?" "+getWindDirectionString(sp,this,todayWeather):""));
           //thiết lập giá trị áp suất
           todayPressure.setText(getString(R.string.pressure) + ": " +UnitConvertor.format((float)pressure,windFormat) + " " +
                   localize(sp, "pressureUnit", "hPa"));
           //độ ẩm
           todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
           //mặt trời mọc
           todaySunrise.setText(getString(R.string.sunrise) + ": " + timeFormat.format(todayWeather.getSunrise()));
           //mặt trời lặn
           todaySunset.setText(getString(R.string.sunset) + ": " + timeFormat.format(todayWeather.getSunset()));
           //icon
           todayIcon.setText(todayWeather.getIcon());
       }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //hủy đăng kí lấng nghe sự kiện internet change
        unregisterReceiver(broadcastReceiver);

    }

    public  String getWindDirectionString(SharedPreferences sp, Context context, Weather weather) {
        try {
            if (Double.parseDouble(weather.getWind()) != 0) {
                //lấy định dạng được setting
                String pref = sp.getString("windDirectionFormat", null);
                //sử dụng mũi tên
                if ("arrow".equals(pref)) {
                    this.winddirection.setImageResource(R.drawable.up_arrow);
                    //xoay ảnh
                    this.winddirection.setRotation(weather.getWindDirectionDegree().floatValue());
                    return "";
                    //xử dụng text
                } else if ("abbr".equals(pref)) {
                    //xóa ảnh
                    this.winddirection.setImageResource(0);
                    //gán text
                    return UnitConvertor.getWindDirectionString(weather.getWindDirectionDegree());
                }else{
                    //khôgn thì k hiển thị gì
                    this.winddirection.setImageResource(0);
                    return "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return "";
    }

    /***
     *
     * @param sp
     * @param preferenceKey
     * @param defaultValueKey
     * @return Lấy ra phần cài đặt về các kiểu đơn vị
     */
    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return getvalueByPreferenceKey(sp, this, preferenceKey, defaultValueKey);
    }

    /***
     *
     * @param sp
     * @param context
     * @param preferenceKey
     * @param defaultValueKey
     * @return Đơn vị được cài đặt từ Người dùng
     */
    public static String getvalueByPreferenceKey(SharedPreferences sp, Context context, String preferenceKey, String defaultValueKey) {
       //lấy value theo key
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        //cài đặt đơn vị tốc độ
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
          //cài đặt đơn vị về áp suất
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
    }
    //tải lại tất cả dữ liệu
    public void preloadWeather() {
            new GetTodayWeatherTask(progressDialog,  this).execute();
            new GetLongTermWeatherTask(progressDialog,this).execute();
    }


        /*
        Giá trị rain vs snow format kiểu.  Lượng mưa,Lượng tuyết  1h trước, tính theo mm
        "rain":{"3h":0}
        "rain":{"3h":3},
        "snow":{"3h":0}
        "snow":{"3h":3},
        rain.1h Rain volume for the last 1 hour, mm
        rain.3h Rain volume for the last 3 hours, mm

        snow.1h Snow volume for the last 1 hour, mm
        snow.3h Snow volume for the last 3 hours, mm
         */
    public static String getRainOrSnowString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            //lượng mưa,hoặc tuyết 3h trc.
            rain = rainObj.optString("3h", "fail");
            //nếu k có. lấy lượng mưa,hoặc tuyết 1h trước.
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        //nếu k có dữ liệu .trả về 0
        return rain;
    }

    /***
     * Lắng nghe sự kiện thay đổi location bởi GPS hoặc internet
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        //tắt bỏ progress
        progressDialog.dismiss();
        try {
            //xóa bỏ update giúp giảm việc lắng nghe thay đổi
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("LocationManager", "permissions issue", e);
        }
        //lấy thông tin tọa độ thu thập được
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        //lấy ra đối tượng SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        //lưu thông tin vào SharePreferences
        editor.putFloat("lat", (float) latitude);
        editor.putFloat("long", (float) longitude);
        editor.commit();
        //tạo luồng mới xử lý việc lấy tên thành phố thông qua tọa độ
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
