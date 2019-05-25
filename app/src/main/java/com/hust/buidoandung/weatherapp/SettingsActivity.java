package com.hust.buidoandung.weatherapp;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.Observable;
import java.util.Observer;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        View bar = LayoutInflater.from(this).inflate(R.layout.activity_settings, root, false);
        root.addView(bar, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_settings);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addPreferencesFromResource(R.xml.preferences);
    }
    //lang nghe su kien khi thay doi cai dat
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "unit":
            case "speedUnit":
            case "pressureUnit":
            case  "windDirectionFormat":
                setListPreferenceSummary(key);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //đăng kí sự kiện setting change tron ghàm onResume
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setListPreferenceSummary("unit");
        setListPreferenceSummary("speedUnit");
        setListPreferenceSummary("pressureUnit");
        setListPreferenceSummary("windDirectionFormat");

    }
    private void setListPreferenceSummary(String preferenceKey) {
        ListPreference preference = (ListPreference) findPreference(preferenceKey);
        preference.setSummary(preference.getEntry());
    }
    //huy dang ky su kien
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void finish() {
        super.finish();
    }

}
