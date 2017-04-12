package com.example.konstantin.weatherie.weathertasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.example.konstantin.weatherie.activities.MainActivity;

/**
 * Created by Konstantin on 19/02/2017.
 */

public class FiveDaysForecastWeatherTask extends WeatherRequestTask {
    public FiveDaysForecastWeatherTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
        super(context, activity, progressDialog);
    }

    @Override
    protected ParseResult parseResponse(String response) {
        return activity.parseFiveDaysJson(response);
    }

    @Override
    protected String getAPIName() {
        //will do request for 5 days including current day
        return "forecast/daily";
    }

}