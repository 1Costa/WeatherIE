package com.example.konstantin.weatherie.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.konstantin.weatherie.activities.MainActivity;
import com.example.konstantin.weatherie.helpers.MesurmentsConvertor;
import com.example.konstantin.weatherie.R;
import com.example.konstantin.weatherie.model.Weather;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Konstantin on 19/02/2017.
 */

public class WeatherRecyclerAdapter extends RecyclerView.Adapter<WeatherViewHolder> {
    private List<Weather> itemList;
    private Context context;

    public WeatherRecyclerAdapter(Context context, List<Weather> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public WeatherViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_view_row2, null);

        WeatherViewHolder viewHolder = new WeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WeatherViewHolder customViewHolder, int i) {
        Weather weatherItem = itemList.get(i);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        // Temperature
        float temperature = MesurmentsConvertor.convertTemperature(Float.parseFloat(weatherItem.getTemperature()), sp);
        if (sp.getBoolean("temperatureInteger", false)) {
            temperature = Math.round(temperature);
        }

        // Rain
        double rain = Double.parseDouble(weatherItem.getRain());
        String rainString = MesurmentsConvertor.getRainString(rain, sp);

        // Wind
        double wind;
        try {
            wind = Double.parseDouble(weatherItem.getWind());
        } catch (Exception e) {
            e.printStackTrace();
            wind = 0;
        }
        wind = MesurmentsConvertor.convertWind(wind, sp);

        // Pressure
        double pressure = MesurmentsConvertor.convertPressure((float) Double.parseDouble(weatherItem.getPressure()), sp);

        TimeZone tz = TimeZone.getDefault();
        String defaultDateFormat = context.getResources().getStringArray(R.array.dateFormatsValues)[0];
        String dateFormat = sp.getString("dateFormat", defaultDateFormat);
        if ("custom".equals(dateFormat)) {
            dateFormat = sp.getString("dateFormatCustom", defaultDateFormat);
        }
        String dateString;
        try {
            SimpleDateFormat resultFormat = new SimpleDateFormat(dateFormat);
            resultFormat.setTimeZone(tz);
            dateString = resultFormat.format(weatherItem.getDate());
        } catch (IllegalArgumentException e) {
            dateString = context.getResources().getString(R.string.error_dateFormat);
        }

        if (sp.getBoolean("differentiateDaysByTint", false)) {
            Date now = new Date();
            int color;
            if (weatherItem.getNumDaysFrom(now) > 0) {
                //TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.colorTintedBackground, R.attr.colorBackground});
                if (weatherItem.getNumDaysFrom(now) % 2 == 1) {
                    color = ContextCompat.getColor(context, R.color.colorTintedBackground);
                } else {
                    color = ContextCompat.getColor(context, R.color.colorBackground);
                }
                //ta.recycle();
                customViewHolder.itemView.setBackgroundColor(color);
            }
        }

        customViewHolder.itemDate.setText(dateString);
        if (sp.getBoolean("displayDecimalZeroes", false)) {
            customViewHolder.itemTemperature.setText(new DecimalFormat("#.0").format(temperature) + " °" + sp.getString("unit", "C"));
        } else {
            customViewHolder.itemTemperature.setText(new DecimalFormat("#.#").format(temperature) + " °" + sp.getString("unit", "C"));
        }
        customViewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() +
                weatherItem.getDescription().substring(1) + rainString);

        customViewHolder.imageViewIcon.setImageResource(Integer.parseInt(weatherItem.getIcon()));

        if (sp.getString("speedUnit", "m/s").equals("bft")) {
            customViewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " +
                    MesurmentsConvertor.getBeaufortName((int) wind) + " " + MainActivity.getWindDirectionString(sp, context, weatherItem));
        } else {
            customViewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " + new DecimalFormat("#.0").format(wind) + " " +
                    MainActivity.localize(sp, context, "speedUnit", "m/s")
                    + " " + MainActivity.getWindDirectionString(sp, context, weatherItem));
        }
   }

    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }
}
