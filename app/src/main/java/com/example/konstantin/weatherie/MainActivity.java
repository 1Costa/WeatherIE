package com.example.konstantin.weatherie;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.konstantin.weatherie.WorldMap.WorldMapActivity;
import com.example.konstantin.weatherie.adapters.PlacesAutoCompleteAdapter;
import com.example.konstantin.weatherie.adapters.RecyclerViewFragment;
import com.example.konstantin.weatherie.adapters.ViewPagerAdapter;
import com.example.konstantin.weatherie.adapters.WeatherRecyclerAdapter;
import com.example.konstantin.weatherie.helpers.MesurmentsConvertor;
import com.example.konstantin.weatherie.helpers.NetworkConnectionCheck;
import com.example.konstantin.weatherie.helpers.Updater;
import com.example.konstantin.weatherie.iconsetters.FiveDaysIcons;
import com.example.konstantin.weatherie.iconsetters.LongTermIcons;
import com.example.konstantin.weatherie.iconsetters.TodayIcons;
import com.example.konstantin.weatherie.model.DefaultCity;
import com.example.konstantin.weatherie.model.Weather;
import com.example.konstantin.weatherie.weathertasks.FiveDaysForecastWeatherTask;
import com.example.konstantin.weatherie.weathertasks.LongTermWeatherTask;
import com.example.konstantin.weatherie.weathertasks.ParseResult;
import com.example.konstantin.weatherie.weathertasks.TaskOutput;
import com.example.konstantin.weatherie.weathertasks.TodayWeatherTask;
import com.example.konstantin.weatherie.weathertasks.WeatherRequestTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    final NetworkConnectionCheck net = new NetworkConnectionCheck(MainActivity.this) ; //check if a phone has internet access
    protected static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    // Time in milliseconds; only reload weather if last update is longer ago than this value
    private static final int NO_UPDATE_REQUIRED_THRESHOLD = 300000;

    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    private static boolean mappingsInitialised = false;

    Typeface weatherFont;
    Weather todayWeather = new Weather();
    private List<Weather> longTermTodayWeather = new ArrayList<>();
    private List<Weather> longTermWeather = new ArrayList<>();
    private List<Weather> fiveDaysForecastWeather = new ArrayList<>();
    private List<Weather> longTermTomorrowWeather = new ArrayList<>();

    Button forecast;
    TextView todayCity;
    ImageView weatherIcon;
    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayWindDirection;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    TextView lastUpdate;
    TextView todayIcon;
    ViewPager viewPager;
    TabLayout tabLayout;
    View appView;

    public String recentCity = "";

    LocationManager locationManager;
    ProgressDialog progressDialog;

    int theme;
    boolean destroyed = false;
    //public static final String  DEFAULT_CITY = "Limerick";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the associated SharedPreferences file with default values
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(theme = getTheme(prefs.getString("theme", "fresh")));
        boolean darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Initialize Forecast view Button
        forecast = (Button)findViewById(R.id.forecast);
        forecast.setOnClickListener(this);
        // Initialize labels for weather
        lastUpdate = (TextView) findViewById(R.id.lastUpdate);
        todayCity = (TextView) findViewById(R.id.todayCity);
        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);
        todayTemperature = (TextView) findViewById(R.id.todayTemperature);
        todayDescription = (TextView) findViewById(R.id.todayDescription);
        todayWind = (TextView) findViewById(R.id.todayWind);
        todayWindDirection =(TextView)findViewById(R.id.todayWindDirection);
        todayPressure = (TextView) findViewById(R.id.todayPressure);
        todayHumidity = (TextView) findViewById(R.id.todayHumidity);
        todayIcon = (TextView) findViewById(R.id.todayIcon);
        todaySunrise = (TextView) findViewById(R.id.todaySunrise);
        todaySunset = (TextView) findViewById(R.id.todaySunset);
        appView = findViewById(R.id.activity_main);
// Initialize viewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");
        //todayIcon.setTypeface(weatherFont);




        progressDialog = new ProgressDialog(MainActivity.this);

        if (darkTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
        }
        preloadWeather();
        updateLastUpdateTime();
        // Set autoupdater
        Updater.setRecurringAlarm(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.forecast:
                ArrayList<Weather> longTermFiveDays = new ArrayList<Weather>(longTermWeather);
                ArrayList<Weather> fiveDaysForecast = new ArrayList<Weather>(fiveDaysForecastWeather);
                Intent forecastActivity = new Intent(this, FiveDaysForecastActivity.class);
                forecastActivity.putExtra("forecastDetailed",longTermFiveDays);
                forecastActivity.putExtra("forecastFiveDays",fiveDaysForecast);
                forecastActivity.putExtra("city", todayWeather.getCity());
                forecastActivity.putExtra("country",todayWeather.getCountry());
                startActivity(forecastActivity);
                break;
        }
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

    //    private int getTheme(String PreferedApplicationTheme) {
//        switch (PreferedApplicationTheme) {
//            case "dark":
//                return R.style.AppTheme_Dark;
//            case "classic":
//                return R.style.AppTheme_Classic;
//            case "classicdark":
//                return R.style.AppTheme_Classic_Dark;
//            default:
//                return R.style.AppTheme;
//        }
//    }

    private boolean shouldUpdate() {
        long lastUpdate = PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1);
        boolean cityChanged = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("cityChanged", false);
        // Update if never checked or last update is longer ago than specified threshold
        return cityChanged || lastUpdate < 0 || (Calendar.getInstance().getTimeInMillis() - lastUpdate) > NO_UPDATE_REQUIRED_THRESHOLD;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if (net.isNetworkAvailable()) {
                getTodayWeather();
                getFiveDaysWeather();
                //getLongTermWeather();
            } else {
                Snackbar.make(appView, getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
            }
            return true;
        }

//        if (id == R.id.action_graphs) {
//            //Intent intent = new Intent(MainActivity.this, GraphActivity.class);
//            //startActivity(intent);
//        }
        if (id == R.id.action_search) {
            searchCities();
            return true;
        }
        if (id == R.id.action_location) {
            getCityByLocation();
            return true;
        }
        if (id == R.id.action_settings) {
            // Create SettingsActivity to manipulate with application
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_world_map) {
            // Create WorldMapActivity to provide layered weather map
            Intent intent = new Intent(MainActivity.this, WorldMapActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_about) {
            // Create dialog or Activity with description of the project
            //aboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    String selectedCity = "";
    private void searchCities() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(this.getString(R.string.search_title));
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View insideAlert = inflater.inflate(R.layout.locations_input, null);

        alert.setView(insideAlert);

        AutoCompleteTextView autocompleteView = (AutoCompleteTextView) insideAlert.findViewById(R.id.locations_autocomplete);
        //autocompleteView.setAdapter(new MainActivity.PlacesAutoCompleteAdapter(this, R.layout.locations_input_list));
        autocompleteView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.locations_input_list));
        autocompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                selectedCity = (String) parent.getItemAtPosition(position);
                if (selectedCity.isEmpty()) {
                   //saveLocation(selectedCity);
                    Toast.makeText(getBaseContext(), "Type In The City Name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //String result = input.getText().toString();
                if (!selectedCity.isEmpty()) {
                    saveLocation(selectedCity);
                }
            }
        });
        alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        alert.show();
    }

    private void saveLocation(String result) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        // Checks if default Limerick city was changed to other location
        recentCity = preferences.getString("city", DefaultCity.DEFAULT_CITY);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", result);
        editor.commit();

        if (!recentCity.equals(result)) {
            // New location, update weather
            getTodayWeather();
            getLongTermWeather();
            getFiveDaysWeather();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        boolean unitsChanged = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("unitsChanged",false);
        if (getTheme(PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "fresh")) != theme) {
            // Restart activity to apply theme
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
        } else if (shouldUpdate() && net.isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
            getFiveDaysWeather();
        }else if(unitsChanged){
            updateTodayWeatherUI();
            updateLongTermWeatherUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;

        if (locationManager != null) {
            try {
                locationManager.removeUpdates(MainActivity.this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void getTodayWeather() {
        new TodayWeatherTask(this, this, progressDialog).execute();
    }

    private void getFiveDaysWeather() {
        new FiveDaysForecastWeatherTask(this, this, progressDialog).execute();
    }

    private void getLongTermWeather() {
        new LongTermWeatherTask(this, this, progressDialog).execute();
    }

    void getCityByLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explanation not needed, since user requests this himself

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.getting_location));
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        locationManager.removeUpdates(MainActivity.this);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            });
            progressDialog.show();
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        } else {
            showLocationSettingsDialog();
        }
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.location_settings);
        alertDialog.setMessage(R.string.location_settings_message);
        alertDialog.setPositiveButton(R.string.location_settings_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        progressDialog.hide();
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("LocationManager", "Error while trying to stop listening for location updates. This is probably a permissions issue", e);
        }
        Log.i("LOCATION (" + location.getProvider().toUpperCase() + ")", location.getLatitude() + ", " + location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        new ProvideCityNameTask(this, this, progressDialog).execute("coords", Double.toString(latitude), Double.toString(longitude));
    }

    class ProvideCityNameTask extends WeatherRequestTask {

        public ProvideCityNameTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() { /*Do Nothing*/ }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        public ParseResult parseResponse(String response) {
            Log.i("RESULT", response.toString());
            try {
                JSONObject reader = new JSONObject(response);

                final String code = reader.optString("cod");
                if ("404".equals(code)) {
                    Log.e("Geolocation", "No city found");
                    return ParseResult.CITY_NOT_FOUND;
                }

                String city = reader.getString("name");
                String country = "";
                JSONObject countryObj = reader.optJSONObject("sys");
                if (countryObj != null) {
                    country = ", " + countryObj.getString("country");
                }

                saveLocation(city + country);

            } catch (JSONException e) {
                Log.e("JSONException Data", response);
                e.printStackTrace();
                return ParseResult.JSON_EXCEPTION;
            }

            return ParseResult.OK;
        }

        @Override
        public void onPostExecute(TaskOutput output) {
            /* Handle possible errors only */
            handleTaskOutput(output);
        }
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

    public static long saveLastUpdateTime(SharedPreferences sp) {
        Calendar now = Calendar.getInstance();
        sp.edit().putLong("lastUpdate", now.getTimeInMillis()).apply();
        return now.getTimeInMillis();
    }

    public void updateLastUpdateTime() {
        updateLastUpdateTime(
                PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
        );
    }

    private void updateLastUpdateTime(long timeInMillis) {
        if (timeInMillis < 0) {
            // No time
            lastUpdate.setText("");
        } else {
            lastUpdate.setText(getString(R.string.last_update, formatTimeWithDayIfNotToday(this, timeInMillis)));
        }
    }

    public static String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar lastCheckedCal = new GregorianCalendar();
        lastCheckedCal.setTimeInMillis(timeInMillis);
        Date lastCheckedDate = new Date(timeInMillis);
        String timeFormat = android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate);
        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
            // Same day, only show time
            return timeFormat;
        } else {
            return android.text.format.DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
        }
    }

    public void updateTodayWeatherUI() {
        try {
            if (todayWeather.getCountry().isEmpty()) {
                preloadWeather();
                return;
            }
        } catch (Exception e) {
            preloadWeather();
            return;
        }
        String city = todayWeather.getCity();
        String country = todayWeather.getCountry();
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        //getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));
        todayCity.setText(city + (country.isEmpty() ? "" : ", " + country));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        // Temperature
        float temperature = MesurmentsConvertor.convertTemperature(Float.parseFloat(todayWeather.getTemperature()), sp);
        if (sp.getBoolean("temperatureInteger", false)) {
            temperature = Math.round(temperature);
        }

        // Rain
        double rain = Double.parseDouble(todayWeather.getRain());
        String rainString = MesurmentsConvertor.getRainString(rain, sp);

        // Wind
        double wind;
        try {
            wind = Double.parseDouble(todayWeather.getWind());
        } catch (Exception e) {
            e.printStackTrace();
            wind = 0;
        }
        wind = MesurmentsConvertor.convertWind(wind, sp);

        // Pressure
        double pressure = MesurmentsConvertor.convertPressure((float) Double.parseDouble(todayWeather.getPressure()), sp);

        todayTemperature.setText(new DecimalFormat("#.#").format(temperature) + " °" + sp.getString("unit", "C"));
        // make first character of description Upper Case
       todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() +
                todayWeather.getDescription().substring(1) + rainString);
        if (sp.getString("speedUnit", "m/s").equals("bft")) {
            todayWind.setText(getString(R.string.wind) + ": " +
                    MesurmentsConvertor.getBeaufortName((int) wind) +
                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
        } else {
            //wind direction
//            String direction = (getString(R.string.wind) + ": " + new DecimalFormat("#.0").format(wind) + " " +
//                    localize(sp, "speedUnit", "m/s") +
//                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
//            SpannableString d = new SpannableString(direction);
//            d.setSpan(new RelativeSizeSpan(2f), d.length()-2,d.length(), 0);
            todayWind.setText(getString(R.string.wind) + ": " + new DecimalFormat("#.0").format(wind) + " " +
                    localize(sp, "speedUnit", "m/s"));
                    todayWindDirection.setText(todayWeather.isWindDirectionAvailable() ? " "
                            + getWindDirectionString(sp, this, todayWeather) : "");
        }
        todayPressure.setText(getString(R.string.pressure) + ": " + new DecimalFormat("#.0").format(pressure) + " " +
                localize(sp, "pressureUnit", "hPa"));
        todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
        todaySunrise.setText(getString(R.string.sunrise) + ": " + timeFormat.format(todayWeather.getSunrise()));
        todaySunset.setText(getString(R.string.sunset) + ": " + timeFormat.format(todayWeather.getSunset()));
        weatherIcon.setImageResource(Integer.parseInt(todayWeather.getIcon()));
        //todayIcon.setText(todayWeather.getIcon());
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

    public static String getWindDirectionString(SharedPreferences sp, Context context, Weather weather) {
        try {
            if (Double.parseDouble(weather.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                if ("arrow".equals(pref)) {
                    return weather.getWindDirection(8).getArrow(context);
                } else if ("abbr".equals(pref)) {
                    return weather.getWindDirection().getLocalizedString(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private void preloadWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String lastToday = sp.getString("lastToday", "");
        if (!lastToday.isEmpty()) {
            new TodayWeatherTask(this, this, progressDialog).execute("cachedResponse", lastToday);
        }
        String lastFiveDays = sp.getString("lastFiveDays", "");
        if (!lastFiveDays.isEmpty()) {
            new FiveDaysForecastWeatherTask(this, this, progressDialog).execute("cachedResponse", lastFiveDays);
        }
        String lastLongterm = sp.getString("lastLongterm", "");
        if (!lastLongterm.isEmpty()) {
            new LongTermWeatherTask(this, this, progressDialog).execute("cachedResponse", lastLongterm);
        }
    }

    public static String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }

    public ParseResult parseTodayJson(String result) {
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResult.CITY_NOT_FOUND;
            }

            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
                todayWeather.setSunrise(countryObj.getString("sunrise"));
                todayWeather.setSunset(countryObj.getString("sunset"));
            }
            todayWeather.setCity(city);
            todayWeather.setCountry(country);
            JSONObject coordinates = reader.getJSONObject("coord");
            if (coordinates != null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                sp.edit().putFloat("latitude", (float) coordinates.getDouble("lon")).putFloat("longitude", (float) coordinates.getDouble("lat")).commit();
            }

            JSONObject main = reader.getJSONObject("main");

            todayWeather.setTemperature(main.getString("temp"));
            todayWeather.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            JSONObject windObj = reader.getJSONObject("wind");
            todayWeather.setWind(windObj.getString("speed"));
            if (windObj.has("deg")) {
                todayWeather.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                Log.e("parseTodayJson", "No wind speed");
                todayWeather.setWindDirectionDegree(null);
            }
            todayWeather.setPressure(main.getString("pressure"));
            todayWeather.setHumidity(main.getString("humidity"));

            JSONObject rainObj = reader.optJSONObject("rain");
            String rain;
            if (rainObj != null) {
                rain = getRainString(rainObj);
            } else {
                JSONObject snowObj = reader.optJSONObject("snow");
                if (snowObj != null) {
                    rain = getRainString(snowObj);
                } else {
                    rain = "0";
                }
            }
            todayWeather.setRain(rain);

            final String idString = reader.getJSONArray("weather").getJSONObject(0).getString("id");
            todayWeather.setId(idString);
            todayWeather.setIcon(TodayIcons.setWeatherIcon(Integer.parseInt(idString), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastToday", result);
            editor.commit();

        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    public ParseResult parseFiveDaysJson(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                if (fiveDaysForecastWeather == null) {
                    fiveDaysForecastWeather = new ArrayList<>();
                }
                return ParseResult.CITY_NOT_FOUND;
            }

            fiveDaysForecastWeather = new ArrayList<>();

            JSONArray list = reader.getJSONArray("list");
            for (i = 1; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                //JSONObject main = listItem.getJSONObject("main");


                Date currentDate = new Date(Long.parseLong(listItem.getString("dt"))*1000);
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, dd", Locale.ENGLISH);
                String weekDay = dayFormat.format(currentDate);
                //weather.setWeekday(weekDay);
                //SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM", Locale.ENGLISH);
                //String dayDate = inputFormat.format(currentDate);
                weather.setDate(listItem.getString("dt"));

                JSONObject temp = listItem.getJSONObject("temp");
                weather.setTemperature(temp.getString("day"));
                if (listItem.has("deg")) {
                    weather.setWind(listItem.getString("speed"));
                    weather.setWindDirectionDegree(listItem.getDouble("deg"));
                }
                else {
                    Log.e("parseFiveDaysJson", "No wind speed");
                    weather.setWindDirectionDegree(null);
                }
                JSONObject conditions = listItem.optJSONArray("weather").getJSONObject(0);
                weather.setDescription(conditions.getString("description"));
                weather.setId(conditions.getString("id"));


                //weather.setIcon(conditions.getString("icon"));

                final String dateMsString = listItem.getString("dt") + "000";
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(dateMsString));
                //weather.setIcon(setWeatherIcon(Integer.parseInt(conditions.getString("id")), cal.get(Calendar.DAY_OF_WEEK)));
                weather.setIcon(FiveDaysIcons.setWeatherIcon(Integer.parseInt(conditions.getString("id"))));

                    fiveDaysForecastWeather.add(weather);

            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastFiveDays", result);
            editor.commit();
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    // convert milliseconds into the day of the week string
    public static String dayStringFormat(long msecs) {
        GregorianCalendar cal = new GregorianCalendar();

        cal.setTime(new Date(msecs));

        int dow = cal.get(Calendar.DAY_OF_WEEK);

        switch (dow) {
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
        }

        return "Unknown";
    }



    public ParseResult parseLongTermJson(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                if (longTermWeather == null) {
                    longTermWeather = new ArrayList<>();
                    longTermTodayWeather = new ArrayList<>();
                    //longTermTomorrowWeather = new ArrayList<>();
                }
                return ParseResult.CITY_NOT_FOUND;
            }

            longTermWeather = new ArrayList<>();
            longTermTodayWeather = new ArrayList<>();

            JSONArray list = reader.getJSONArray("list");
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");

                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));
                weather.setDescription(listItem.optJSONArray("weather").getJSONObject(0).getString("description"));
                JSONObject windObj = listItem.optJSONObject("wind");
                if (windObj != null) {
                    weather.setWind(windObj.getString("speed"));
                    weather.setWindDirectionDegree(windObj.getDouble("deg"));
                }
                weather.setPressure(main.getString("pressure"));
                weather.setHumidity(main.getString("humidity"));

                JSONObject rainObj = listItem.optJSONObject("rain");
                String rain = "";
                if (rainObj != null) {
                    rain = getRainString(rainObj);
                } else {
                    JSONObject snowObj = listItem.optJSONObject("snow");
                    if (snowObj != null) {
                        rain = getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                weather.setRain(rain);

                final String idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id");
                weather.setId(idString);

                final String dateMsString = listItem.getString("dt") + "000";
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(dateMsString));
                weather.setIcon(LongTermIcons.setWeatherIcon(Integer.parseInt(idString), cal.get(Calendar.HOUR_OF_DAY)));



                Calendar today = Calendar.getInstance();
                //today.set(Calendar.DAY_OF_YEAR, 1);
                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    longTermTodayWeather.add(weather);

                } else {
                    longTermWeather.add(weather);
                }
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastLongterm", result);
            editor.commit();
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    public WeatherRecyclerAdapter getAdapter(int id) {
        WeatherRecyclerAdapter weatherRecyclerAdapter;
        if (id == 0) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTodayWeather);
        } else if (id == 1) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTomorrowWeather);
        } else {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermWeather);
        }
        return weatherRecyclerAdapter;
    }


    public void updateLongTermWeatherUI() {
        if (destroyed) {
            return;
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 0);
        RecyclerViewFragment recyclerViewFragmentToday = new RecyclerViewFragment();
        recyclerViewFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, getString(R.string.today_3h));

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

        if (currentPage == 0 && longTermTodayWeather.isEmpty()) {
            currentPage = 1;
        }
        viewPager.setCurrentItem(currentPage, false);
    }

//    public enum ParseResult {
//        OK, JSON_EXCEPTION, CITY_NOT_FOUND
//    }
//
//    public enum TaskResult {
//        SUCCESS, BAD_RESPONSE, IO_EXCEPTION, TOO_MANY_REQUESTS;
//    }
//
//    public class TaskOutput {
//        // Indicates result of parsing server response
//        ParseResult parseResult;
//        // Indicates result of background task
//        TaskResult taskResult;
//    }
}
