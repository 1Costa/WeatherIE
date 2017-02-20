package com.example.konstantin.weatherie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.konstantin.weatherie.adapters.RecyclerViewFiveDaysFragment;
import com.example.konstantin.weatherie.adapters.RecyclerViewFragment;
import com.example.konstantin.weatherie.adapters.ViewPagerAdapter;
import com.example.konstantin.weatherie.adapters.WeatherRecyclerAdapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FiveDaysForecastActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    private ArrayList<Weather> longTermWeather = new ArrayList<>();
    private ArrayList<Weather> fiveDaysWeather = new ArrayList<>();
    private ListView fiveDaysListView;
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd-MM");
    SimpleAdapter adapter;
    ViewPager viewPager;
    TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_days_forecast);

        // Load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fiveDaysListView = (ListView)findViewById(R.id.fiveDaysListView);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);


        longTermWeather = (ArrayList<Weather>) getIntent().getSerializableExtra("forecastDetailed");
        fiveDaysWeather = (ArrayList<Weather>) getIntent().getSerializableExtra("forecastFiveDays");
        //progressDialog = new ProgressDialog(FiveDaysForecastActivity.this);
        //preloadWeather();
       // updateLastUpdateTime();
        updateListView();
        updateLongTermWeatherUI();
    }

    private void updateListView() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FiveDaysForecastActivity.this);

        // create a List of Map<String, ?> objects
        ArrayList<HashMap<String, String>> data =
                new ArrayList<HashMap<String, String>>();
        for (Weather item : fiveDaysWeather) {
            HashMap<String, String> map = new HashMap<String, String>();
            //Date
            map.put("day", item.getDate().toString());
            // Temperature
            float temperature = MesurmentsConvertor.convertTemperature(Float.parseFloat(item.getTemperature()), sp);
            if (sp.getBoolean("temperatureInteger", false)) {
                map.put("temp", String.valueOf(Math.round(temperature)));
                //temperature = Math.round(temperature);
            }
            if (sp.getBoolean("displayDecimalZeroes", false)) {
                map.put("temp", String.valueOf(new DecimalFormat("#.0").format(temperature) + " °" + sp.getString("unit", "C")));
            } else {
                map.put("temp", String.valueOf(new DecimalFormat("#.#").format(temperature) + " °" + sp.getString("unit", "C")));
            }

            data.add(map);
        }

        // create the resource, from, and to variables
        int resource = R.layout.five_days_row;
        String[] from = {"day","temp"};
        int[] to = {R.id.dayOfWeek,R.id.temperature};

        fiveDaysListView.setBackgroundColor(0);
        // create and set the adapter
        adapter =
                new SimpleAdapter(this, data, resource, from, to);
        fiveDaysListView.setAdapter(adapter);

    }

    public WeatherRecyclerAdapter getAdapter(int id) {
        WeatherRecyclerAdapter weatherRecyclerAdapter;

            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermWeather);

        return weatherRecyclerAdapter;
    }


    public void updateLongTermWeatherUI() {


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle fiveDaysForecast = new Bundle();
        fiveDaysForecast.putInt("day", 0);
        RecyclerViewFiveDaysFragment recyclerViewFiveDaysFragment = new RecyclerViewFiveDaysFragment();
        recyclerViewFiveDaysFragment.setArguments(fiveDaysForecast);
        viewPagerAdapter.addFragment(recyclerViewFiveDaysFragment, getString(R.string.five_days_forecast));

//        Bundle bundleTomorrow = new Bundle();
//        bundleTomorrow.putInt("day", 1);
//        RecyclerViewFragment recyclerViewFragmentTomorrow = new RecyclerViewFragment();
//        recyclerViewFragmentTomorrow.setArguments(bundleTomorrow);
//        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, getString(R.string.tomorrow));
//
//        Bundle bundle = new Bundle();
//        bundle.putInt("day", 2);
//        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
//        recyclerViewFragment.setArguments(bundle);
//        viewPagerAdapter.addFragment(recyclerViewFragment, getString(R.string.later));

        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(currentPage, false);
    }

//    private void preloadWeather() {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FiveDaysForecastActivity.this);
//        String lastLongterm = sp.getString("lastLongterm", "");
//        if (!lastLongterm.isEmpty()) {
//            new LongTermWeatherTaskFiveDays(this, this, progressDialog).execute("cachedResponse", lastLongterm);
//        }
//    }


}
