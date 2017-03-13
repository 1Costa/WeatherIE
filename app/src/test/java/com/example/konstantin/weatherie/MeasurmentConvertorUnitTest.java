package com.example.konstantin.weatherie;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.konstantin.weatherie.helpers.MesurmentsConvertor;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Konstantin on 07/03/2017.
 */

public class MeasurmentConvertorUnitTest {
    @Test
    public void testConvertKelvinToCelsius_Fail_Data() {

        float actual = MesurmentsConvertor.kelvinToCelsius(100f);
        // expected value is 212
        float expected = 212f;
        // use this method because float is not precise
        assertNotEquals("Conversion from celsius to celsius failed", expected,
                actual, 0.001);
    }

    @Test
    public void testConvertKelvinToCelsius_Success() {

        float actual = MesurmentsConvertor.kelvinToCelsius(290.5f);
        // expected value is 17.35
        float expected = 17.35f;
        // use this method because float is not precise
        assertEquals("Conversion from celsius to celsius success", expected,
                actual, 0.001);
    }
}
