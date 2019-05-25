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
    private List<Weather> itemList;
    private Context context;
    public WeatherAdapter(Context context, List<Weather> itemList) {
        this.itemList = itemList;
        this.context = context;
    }
    //su dung view holder nham tang hieu nang
    @Override
    public WeatherHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_data, null);
        WeatherHolder viewHolder = new WeatherHolder(view);
        return viewHolder;
    }
    //xu ly viec truyen data vao view holder
    @Override
    public void onBindViewHolder( WeatherHolder viewHolder, int i) {
        Weather weatherItem = itemList.get(i);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        // Temperature
        float temperature = UnitConvertor.convertTemperature(Float.parseFloat(weatherItem.getTemperature()), sp);
        temperature = Math.round(temperature);
        // Rain
        double rain = Double.parseDouble(weatherItem.getRain());
        String rainString = UnitConvertor.getRainString(rain, sp);
        // Wind
        double wind;
        try {
            wind = Double.parseDouble(weatherItem.getWind());
        } catch (Exception e) {
            e.printStackTrace();
            wind = 0;
        }
        wind = UnitConvertor.convertWind(wind, sp);
        // Pressure
        double pressure = UnitConvertor.convertPressure((float) Double.parseDouble(weatherItem.getPressure()), sp);
        DecimalFormat formatOne=new DecimalFormat("0.#");
        DecimalFormat windFormat=new DecimalFormat("#.0");
        //thiet lap data cho view
        viewHolder.itemTemperature.setText(UnitConvertor.format(temperature,formatOne)+ " " + sp.getString("unit", "°C"));
        viewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() +
                weatherItem.getDescription().substring(1) + rainString);
        viewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " +  UnitConvertor.format((float) wind,windFormat) + " " +
                    MainActivity.localize(sp, context, "speedUnit", "m/s")
                + " " + getWindDirectionString(sp, context, weatherItem,viewHolder.winddirection));

        viewHolder.itemPressure.setText(context.getString(R.string.pressure) + ": " + UnitConvertor.format((float)pressure,windFormat)  + " " +
                MainActivity.localize(sp, context, "pressureUnit", "hPa"));
        viewHolder.itemHumidity.setText(context.getString(R.string.humidity) + ": " + weatherItem.getHumidity() + " %");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

        viewHolder.itemDate.setText(sdfTime.format(weatherItem.getDate()));
        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        viewHolder.itemIcon.setTypeface(weatherFont);
        viewHolder.itemIcon.setText(weatherItem.getIcon());

    }
    //ham xu ly huong gio
    public  String getWindDirectionString(SharedPreferences sp, Context context, Weather weather,ImageView imageView) {
        try {
            if (Double.parseDouble(weather.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                //truong hop cai dat la dung mui ten
                if ("arrow".equals(pref)) {
                    imageView.setImageResource(R.drawable.up_arrow);
                    imageView.setRotation(weather.getWindDirectionDegree().floatValue());
                    return "";
                    //truong hop su dung text kieu N-E
                } else if ("abbr".equals(pref)) {
                    imageView.setImageResource(0);
                    return UnitConvertor.getWindDirectionString(weather.getWindDirectionDegree());
                }else{
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
        public TextView itemDate;
        public TextView itemTemperature;
        public TextView itemDescription;
        public TextView itemyWind;
        public TextView itemPressure;
        public TextView itemHumidity;
        public TextView itemIcon;
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
