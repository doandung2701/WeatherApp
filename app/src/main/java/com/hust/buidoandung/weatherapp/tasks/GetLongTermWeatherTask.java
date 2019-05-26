package com.hust.buidoandung.weatherapp.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.gson.JsonArray;
import com.hust.buidoandung.weatherapp.utils.DefaultValue;
import com.hust.buidoandung.weatherapp.MainActivity;
import com.hust.buidoandung.weatherapp.model.Weather;
import com.hust.buidoandung.weatherapp.utils.UnitConvertor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
/*
Lấy thông tin thời tiếp 5 ngày liên tiếp
 */
public class GetLongTermWeatherTask extends AsyncTask<String,String, List<List<Weather>>> {
//    progess
ProgressDialog progressDialog;
//main activity
    MainActivity mainActivity;
    public GetLongTermWeatherTask(ProgressDialog progressDialog, MainActivity activity) {
        this.progressDialog=progressDialog;
        this.mainActivity=activity;
    }

    /**
     *
     * @param response
     * @return Danh sách 3 phần tử chứa 3 danh sách dữ liệu về thời tiết
     * @throws Exception
     */
    private List<List<Weather>> parseTodayJson(String response) throws Exception {
        int i;
        try {
            //đọc object
            JSONObject reader = new JSONObject(response);
            //lây ra mã trả về
            final String code = reader.optString("cod");
            //nếu lỗi
            if ("404".equals(code)) {
                return null;
            }
            //khởi tạo dữ liệu cho tab
            List<List<Weather>> data=new ArrayList<List<Weather>>();
            //những ngày còn lại
            List<Weather> longTermWeather=new ArrayList<>();
            //ngày hôm nay
            List<Weather> longTermTodayWeather=new ArrayList<>();
            //ngày mai
            List<Weather> longTermTomorrowWeather=new ArrayList<>();
            //đọc list data
            JSONArray list = reader.getJSONArray("list");
            //sử dùng thời gian của hệ thống hiện tại để phân chia dữ liệu
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            //ngày hôm nay
            int thisToday=today.get(Calendar.DAY_OF_YEAR);
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();
                //lấy phần tử đầu tiên
                JSONObject listItem = list.getJSONObject(i);
                //các thông tin chính
                JSONObject main = listItem.getJSONObject("main");
                //nhiệt độ
                weather.setTemperature(main.getString("temp"));
                //áp ssất
                weather.setPressure(main.getString("pressure"));
                //độ ẩm
                weather.setHumidity(main.getString("humidity"));
                //date
                weather.setDate(listItem.getString("dt_txt"));
                JSONArray wt=listItem.optJSONArray("weather");
                // lấy miêu tả
                weather.setDescription(wt.getJSONObject(0).getString("description"));
                JSONObject windObj = listItem.optJSONObject("wind");
                // xử lý nếu tồn tại data về gió
                if (windObj != null) {
                    weather.setWind(windObj.getString("speed"));
                    weather.setWindDirectionDegree(windObj.getDouble("deg"));

                }

            //rain
                JSONObject rainObj = listItem.optJSONObject("rain");
                String rain = "";
                //nếu có mưa
                if (rainObj != null) {
                    rain = MainActivity.getRainOrSnowString(rainObj);
                } else {
                    //nếu có tuyết
                    JSONObject snowObj = listItem.optJSONObject("snow");
                    if (snowObj != null) {
                        //lây ra thông tin
                        rain = MainActivity.getRainOrSnowString(snowObj);
                    } else {
                        //k thì gán bằng 0.tức k mưa
                        rain = "0";
                    }
                }
                weather.setRain(rain);
                //lấy ra id : Weather condition id. miêu tả tình trạng thời tiết
                final String idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id");
                //set icon
                weather.setIcon(UnitConvertor.setWeatherIcon(Integer.parseInt(idString),today.get(Calendar.HOUR_OF_DAY)));
                weather.setId(idString);
                //convert date trả vè để xử lý
                Calendar cal=Calendar.getInstance();

                cal.setTime(weather.getDate());
                //nêu thuộc ngày hôm nay

                if (cal.get(Calendar.DAY_OF_YEAR) == thisToday) {
                    longTermTodayWeather.add(weather);
                    //ngày mai
                } else if (cal.get(Calendar.DAY_OF_YEAR) == thisToday + 1) {
                    longTermTomorrowWeather.add(weather);
                } else {
                    //còn lại các ngày sau
                   longTermWeather.add(weather);
                }
            }
            //thêm dữ liệu vào response

            data.add(longTermTodayWeather);
            data.add(longTermTomorrowWeather);
            data.add(longTermWeather);
            return data;
        } catch (JSONException e) {
            //parse lỗi . trả về null
            Log.e("JSONException Data", response);
            e.printStackTrace();
            return null;
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

    @Override
    protected void onPostExecute(final List<List<Weather>> lists) {
        //tắt progess
        progressDialog.dismiss();
        //xử lý lỗi. ta đẩy thông báo lên Snackbar
        if(lists==null){
            Snackbar.make(mainActivity.findViewById(android.R.id.content), "Specified city is not found.", Snackbar.LENGTH_LONG).show();
        }else{
            //nếu k. update dữ liệu
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.updateLongTermWeatherUI(lists);

                }
            });
        }
    }
/*
Đối tượng trả về 1 list danh sach gồm 3 luồng dữ liêu. Hôm nay,ngày mai , và 3 ngày còn lại
 */
    @Override
    protected List<List<Weather>> doInBackground(String... strings) {
        try {
            List<List<Weather>> data;
            //sinh đối tượng URL
            URL url=createURL();
            String response="";
            //mở kết nối
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            int responseCode=connection.getResponseCode();
            if(responseCode==200){
                //đọc dữ liệu trả về sử dụng buffer
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader r = new BufferedReader(inputStreamReader);
                String line=null;
                //nối dữ liệu vào response
                while ((line=r.readLine())!=null){
                    response+=line;
                }
                //đóng các luồng
                r.close();
                connection.disconnect();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity).edit();
                Calendar now = Calendar.getInstance();
                //Cập nhật thời gian  update
                editor.putLong("lastUpdate", now.getTimeInMillis());
                //lưu lại giá trị này trường hợp k có mạng, ta sẽ lấy dữ liệu này làm cache
                editor.putString("longTermTodayWeather",response);
                editor.commit();
                //convert về đối tượng
                data=parseTodayJson(response);
                return data;
            }else{
                Snackbar.make(mainActivity.findViewById(android.R.id.content), "Specified city is not found.", Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }//Trường hợp lỗi, ta sé lấy dữ liệu từ  SharedPreferences là dữ liệu gần đây nhất.
        SharedPreferences editor = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String response=editor.getString("longTermTodayWeather","");

        if(!response.equals("")){
            try {
                //pase ngay đối tượng về dữ liệu cần response cho mainactivity
                return parseTodayJson(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }//trường hợp k có . trả về null
        return null;
    }
    //sinh đối tượng URL
    private URL createURL() throws Exception{
        //lấy key
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String apiKey=sp.getString("apiKey","fce95bdbd820ccf29a68b9574b50fe50");
        StringBuilder stringBuilder=new StringBuilder("http://api.openweathermap.org/data/2.5/");
        //vì lá lấy dữ liệu 5 ngày, nên ta thêm forecast vào URL
        stringBuilder.append("forecast").append("?");
        //gán tên thành phố
        final String city = sp.getString("city", DefaultValue.DEFAULT_CITY);
        //nối tên thành phố
        stringBuilder.append("q=").append(URLEncoder.encode(city, "UTF-8"));
        //ngôn ngữ
        stringBuilder.append("&lang=").append(Locale.getDefault().getLanguage());
        //chế độ jsson
        stringBuilder.append("&mode=json");
        //nối api
        stringBuilder.append("&appid=").append(apiKey);
        return new URL(stringBuilder.toString());
    }
}
