package com.example.konstantin.weatherie.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.konstantin.weatherie.R;
import com.example.konstantin.weatherie.fragments.SettingsFragment;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getTheme(PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "darksky")));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Display the fragment as the main content.

//        getFragmentManager().beginTransaction()
//                .replace(android.R.id.content, new SettingsFragment())
//                .commit();
    }
    public void requestReadLocationPermission() {
        System.out.println("Calling request location permission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MainActivity.MY_PERMISSIONS_ACCESS_FINE_LOCATION);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MainActivity.MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }
        } else {
            ((SettingsFragment)getFragmentManager().findFragmentById(R.id.settings_fragment)).privacyGuardWorkaround();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MainActivity.MY_PERMISSIONS_ACCESS_FINE_LOCATION) {
            boolean permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            ((SettingsFragment)getFragmentManager().findFragmentById(R.id.settings_fragment)).setGranted(permissionGranted);

        }
    }

    private int getTheme(String themePref) {
        switch (themePref) {
            case "clearblue":
                return R.style.AppTheme;
            case "dark":
                return R.style.AppTheme_Dark;
            case "classic":
                return R.style.AppTheme_Classic;
            case "classicdark":
                return R.style.AppTheme_Classic_Dark;
            case "clearsky":
                return R.style.AppTheme_Classic_Dark_ClearSky;
            case "clover":
                return R.style.AppTheme_Classic_Dark_Clover;
            default:
                return R.style.AppTheme_Classic_Dark_DarkSky;


        }
    }
}
