package com.example.konstantin.weatherie.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;

import com.example.konstantin.weatherie.R;
import com.example.konstantin.weatherie.helpers.MesurmentsConvertor;
import com.example.konstantin.weatherie.model.Weather;
import com.example.konstantin.weatherie.weathertasks.ParseResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class GraphsActivity extends AppCompatActivity {

    SharedPreferences sp;

    int theme;
    Context context;

    ArrayList<Weather> weatherGraphsData = new ArrayList<>();

    float minTemp = 100000;
    float maxTemp = 0;
    float minRain = 100000;
    float maxRain = 0;
    boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(theme = getTheme(prefs.getString("theme", "darksky")));
        darkTheme = theme == R.style.AppTheme_NoActionBar_Classic_Dark_DarkSky ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark_ClearSky ||
                theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        Toolbar toolbar = (Toolbar) findViewById(R.id.graphs_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (darkTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
        }
        sp = PreferenceManager.getDefaultSharedPreferences(GraphsActivity.this);
        String lastLongterm = sp.getString("lastLongterm", "");

        if (parseLongTermJson(lastLongterm) == ParseResult.OK) {
            temperatureGraph();
            rainGraph();
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.msg_err_parsing_json, Snackbar.LENGTH_LONG).show();
        }
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Replaced in Manifest to get  home from this activity
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                finish();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//
//        }
//    }

    private void temperatureGraph() {
        LineChartView lineChartView = (LineChartView) findViewById(R.id.graph_temperature);

        // Data
        LineSet dataset = new LineSet();
        for (int i = 0; i < weatherGraphsData.size(); i++) {
            float temperature = MesurmentsConvertor.convertTemperature(Float.parseFloat(weatherGraphsData.get(i).getTemperature()), sp);

            if (temperature < minTemp) {
                minTemp = temperature;
            }

            if (temperature > maxTemp) {
                maxTemp = temperature;
            }

            dataset.addPoint(getDayOfTheWeek(weatherGraphsData.get(i), i), (float) ((Math.ceil(temperature / 2)) * 2));
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor("#028e07"));
        dataset.setThickness(4); // 2 to 5 looks good

        lineChartView.addData(dataset);

        // Grid
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        if (darkTheme) {
            paint.setColor(Color.parseColor("#f7f4f4"));
        }else {
            paint.setColor(Color.parseColor("#2b0808"));
        }
        paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0)); // the best hyphen length is 10
        paint.setStrokeWidth(2);
        lineChartView.setGrid(ChartView.GridType.FULL, paint);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.setAxisBorderValues((int) minTemp - 2, (int) maxTemp + 2);
        lineChartView.setStep(2);
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        if (darkTheme) {
            lineChartView.setLabelsColor(Color.parseColor("#effff7"));
        }else {
            lineChartView.setLabelsColor(Color.parseColor("#002613"));
        }

        lineChartView.show();
    }

    private void rainGraph() {
        LineChartView lineChartView = (LineChartView) findViewById(R.id.graph_rain);

        // Data
        LineSet dataset = new LineSet();
        for (int i = 0; i < weatherGraphsData.size(); i++) {
            float rain = Float.parseFloat(weatherGraphsData.get(i).getRain());

            if (rain < minRain) {
                minRain = rain;
            }

            if (rain > maxRain) {
                maxRain = rain;
            }

            dataset.addPoint(getDayOfTheWeek(weatherGraphsData.get(i), i), rain);
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor("#2196F3"));
        dataset.setThickness(4);

        lineChartView.addData(dataset);

        // Grid
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        if (darkTheme) {
            paint.setColor(Color.parseColor("#f7f4f4"));
        }else {
            paint.setColor(Color.parseColor("#2b0808"));
        }
        paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        paint.setStrokeWidth(2);
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, paint);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.setAxisBorderValues((int) minRain - 1, (int) maxRain + 2);
        lineChartView.setStep(1);
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        if (darkTheme) {
            lineChartView.setLabelsColor(Color.parseColor("#effff7"));
        }else {
            lineChartView.setLabelsColor(Color.parseColor("#002613"));
        }

        lineChartView.show();
    }

    public ParseResult parseLongTermJson(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResult.CITY_NOT_FOUND;
            }

            JSONArray list = reader.getJSONArray("list");
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");
                JSONObject rainObj = listItem.optJSONObject("rain");
                JSONObject snowObj = listItem.optJSONObject("snow");
                if (rainObj != null) {
                    weather.setRain(MainActivity.getRainString(rainObj));
                } else {
                    weather.setRain(MainActivity.getRainString(snowObj));
                }
                // get days of the forecast time stamp and
                // temperature for every three hours
                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));
                weatherGraphsData.add(weather);
            }
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    String previous = "";

    public String getDayOfTheWeek(Weather weather, int i) {
        if ((i + 4) % 4 == 0) {
            //set day of the week in short format //Mon
            SimpleDateFormat resultFormat = new SimpleDateFormat("EE");
            resultFormat.setTimeZone(TimeZone.getDefault());
            String output = resultFormat.format(weather.getDate());
            if (!output.equals(previous)) {
                previous = output;
                return output;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    private int getTheme(String themePref) {
        switch (themePref) {
            case "clearblue":
                return R.style.AppTheme_NoActionBar;
            case "dark":
                return R.style.AppTheme_NoActionBar_Dark;
            case "classic":
                return R.style.AppTheme_NoActionBar_Classic;
            case "classicdark":
                return R.style.AppTheme_NoActionBar_Classic_Dark;
            case "clearsky":
                return R.style.AppTheme_NoActionBar_Classic_Dark_ClearSky;
            case "clover":
                return R.style.AppTheme_NoActionBar_Classic_Dark_DarkSky;
            default:
                return R.style.AppTheme_NoActionBar_Classic_Dark_DarkSky;
        }
    }
}
