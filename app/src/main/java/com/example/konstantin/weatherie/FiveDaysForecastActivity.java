package com.example.konstantin.weatherie;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.konstantin.weatherie.adapters.RecyclerViewFiveDaysFragment;
import com.example.konstantin.weatherie.adapters.ViewPagerAdapter;
import com.example.konstantin.weatherie.adapters.WeatherRecyclerAdapter;
import com.example.konstantin.weatherie.helpers.MesurmentsConvertor;
import com.example.konstantin.weatherie.model.Weather;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FiveDaysForecastActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    String city;
    String country;
    private ArrayList<Weather> longTermWeather = new ArrayList<>();
    private ArrayList<Weather> fiveDaysWeather = new ArrayList<>();
    private ListView fiveDaysListView;
    SimpleAdapter adapter;
    ViewPager viewPager;
    TabLayout tabLayout;
    String image;
    //Typeface weatherFont;
    TextView icon;
    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    int theme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the associated SharedPreferences file with default values
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(theme = getTheme(prefs.getString("theme", "fresh")));
        boolean darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_days_forecast);

        // Load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_five);
        setSupportActionBar(toolbar);
        fiveDaysListView = (ListView)findViewById(R.id.fiveDaysListView);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);



        longTermWeather = (ArrayList<Weather>) getIntent().getSerializableExtra("forecastDetailed");
        fiveDaysWeather = (ArrayList<Weather>) getIntent().getSerializableExtra("forecastFiveDays");
        city = getIntent().getStringExtra("city");
        country = getIntent().getStringExtra("country");
        //weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");

        //progressDialog = new ProgressDialog(FiveDaysForecastActivity.this);
        //preloadWeather();
       // updateLastUpdateTime();

        updateLongTermWeatherUI();
    }
    private int getTheme(String PreferedApplicationTheme) {
        switch (PreferedApplicationTheme) {
            case "dark":
                return R.style.AppTheme_NoActionBar_Dark;
            case "classic":
                return R.style.AppTheme_NoActionBar_Classic;
            case "classicdark":
                return R.style.AppTheme_NoActionBar_Classic_Dark;
            default:
                return R.style.AppTheme_NoActionBar;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView();
        //getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));
        getSupportActionBar().setTitle("Forecast for "+ city + (country.isEmpty() ? "" : ", " + country));
    }

    private void updateListView() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplication());
        // create a List of Map<String, ?> objects
        ArrayList<HashMap<String, String>> data =
                new ArrayList<HashMap<String, String>>();
        for (Weather item : fiveDaysWeather) {
            HashMap<String, String> map = new HashMap<String, String>();
            //Day of the week and short date
            Date fullDate = item.getDate();
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
            SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMMM-dd", Locale.ENGLISH);
            String day = dayFormat.format(fullDate);
            String dateShort = shortDateFormat.format(fullDate);
            // Week Day
            map.put("day", day);
            //Day date short  e.g. March-01
            map.put("dateShort", dateShort);
            //forecast conditions description
            map.put("description", item.getDescription());
            //Wind
            double wind;
            try {
                wind = Double.parseDouble(item.getWind());
            } catch (Exception e) {
                e.printStackTrace();
                wind = 0;
            }
            wind = MesurmentsConvertor.convertWind(wind, sp);

            if (sp.getString("speedUnit", "m/s").equals("bft")) {
                map.put("wind",getString(R.string.wind) + ": " +
                        MesurmentsConvertor.getBeaufortName((int) wind) +
                        (item.isWindDirectionAvailable() ? " " + MainActivity.getWindDirectionString(sp, this, item) : ""));
            } else {
                map.put("wind",getString(R.string.wind) + ": " + new DecimalFormat("#.0").format(wind) + " " +
                        localize(sp, "speedUnit", "m/s") +
                        (item.isWindDirectionAvailable() ? " " + MainActivity.getWindDirectionString(sp, this, item) : ""));
            }
            // Temperature
            float temperature = MesurmentsConvertor.convertTemperature(Float.parseFloat(item.getTemperature()), sp);
            if (sp.getBoolean("temperatureInteger", false)) {
                map.put("temp", String.valueOf(Math.round(temperature)));
                temperature = Math.round(temperature);
            }
            if (sp.getBoolean("displayDecimalZeroes", false)) {
                map.put("temp", String.valueOf(new DecimalFormat("#.0").format(temperature) + " °" + sp.getString("unit", "C")));
            } else {
                map.put("temp", String.valueOf(new DecimalFormat("#.#").format(temperature) + " °" + sp.getString("unit", "C")));
            }
            map.put("icon",item.getIcon());
            //map.put("icon", item.getIcon());

            data.add(map);
        }

        // create the resource, from, and to variables
        int resource = R.layout.five_days_row2;
        String[] from = {"day", "dateShort","wind", "description","temp", "icon"};
        int[] to = {R.id.dayOfWeek,R.id.dateShort, R.id.wind, R.id.description,R.id.temperature, R.id.viewIcon};

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
        viewPagerAdapter.addFragment(recyclerViewFiveDaysFragment, getString(R.string.h3_detailed_forecast));

        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(currentPage, false);
    }

    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return localize(sp, this, preferenceKey, defaultValueKey);
    }

    public static String localize(SharedPreferences sp, Context context, String preferenceKey, String defaultValueKey) {
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
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