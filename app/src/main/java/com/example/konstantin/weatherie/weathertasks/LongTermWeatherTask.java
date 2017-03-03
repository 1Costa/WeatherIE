package com.example.konstantin.weatherie.weathertasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.example.konstantin.weatherie.MainActivity;
import com.example.konstantin.weatherie.weathertasks.ParseResult;
import com.example.konstantin.weatherie.weathertasks.WeatherRequestTask;

/**
 * Created by Konstantin on 17/02/2017.
 */

public class LongTermWeatherTask extends WeatherRequestTask {
    public LongTermWeatherTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
        super(context, activity, progressDialog);
    }

    @Override
    protected ParseResult parseResponse(String response) {
        return activity.parseLongTermJson(response);
    }

    @Override
    protected String getAPIName() {
        //will do request for 5 day 3 hourly forecast including current day
        return "forecast";
    }

    @Override
    protected void updateMainUI() {
        activity.updateLongTermWeatherUI();
    }
}