package com.example.konstantin.weatherie;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.konstantin.weatherie.adapters.RecyclerViewFiveDaysFragment;
import com.example.konstantin.weatherie.adapters.RecyclerViewFragment;
import com.example.konstantin.weatherie.adapters.ViewPagerAdapter;
import com.example.konstantin.weatherie.adapters.WeatherRecyclerAdapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FiveDaysForecastActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    private ArrayList<Weather> longTermWeather = new ArrayList<>();
    private ArrayList<Weather> fiveDaysWeather = new ArrayList<>();
    private ListView fiveDaysListView;
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd-MM");
    SimpleAdapter adapter;
    ViewPager viewPager;
    TabLayout tabLayout;
    String image;
    //Typeface weatherFont;
    TextView icon;
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
        //weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");

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
            //Day of the week
            map.put("day", item.getWeekday());
            // conditions description
            map.put("description", item.getDescription());
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
            map.put("icon",getImage(item.getIcon()));
            //map.put("icon", item.getIcon());
            image = item.getIcon();

            data.add(map);
        }

        // create the resource, from, and to variables
        int resource = R.layout.five_days_row;
        String[] from = {"day","description","temp", "icon"};
        int[] to = {R.id.dayOfWeek,R.id.description,R.id.temperature, R.id.viewIcon};

        fiveDaysListView.setBackgroundColor(0);
        // create and set the adapter
        adapter =
                new TypefacedSimpleAdapter(this, data, resource,from,to);
//new String[]{"day","description","icon","temp"},
       //new int[] {R.id.dayOfWeek,R.id.description,R.id.viewIcon, R.id.temperature}
        fiveDaysListView.setAdapter(adapter);

    }

    private String getImage (String icon){

        if (icon.contains("10d")) {
            return String.valueOf(R.drawable.medium_rain);
        }
        return String.valueOf(R.drawable.ic_cloud_black_18dp);
    }

    private class TypefacedSimpleAdapter extends SimpleAdapter {
        //private final Typeface mTypeface;

        public TypefacedSimpleAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            //mTypeface = Typeface.createFromAsset(context.getAssets(),"fonts/weather.ttf");
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            LinearLayout ll = (LinearLayout) view; // get the parent layout view
            //TextView icon = (TextView) ll.findViewById(R.id.icon);
            //icon.setTypeface(mTypeface);
            //ImageView viewIcon = (ImageView)ll.findViewById(R.id.viewIcon);

            //viewIcon.setImageResource(R.drawable.medium_rain);
            return ll;
        }
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

        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(currentPage, false);
    }


}
//public class TypefacedSimpleAdapter extends SimpleAdapter {
//    //private final Typeface mTypeface;
//
//    public TypefacedSimpleAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
//        super(context, data, resource, from, to);
//        //mTypeface = Typeface.createFromAsset(context.getAssets(),"fonts/weather.ttf");
//    }
//
//    @Override public View getView(int position, View convertView, ViewGroup parent) {
//        View view = super.getView(position, convertView, parent);
//        LinearLayout ll = (LinearLayout) view; // get the parent layout view
//        //TextView icon = (TextView) ll.findViewById(R.id.icon);
//        //icon.setTypeface(mTypeface);
//        ImageView viewIcon = (ImageView)ll.findViewById(R.id.viewIcon);
//
//        viewIcon.setImageResource(R.drawable.medium_rain);
//
//        return ll;
//    }
//
//}
//
//    public String getImage (String icon){
//
//        if (icon.contains("10d")) {
//            return String.valueOf(R.drawable.medium_rain);
//        }
//        return String.valueOf(R.drawable.ic_cloud_black_18dp);
//    }
//
//map.put("icon",getImage(item.getIcon()));