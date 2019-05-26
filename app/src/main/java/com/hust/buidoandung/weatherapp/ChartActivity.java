package com.hust.buidoandung.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.hust.buidoandung.weatherapp.model.Weather;
import com.hust.buidoandung.weatherapp.utils.UnitConvertor;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity {

    private LinearLayout chartLyt;
    private LinearLayout chartLyt1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        getSupportActionBar().hide(); // hide the title bar

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_chart);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ArrayList<Weather> todayHourly = new ArrayList<Weather>();
        todayHourly = (ArrayList<Weather>) getIntent().getSerializableExtra("TemperatureHourly"); //lấy ra một list các đối tượng từ intent
//        todayHourly = (ArrayList<Weather>) getIntent().getSerializableExtra("TomorrowHourly"); //lấy ra một list các đối tượng từ intent

        chartLyt = findViewById(R.id.chart); // tìm thành phàn view có id là chart

        XYSeries series = new XYSeries("Temperature hourly");
        int hour = (8-todayHourly.size())*3;
        float maxY = -1000;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        for (Weather hf : todayHourly) {
            float temperature = UnitConvertor.convertTemperature(Float.parseFloat(hf.getTemperature()), sp);
            temperature = Math.round(temperature);
            series.add(hour, temperature);
            hour += 3;
            if (maxY < temperature) {
                maxY = temperature;
            }
        }
//        for (int i = 0; i <= 24; i += 2) {
//            series.add(i, i + 20); //toa do cac diem
//        }
        // Now we create the renderer
        XYSeriesRenderer renderer = new XYSeriesRenderer(); //các điểm
        renderer.setLineWidth(2);
        renderer.setColor(Color.RED);
        // Include low and max value
        renderer.setDisplayBoundingPoints(true);
        // we add point markers
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setPointStrokeWidth(3);
        renderer.setDisplayChartValues(true); //hiện thị giá trị của các điểm
        renderer.setChartValuesTextSize(30); // set kích cỡ các giá trị hiển thị

        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); //đồ thị
        mRenderer.addSeriesRenderer(renderer);
        mRenderer.setLabelsTextSize(25);
        mRenderer.setLegendTextSize(20); //set kích cỡ chữ chú thích
        mRenderer.setZoomEnabled(false,false); //không zoom được đồ thị

        // We want to avoid black border
        // transparent margins
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(maxY+20); //tim ra max hiện thị trên trục y
        mRenderer.setYAxisMin(0);   //set min hiện thị trên trục y
        mRenderer.setShowGrid(true); // we show the grid

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);
        GraphicalView chartView = ChartFactory.
                getLineChartView(this, dataset, mRenderer);

        if(todayHourly.size() != 0) { // khi mà không còn tham số cho thời tiết hôm nay sẽ ko hiện thị chart
            chartLyt.addView(chartView,0);
        } else {
            chartLyt.setVisibility(View.GONE); // Ẩn view chart hourly
            findViewById(R.id.txtHourly).setVisibility(View.GONE); // Ẩn view text hourly
        }

        //End chart today

        ArrayList<Weather> tomorrowHourly = new ArrayList<Weather>();
        tomorrowHourly = (ArrayList<Weather>) getIntent().getSerializableExtra("TomorrowHourly"); //lấy ra một list các đối tượng từ intent
        chartLyt1 = findViewById(R.id.chart1); // tìm thành phàn view có id là chart1

        XYSeries series1 = new XYSeries("Temperature tomorrow hourly");
        int hour1 = (8-tomorrowHourly.size())*3;
        System.out.println("HOUR1"+hour1);
        float maxY1 = -1000;
        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(this);
        for (Weather hf : tomorrowHourly) {
            float temperature = UnitConvertor.convertTemperature(Float.parseFloat(hf.getTemperature()), sp);
            temperature = Math.round(temperature);
            series1.add(hour1, temperature);
            hour1 += 3;
            if (maxY1 < temperature) {
                maxY1 = temperature;
            }
        }

        XYSeriesRenderer renderer1 = new XYSeriesRenderer(); //các điểm
        renderer1.setLineWidth(2);
        renderer1.setColor(Color.RED);
        // Include low and max value
        renderer1.setDisplayBoundingPoints(true);
        // we add point markers
        renderer1.setPointStyle(PointStyle.CIRCLE);
        renderer1.setPointStrokeWidth(3);
        renderer1.setDisplayChartValues(true); //hiện thị giá trị của các điểm
        renderer1.setChartValuesTextSize(30); // set kích cỡ các giá trị hiển thị

        XYMultipleSeriesRenderer mRenderer1 = new XYMultipleSeriesRenderer(); //đồ thị
        mRenderer1.addSeriesRenderer(renderer1);
        mRenderer1.setLabelsTextSize(25);
        mRenderer1.setLegendTextSize(20); //set kích cỡ chữ chú thích
        mRenderer1.setZoomEnabled(false,false); //không zoom được đồ thị

        // We want to avoid black border
        // transparent margins
        mRenderer1.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        // Disable Pan on two axis
        mRenderer1.setPanEnabled(false, false);
        mRenderer1.setYAxisMax(maxY1+20); //tim ra max hiện thị trên trục y
        mRenderer1.setYAxisMin(0);   //set min hiện thị trên trục y
        mRenderer1.setShowGrid(true); // we show the grid

        XYMultipleSeriesDataset dataset1 = new XYMultipleSeriesDataset();
        dataset1.addSeries(series1);
        GraphicalView chartView1 = ChartFactory.
                getLineChartView(this, dataset1, mRenderer1);

        chartLyt1.addView(chartView1,0);

        //End chart tomorrow
    }
}
