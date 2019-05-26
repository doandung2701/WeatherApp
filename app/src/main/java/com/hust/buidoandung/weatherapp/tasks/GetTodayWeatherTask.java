package com.hust.buidoandung.weatherapp.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.hust.buidoandung.weatherapp.utils.DefaultValue;
import com.hust.buidoandung.weatherapp.MainActivity;
import com.hust.buidoandung.weatherapp.model.Weather;
import com.hust.buidoandung.weatherapp.utils.UnitConvertor;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 */
public class GetTodayWeatherTask extends AsyncTask<String,String, Weather> {
    //khai báo biến cần sử dụng
    ProgressDialog progressDialog;
    MainActivity mainActivity;
    public GetTodayWeatherTask(ProgressDialog progressDialog,  MainActivity activity) {
       this.progressDialog=progressDialog;
       this.mainActivity=activity;
    }

    @Override
    protected Weather doInBackground(String... strings) {

        try {
            Weather weather;
            //tạo URL
            URL url=createURL();
            String response="";
            //mở kết nối
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            int responseCode=connection.getResponseCode();
            //trường hợp thành công
            if(responseCode==200){
                //tạo đối tượng đọc buffer
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader r = new BufferedReader(inputStreamReader);
                String line=null;
                //đọc dữ liệu trả về
                while ((line=r.readLine())!=null){
                    response+=line;
                }
                //đóng kết nối
                r.close();
                connection.disconnect();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity).edit();
                Calendar now = Calendar.getInstance();
                //update thời gian cập nhật
                editor.putLong("lastUpdate", now.getTimeInMillis());
                //tạo cache bằng cách lưu giá trị todayweather gần nhất vào SharedPreferences
                editor.putString("todayWeather",response);
                editor.commit();
                //parse ra object
                weather=parseTodayJson(response);
                return weather;
            }
        } catch (Exception e) {
            e.printStackTrace();


        }
        //trường hợp lỗi . xử lý bằng cách lấy dữ liệu cache đã lưu trong SharedPreferences
        SharedPreferences editor = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String response=editor.getString("todayWeather","");
        if(response!=""){
            return parseTodayJson(response);
        }
        //nếu k có cache. đành return null :(
        return null;
    }

    @Override
    protected void onPostExecute(final Weather weather) {
        //tắt progess
        progressDialog.dismiss();
        //nếu lỗi
        if(weather==null){
            Snackbar.make(mainActivity.findViewById(android.R.id.content), "Specified city is not found.", Snackbar.LENGTH_LONG).show();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            //update lại city trong SharedPreferences về giá trị default: HN
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("city", DefaultValue.DEFAULT_CITY);
            editor.commit();
        }else{
            //update giá trị lat, long trong SharedPreferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat("lat", weather.getLat());
            editor.putFloat("long", weather.getLog());
            editor.commit();
            //cập nhật giao diện
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //update địa chỉ
                    mainActivity.saveLocation(weather.getCity()+", "+weather.getCountry());
                    //update view todayWeather
                    mainActivity.updateTodayWeatherUI(weather);
                }
            });
        }
    }

    @Override
    protected void onPreExecute() {
         //Hiển thị progess. hủy action khi ng dùng bấm ra ngoài progess.
        if(!progressDialog.isShowing()){
            progressDialog.setMessage("Waiting...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }




    private Weather parseTodayJson(String response) {

        try {
            JSONObject reader = new JSONObject(response);
            //lây ra mã trả về
            final String code = reader.optString("cod");
            //nếu lỗi
            if(code.equals("404")){
                return null;
            }
            Weather weather=new Weather();
            //prase thong tin ve thanh pho
            //tên
            String city = reader.getString("name");
            //tọa độ
            JSONObject coord=reader.optJSONObject("coord");
            weather.setLat((float) coord.getDouble("lat"));
            weather.setLog((float)coord.getDouble("lon"));
            String country = "";
            //thành phố.
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
                //t.g mặt trời  mọc
                //t.g mặt trời lặn
                weather.setSunrise(countryObj.getString("sunrise"));
                weather.setSunset(countryObj.getString("sunset"));
            }
            weather.setCity(city);
            weather.setCountry(country);
            //lay thong tin ve nhiet do,do am...
            JSONObject main = reader.getJSONObject("main");
            //nhiệt độ
            weather.setTemperature(main.getString("temp"));
            //ap suâts
            weather.setPressure(main.getString("pressure"));
            //độ ẩm
            weather.setHumidity(main.getString("humidity"));
            //mieu ta ve tinh trang thoi tiet
            JSONObject wt=reader.getJSONArray("weather").getJSONObject(0);
            weather.setDescription(wt.getString("description"));
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            //lấy id
            String idString = wt.getString("id");
            //thiêt lập icon
            weather.setIcon(UnitConvertor.setWeatherIcon(Integer.parseInt(idString),today.get(Calendar.HOUR_OF_DAY)));

            weather.setId(idString);
            //gio va huong cua gio
            JSONObject windObj = reader.getJSONObject("wind");
            weather.setWind(windObj.getString("speed"));
            //xu ly huong gio
            if (windObj.has("deg")) {

                weather.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                Log.e("parseTodayJson", "No wind direction available");
                weather.setWindDirectionDegree(null);
            }

            //xu ly ve gia tri mua
            JSONObject rainObj = reader.optJSONObject("rain");
            String rain;
            if (rainObj != null) {
                //trời mưa
                rain = MainActivity.getRainOrSnowString(rainObj);
            } else {
                //trời có tuyết
                JSONObject snowObj = reader.optJSONObject("snow");
                if (snowObj != null) {
                    rain = MainActivity.getRainOrSnowString(snowObj);
                } else {
                    rain = "0";
                }
            }
            weather.setRain(rain);

            return weather;
        }catch (Exception e){
            Log.d("Exception",e.getMessage());
            return null;
        }
    }

    /***
     *
     * @return URL
     * @throws Exception
     */
    private URL createURL() throws Exception{
        //lấy api
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String apiKey=sp.getString("apiKey","fce95bdbd820ccf29a68b9574b50fe50");
        StringBuilder stringBuilder=new StringBuilder("http://api.openweathermap.org/data/2.5/");
        //lấy thòi tiết hôm nay nên sử dụng weather
        stringBuilder.append("weather").append("?");
        //nối tên thành phố
        final String city = sp.getString("city", DefaultValue.DEFAULT_CITY);
        stringBuilder.append("q=").append(URLEncoder.encode(city, "UTF-8"));
        //ngôn ngữ
        stringBuilder.append("&lang=").append(Locale.getDefault().getLanguage());
        stringBuilder.append("&mode=json");
        //api
        stringBuilder.append("&appid=").append(apiKey);
        return new URL(stringBuilder.toString());
    }

}