package com.example.konstantin.weatherie;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Konstantin on 08/02/2017.
 */

class TodayWeatherTask extends WeatherRequestTask {
    public TodayWeatherTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
        super(context, activity, progressDialog);
    }

    @Override
    protected void onPreExecute() {
        loading = 0;
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(TaskOutput output) {
        super.onPostExecute(output);
        // Update widgets
        //AbstractWidgetProvider.updateWidgets(MainActivity.this);
        //DashClockWeatherExtension.updateDashClock(MainActivity.this);
    }

    @Override
    protected ParseResult parseResponse(String response) {
        return activity.parseTodayJson(response);
    }

    @Override
    protected String getAPIName() {
        return "weather";
    }

    @Override
    protected void updateMainUI() {
        activity.updateTodayWeatherUI();
        activity.updateLastUpdateTime();
    }
}

