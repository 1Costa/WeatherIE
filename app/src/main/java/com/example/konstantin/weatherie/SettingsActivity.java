package com.example.konstantin.weatherie;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getTheme(PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "fresh")));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Display the fragment as the main content.

//        getFragmentManager().beginTransaction()
//                .replace(android.R.id.content, new SettingsFragment())
//                .commit();
    }

    private int getTheme(String themePref) {
        switch (themePref) {
            case "dark":
                return R.style.AppTheme_Dark;
            case "classic":
                return R.style.AppTheme_Classic;
            case "classicdark":
                return R.style.AppTheme_Classic_Dark;
            default:
                return R.style.AppTheme;
        }
    }
}
