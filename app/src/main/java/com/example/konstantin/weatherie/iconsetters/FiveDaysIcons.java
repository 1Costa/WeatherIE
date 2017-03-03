package com.example.konstantin.weatherie.iconsetters;

import com.example.konstantin.weatherie.R;

/**
 * Created by Konstantin on 02/03/2017.
 */

public class FiveDaysIcons{

    public static String setWeatherIcon(int actualId){
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
                icon = String.valueOf(R.drawable.weather_sunny);
        }else if(actualId == 801){
            icon = String.valueOf(R.drawable.weather_few_clouds_day);
        }else if(actualId == 803){
            icon = String.valueOf(R.drawable.weather_few_clouds_day);
        }else {
            switch (id) {
                case 2:
                    icon = String.valueOf(R.drawable.weather_thunder);
                    //icon = this.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = String.valueOf(R.drawable.medium_rain);
                    //icon = this.getString(R.string.weather_drizzle);
                    break;
                case 5:
                    icon = String.valueOf(R.drawable.weather_rainy_day);
                    //icon = this.getString(R.string.weather_rainy);
                    break;
                case 6:
                    icon = String.valueOf(R.drawable.weather_snowy_day);
                    //icon = this.getString(R.string.weather_snowy);
                    break;
                case 7:
                    icon = String.valueOf(R.drawable.weather_foggy);
                    //icon = this.getString(R.string.weather_foggy);
                    break;
                case 8:
                    //icon = this.getString(R.string.weather_cloudy);
                    icon = String.valueOf(R.drawable.weather_cloudy);
                    break;


            }
        }
        return icon;
    }
}
