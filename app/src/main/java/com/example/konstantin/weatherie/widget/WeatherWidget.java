package com.example.konstantin.weatherie.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.example.konstantin.weatherie.MainActivity;
import com.example.konstantin.weatherie.R;
import com.example.konstantin.weatherie.helpers.Updater;
import com.example.konstantin.weatherie.model.Weather;

import java.text.DateFormat;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends WidgetDataProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.weather_widget);
            Intent intent = new Intent(context, Updater.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetButtonRefresh, pendingIntent);

            Intent intent2 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, 0);
            remoteViews.setOnClickPendingIntent(R.id.weatherWidgetId, pendingIntent2);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Weather widgetWeather = new Weather();
            if(!sp.getString("lastToday", "").equals("")) {
                widgetWeather = parseWidgetJson(sp.getString("lastToday", ""), context);
            }
            else {
                try {
                    pendingIntent2.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                return;
            }

            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            remoteViews.setTextViewText(R.id.widgetCity, widgetWeather.getCity());
            remoteViews.setTextViewText(R.id.widgetTemperature, widgetWeather.getTemperature());
            remoteViews.setTextViewText(R.id.widgetLastUpdate, widgetWeather.getLastUpdated());
            remoteViews.setImageViewResource(R.id.widgetIcon, Integer.parseInt(widgetWeather.getIcon()));

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}

