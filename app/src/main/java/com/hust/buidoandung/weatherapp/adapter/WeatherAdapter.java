package com.hust.buidoandung.weatherapp.adapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.hust.buidoandung.weatherapp.MainActivity;
import com.hust.buidoandung.weatherapp.R;
import com.hust.buidoandung.weatherapp.model.Weather;
import com.hust.buidoandung.weatherapp.utils.UnitConvertor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherHolder> {
    //dữ liệu đổ vào
    private List<Weather> itemList;
    //MainActivity
    private Context context;
    public WeatherAdapter(Context context, List<Weather> itemList) {
        this.itemList = itemList;
        this.context = context;
    }
    //su dung view holder nham tang hieu nang
    @Override
    public WeatherHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //inflate đối tượng row_data
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_data, null);
        //sau đó tạo ra đối tựng ViewHolder
        WeatherHolder viewHolder = new WeatherHolder(view);
        return viewHolder;
    }
    //xu ly viec truyen data vao view holder
    @Override
    public void onBindViewHolder( WeatherHolder viewHolder, int i) {
        //lấy đối tượng thứ i
        Weather weatherItem = itemList.get(i);
        //lấy ra đối tượng SharedPreferences để lấy được giá trị cài đặt về cách hiển thị
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        // Temperature
        float temperature = UnitConvertor.convertTemperature(Float.parseFloat(weatherItem.getTemperature()), sp);
        temperature = Math.round(temperature);
        // Rain
        double rain = Double.parseDouble(weatherItem.getRain());
        //đổi đơn vị
        String rainString = UnitConvertor.getRainString(rain, sp);
        // Wind
        double wind;
        try {
            wind = Double.parseDouble(weatherItem.getWind());
        } catch (Exception e) {
            e.printStackTrace();
            wind = 0;
        }
        //đổi đơn vị cho gió
        wind = UnitConvertor.convertWind(wind, sp);
        // Đổi đơn vị cho áp suats
        double pressure = UnitConvertor.convertPressure((float) Double.parseDouble(weatherItem.getPressure()), sp);
        //sử dụng 2 format .
        //định dạng phần thập phân chỉ có 1 chữ số
        DecimalFormat formatOne=new DecimalFormat("0.#");
        //định dạng phần nguyên chỉ có 1 cs.
        DecimalFormat windFormat=new DecimalFormat("#.0");
        //thiet lap data cho view
        viewHolder.itemTemperature.setText(UnitConvertor.format(temperature,formatOne)+ " " + sp.getString("unit", "°C"));
        //giá trị miêu tả tình trạng thời tiết. Nếu có mưa, sẽ chèn thêm text mưa vào
        viewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() +
                weatherItem.getDescription().substring(1) + rainString);
        //set giá trị gió. trong đó có thiết lập.Hướng gió hiển thị chữ hay Hình.
        viewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " +  UnitConvertor.format((float) wind,windFormat) + " " +
                ((MainActivity)context).localize(sp, "speedUnit", "m/s")
                + " " + getWindDirectionString(sp, context, weatherItem,viewHolder.winddirection));
        //thiết lập giá trị áp suất
        viewHolder.itemPressure.setText(context.getString(R.string.pressure) + ": " + UnitConvertor.format((float)pressure,windFormat)  + " " +
                ((MainActivity)context).localize(sp, "pressureUnit", "hPa"));
        viewHolder.itemHumidity.setText(context.getString(R.string.humidity) + ": " + weatherItem.getHumidity() + " %");
        //format thời gian của 1 đơn vị thời tiét
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

        viewHolder.itemDate.setText(sdfTime.format(weatherItem.getDate()));
        //sử dụng typeface để vẽ lên icon
        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        //thiết lập icon
        viewHolder.itemIcon.setTypeface(weatherFont);
        viewHolder.itemIcon.setText(weatherItem.getIcon());

    }
    //ham xu ly huong gio
    public  String getWindDirectionString(SharedPreferences sp, Context context, Weather weather,ImageView imageView) {
        try {
            //nếu có hướng gió được parse ra
            if (Double.parseDouble(weather.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                //TH sử dụng mũi tên hiển thị
                if ("arrow".equals(pref)) {
                    //sử dụng ảnh và xoay góc độ theo góc gió
                    imageView.setImageResource(R.drawable.up_arrow);
                    imageView.setRotation(weather.getWindDirectionDegree().floatValue());
                    return "";
                    //TH sử dụng text N-E
                } else if ("abbr".equals(pref)) {
                    //cho ẩn ảnh và hiển thị text
                    imageView.setImageResource(0);
                    return UnitConvertor.getWindDirectionString(weather.getWindDirectionDegree());
                }else{
                    //không thì k hiện gì
                    imageView.setImageResource(0);
                    return "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return "";
    }
    @Override
    public int getItemCount() {
        if(itemList!=null){
            return  itemList.size();
        }
        return 0;
    }
    //tang hieu nang ung dung.Su dung viewholder
    static class WeatherHolder extends RecyclerView.ViewHolder {
        //date
        public TextView itemDate;
        //nhiệt độ
        public TextView itemTemperature;
        //miêu tả
        public TextView itemDescription;
        //gió
        public TextView itemyWind;
        //áp suất
        public TextView itemPressure;
        //độ ẩm
        public TextView itemHumidity;
        //icon
        public TextView itemIcon;
        //hương gió
        public ImageView winddirection;
        public WeatherHolder(View view) {
            super(view);
            this.itemDate =  view.findViewById(R.id.itemDate);
            this.itemTemperature =  view.findViewById(R.id.itemTemperature);
            this.itemDescription =  view.findViewById(R.id.itemDescription);
            this.itemyWind =  view.findViewById(R.id.itemWind);
            this.itemPressure =  view.findViewById(R.id.itemPressure);
            this.itemHumidity = view.findViewById(R.id.itemHumidity);
            this.itemIcon =  view.findViewById(R.id.itemIcon);
            this.winddirection=view.findViewById(R.id.winddirection);
        }
    }
}
