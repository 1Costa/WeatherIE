package com.example.konstantin.weatherie.iconsetters;

import com.example.konstantin.weatherie.R;

/**
 * Created by Konstantin on 02/03/2017.
 */

public class LongTermIcons {

    public static String setWeatherIcon(int actualId, int hourOfDay) {
        int id = actualId / 100;
        String icon = "";
            if (hourOfDay >= 7 && hourOfDay < 20) {
                if (actualId == 800) {
                    icon = String.valueOf(R.drawable.weather_sunny);
                }else if(actualId == 801){
                    icon = String.valueOf(R.drawable.weather_few_clouds_day);
                }else if(actualId == 803){
                    icon = String.valueOf(R.drawable.weather_broken_clouds_day);
                }else {
                    switch (id) {
                        case 2:
                            icon = String.valueOf(R.drawable.weather_thunder);
                            break;
                        case 3:
                            icon = String.valueOf(R.drawable.medium_rain);
                            break;
                        case 5:
                            icon = String.valueOf(R.drawable.weather_rainy_day);
                            break;
                        case 6:
                            icon = String.valueOf(R.drawable.weather_snowy_day);
                            break;
                        case 7:
                            icon = String.valueOf(R.drawable.weather_foggy);
                            break;
                        case 8:
                            icon = String.valueOf(R.drawable.weather_cloudy);
                            break;
                    }
                }
            } else {
                if (actualId == 800) {
                    icon = String.valueOf(R.drawable.weather_clear_night);
                }else if(actualId == 801){
                    icon = String.valueOf(R.drawable.weather_few_clouds_night);
                }else {
                    switch (id) {
                        case 2:
                            icon = String.valueOf(R.drawable.weather_thunder);
                            break;
                        case 3:
                            icon = String.valueOf(R.drawable.medium_rain);
                            break;
                        case 5:
                            icon = String.valueOf(R.drawable.weather_rainy_night);
                            break;
                        case 6:
                            icon = String.valueOf(R.drawable.weather_snowy_night);
                            break;
                        case 7:
                            icon = String.valueOf(R.drawable.weather_foggy_night);
                            break;
                        case 8:
                            icon = String.valueOf(R.drawable.weather_cloudy);
                            break;
                    }
                }
            }

        return icon;
    }
}
