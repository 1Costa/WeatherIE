package com.example.konstantin.weatherie;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Konstantin on 21/02/2017.
 */

public class DateUtils {

    private static final DateFormat SERVER_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

    private static final DateFormat SERVER_FORMAT_1 =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static final DateFormat OUTPUT_DATE_FORMAT =
            new SimpleDateFormat("EEEE, MMMM dd", Locale.ENGLISH);

    public static final DateFormat OUTPUT_DAY_FORMAT =
            new SimpleDateFormat("EEEE", Locale.ENGLISH);

    // 2013-10-11T18:08:04
// 2013-10-11
    public static Date dateStrToDate(String dateStr){
        Date date = new Date();

        try {
            date = OUTPUT_DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            try {
                date = OUTPUT_DAY_FORMAT.parse(dateStr);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }

        return date;
    }
}

