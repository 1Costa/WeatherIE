package com.example.konstantin.weatherie.WorldMap;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.konstantin.weatherie.R;
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

public class WorldMapActivity extends FragmentActivity{ // implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static String OWM_TILE_URL = "http://tile.openweathermap.org/map/%s/%d/%d/%d.png";
    private Spinner spinner;
    private String tileType = "clouds";
    private TileOverlay tileOver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String apiKey = sp.getString("apiKey", getResources().getString(R.string.apiKey));
        final WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/map.html?lat=" + prefs.getFloat("latitude", 0) + "&lon=" + prefs.getFloat("longitude", 0) + "&appid=" + apiKey);
        spinner = (Spinner) findViewById(R.id.tileType);

        String[] tileName = new String[]{"Clouds", "Temperature", "Precipitations", "Snow", "Rain", "Wind", "Sea level press."};
        final String removeLayers =("map.removeLayer(cloudsLayer);" +
                "map.removeLayer(temperatureLayer);map.removeLayer(precipitationsLayer);map.removeLayer(snowLayer);" +
                "map.removeLayer(rainLayer);map.removeLayer(windLayer);map.removeLayer(seaLevelPressLayer);");

        ArrayAdapter<String> adpt = new ArrayAdapter<String>(this, R.layout.spinner_custom_item, tileName);

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
                        //tileType = "clouds";
                        webView.loadUrl("javascript:" + removeLayers + "map.addLayer(cloudsLayer);");
                        break;
                    case 1:
                        //tileType = "temp";
                        webView.loadUrl("javascript:" + removeLayers + "map.addLayer(temperatureLayer);");
                        break;
                    case 2:
                        //tileType = "precipitation";
                        webView.loadUrl("javascript:" + removeLayers + "map.addLayer(precipitationsLayer);");
                        break;
                    case 3:
                        //tileType = "snow";
                        webView.loadUrl("javascript:" + removeLayers + "map.addLayer(snowLayer);");
                        break;
                    case 4:
                        //tileType = "rain";
                        webView.loadUrl("javascript:" + removeLayers + "map.addLayer(rainLayer);");
                        break;
                    case 5:
                        //tileType = "wind";
                        webView.loadUrl("javascript:" + removeLayers + "map.addLayer(windLayer);");
                        break;
                    case 6:
                        //tileType = "pressure";
                        webView.loadUrl("javascript:" + removeLayers + "map.addLayer(seaLevelPressLayer);");
                        break;

                }

//                if (mMap != null) {
//                    tileOver.remove();
//                    setUpMap();
//                }
            }
        });
       // setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
    }

//    private void setUpMapIfNeeded() {
//        // Do a null check to confirm that we have not already instantiated the map.
//        if (mMap == null) {
//            // Try to obtain the map from the SupportMapFragment.
//
//            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                    .findFragmentById(R.id.map);
//            mapFragment.getMapAsync(this);
//            // Check if we were successful in obtaining the map.
//            if (mMap != null) {
//                setUpMap();
//            }
//        }
//    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // Add weather tile

//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //tileOver = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(createTilePovider()));

        tileOver = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(createTransparentTileProvider()));
    }
    private TileProvider createTransparentTileProvider() {
        return new TransparentTileCustomLayerOWM(tileType);
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
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        setUpMap();
//    }
}
