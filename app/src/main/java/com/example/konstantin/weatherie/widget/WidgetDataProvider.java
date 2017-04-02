package com.example.konstantin.weatherie.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.konstantin.weatherie.MainActivity;
import com.example.konstantin.weatherie.R;
import com.example.konstantin.weatherie.helpers.MesurmentsConvertor;
import com.example.konstantin.weatherie.iconsetters.TodayIcons;
import com.example.konstantin.weatherie.model.Weather;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

/**
 * Created by Konstantin on 07/03/2017.
 */

public abstract class WidgetDataProvider extends AppWidgetProvider {

    protected Weather parseWidgetJson(String result, Context context) {
        try {
            //MainActivity.initMappings();
            JSONObject reader = new JSONObject(result);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            Weather weatherWidget = new Weather();
            weatherWidget.setCity(reader.getString("name"));
            // Temperature
            float temperature = MesurmentsConvertor.convertTemperature(Float.parseFloat(reader.optJSONObject("main").getString("temp").toString()), sp);
            if (sp.getBoolean("temperatureInteger", false)) {
                temperature = Math.round(temperature);
            }
            weatherWidget.setTemperature(Math.round(temperature) + "°" + localize(sp, context, "unit", "C"));
            final String idString = reader.getJSONArray("weather").getJSONObject(0).getString("id");
            weatherWidget.setId(idString);
            weatherWidget.setIcon(TodayIcons.setWeatherIcon(Integer.parseInt(idString), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
            //last time updated
            long lastUpdateTimeInMillis = sp.getLong("lastUpdate", -1);
            String lastUpdate;
            if (lastUpdateTimeInMillis < 0) {
                // No time
                lastUpdate = "";
            } else {
                lastUpdate = context.getString(R.string.last_update_widget, MainActivity.formatTimeWithDayIfNotToday(context, lastUpdateTimeInMillis));
            }
            weatherWidget.setLastUpdated(lastUpdate);

            return weatherWidget;
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return new Weather();
        }
    }

    protected String localize(SharedPreferences sp, Context context, String preferenceKey,
                              String defaultValueKey) {
        return MainActivity.localize(sp, context, preferenceKey, defaultValueKey);
    }

    public static void updateWidgets(Context context) {
        updateWidgets(context, WeatherWidget.class);

    }

    private static void updateWidgets(Context context, Class widgetClass) {
        Intent intent = new Intent(context.getApplicationContext(), widgetClass)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(), widgetClass));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.getApplicationContext().sendBroadcast(intent);
    }
}
