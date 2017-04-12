package com.example.konstantin.weatherie.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.example.konstantin.weatherie.R;
import com.example.konstantin.weatherie.activities.MainActivity;
import com.example.konstantin.weatherie.activities.SettingsActivity;
import com.example.konstantin.weatherie.helpers.Updater;

import java.text.SimpleDateFormat;
import java.util.Date;


public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    // Sun Feb 19 12:00:00 GMT+00:00 2017
    Date SAMPLE_DATE = new Date(1487505600000l);// long milliseconds
    MainActivity main = new MainActivity();
    private boolean unitsChanged = false;
    SharedPreferences preferences;
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //getActivity().setTheme(getTheme(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("theme", "darksky")));
        super.onCreate(savedInstanceState);

//        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle("Settings");

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplication());
        addPreferencesFromResource(R.xml.prefs);
        setHasOptionsMenu(true);

    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        setCustomDateEnabled();
        updateDateFormatList();

        // Set summaries to current value
        setListPreferenceSummary("unit");
        setListPreferenceSummary("lengthUnit");
        setListPreferenceSummary("speedUnit");
        setListPreferenceSummary("pressureUnit");
        setListPreferenceSummary("refreshInterval");
        setListPreferenceSummary("windDirectionFormat");
        setListPreferenceSummary("theme");
    }

    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("unitsChanged", unitsChanged);
        editor.commit();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "unit":
            case "lengthUnit":
            case "speedUnit":
            case "pressureUnit":
            case "windDirectionFormat":
                unitsChanged = true;
                setListPreferenceSummary(key);
                break;
            case "refreshInterval":
                setListPreferenceSummary(key);
                Updater.setRecurringAlarm(getActivity());
                break;
            case "dateFormat":
                setCustomDateEnabled();
                setListPreferenceSummary(key);
                unitsChanged = true;
                break;
            case "dateFormatCustom":
                updateDateFormatList();
                unitsChanged = true;
                break;
            case "theme":
                // Restart activity to apply theme
                getActivity().overridePendingTransition(0, 0);
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
                startActivity(getActivity().getIntent());
                break;
            case "updateLocationAutomatically":
                if (sharedPreferences.getBoolean(key, false) == true) {
                    ((SettingsActivity)getActivity()).requestReadLocationPermission();
                }
                break;
        }
    }

    public void setGranted(boolean permissionGranted) {
        CheckBoxPreference checkBox = (CheckBoxPreference) findPreference("updateLocationAutomatically");
        if(permissionGranted){
            checkBox.setChecked(permissionGranted);
            if (permissionGranted) {
                privacyGuardWorkaround();
            }
        }
        else{
            checkBox.setChecked(permissionGranted);
        }
    }

    public void privacyGuardWorkaround() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            DummyLocationListener dummyLocationListener = new DummyLocationListener();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, dummyLocationListener);
            locationManager.removeUpdates(dummyLocationListener);
        } catch (SecurityException e) {

        }
    }

    private void setListPreferenceSummary(String preferenceKey) {

        ListPreference preference = (ListPreference) findPreference(preferenceKey);
        preference.setSummary(preference.getEntry());
    }

    private void setCustomDateEnabled() {
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        Preference customDatePref = findPreference("dateFormatCustom");
        customDatePref.setEnabled("custom".equals(sp.getString("dateFormat", "")));
    }

    private void updateDateFormatList() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Resources res = getResources();

        ListPreference dateFormatPref = (ListPreference) findPreference("dateFormat");
        String[] dateFormatsValues = res.getStringArray(R.array.dateFormatsValues);
        String[] dateFormatsEntries = new String[dateFormatsValues.length];

        EditTextPreference customDateFormatPref = (EditTextPreference) findPreference("dateFormatCustom");
        customDateFormatPref.setDefaultValue(dateFormatsValues[0]);

        SimpleDateFormat sdformat = new SimpleDateFormat();
        for (int i=0; i<dateFormatsValues.length; i++) {
            String value = dateFormatsValues[i];
            if ("custom".equals(value)) {
                String renderedCustom;
                try {
                    sdformat.applyPattern(sp.getString("dateFormatCustom", dateFormatsValues[0]));
                    renderedCustom = sdformat.format(SAMPLE_DATE);
                } catch (IllegalArgumentException e) {
                    renderedCustom = res.getString(R.string.error_dateFormat);
                }
                dateFormatsEntries[i] = String.format("%s:\n%s",
                        res.getString(R.string.setting_dateFormatCustom),
                        renderedCustom);
            } else {
                sdformat.applyPattern(value);
                dateFormatsEntries[i] = sdformat.format(SAMPLE_DATE);
            }
        }

        dateFormatPref.setDefaultValue(dateFormatsValues[0]);
        dateFormatPref.setEntries(dateFormatsEntries);

        setListPreferenceSummary("dateFormat");
    }

    public class DummyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}

