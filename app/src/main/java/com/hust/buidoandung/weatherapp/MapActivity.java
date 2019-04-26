package com.hust.buidoandung.weatherapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.hust.buidoandung.weatherapp.utils.DefaultValue;

import java.net.MalformedURLException;
import java.net.URL;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static String OWM_TILE_URL = "http://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=fce95bdbd820ccf29a68b9574b50fe50";
    private String tileType = "rain";
    private TileOverlay tileOver;
    SharedPreferences preferences;
    Float latitude;
    Float longtitude;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.rain:
                    tileType="rain";
                   break;
                case R.id.wind:
                    tileType = "wind";
                    break;
                case R.id.temp:
                    tileType = "temp";
                    break;
            }
            if(mMap!=null){
                tileOver.remove();
                setUpMap();
                return  true;
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        preferences=PreferenceManager.getDefaultSharedPreferences(this);
         latitude=preferences.getFloat("lat", DefaultValue.DEFAULT_LAT);
         longtitude=preferences.getFloat("long",DefaultValue.DEFAULT_LONG);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        LatLng coordinate = new LatLng(latitude, longtitude); //Store these lat lng values somewhere. These should be constant.
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                coordinate, 8);
        mMap.animateCamera(location);
        setUpMap();
    }

    private void setUpMap() {
        tileOver = mMap.addTileOverlay(new TileOverlayOptions(). tileProvider(createTransparentTileProvider()));

    }
    private TileProvider createTilePovider() {
        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String fUrl = String.format(OWM_TILE_URL, tileType == null ? "rain" : tileType, zoom, x, y);
                URL url = null;
                try {
                    url = new URL(fUrl);
                }
                catch(MalformedURLException mfe) {
                    mfe.printStackTrace();
                }

                return url;
            }
        } ;

        return tileProvider;
    }
    private TileProvider createTransparentTileProvider() {
        return new TransparentTileOWM(tileType);
    }

}
