package com.hust.buidoandung.weatherapp;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static String OWM_TILE_URL = "http://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=fce95bdbd820ccf29a68b9574b50fe50";
    private Spinner spinner;
    private String tileType = "temp";
    private TileOverlay tileOver;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        spinner = (Spinner) findViewById(R.id.tileType);

        String[] tileName = new String[]{"Clouds", "Temperature", "Precipitations", "Snow", "Rain", "Wind", "Sea level press."};

        ArrayAdapter<String> adpt = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tileName);

        spinner.setAdapter(adpt);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Check click
                switch (position) {
                    case 0:
                        tileType = "clouds";
                        if (mMap != null) {
                            tileOver.remove();
                            setUpMap();
                        }
                        break;
                    case 1:
                        tileType = "temp";
                        break;
                    case 2:
                        tileType = "precipitation";
                        break;
                    case 3:
                        tileType = "snow";
                        break;
                    case 4:
                        tileType = "rain";
                        break;
                    case 5:
                        tileType = "wind";
                        break;
                    case 6:
                        tileType = "pressure";
                        break;

                }

                if (mMap != null) {
                    tileOver.remove();
                    setUpMap();
                }


            }
        });
//        setUpMap();
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        setUpMap();
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

    private void setUpMap() {
        // Add weather tile
        //tileOver = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(createTilePovider()));

        tileOver = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(createTransparentTileProvider()));
    }

    private TileProvider createTransparentTileProvider() {
        return new TransparentTileOWM(tileType);
    }


    private TileProvider createTilePovider() {
        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String fUrl = String.format(OWM_TILE_URL, tileType == null ? "clouds" : tileType, zoom, x, y);
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
}
