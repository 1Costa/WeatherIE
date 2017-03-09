package com.example.konstantin.weatherie;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Konstantin on 07/03/2017.
 */

public class MeasurmentConverterUnitTest {

    @Test
    public void testConvertFahrenheitToCelsius() {
        float actual = MeasurmentsConvertor.convertCelsiusToFahrenheit(100);
        // expected value is 212
        float expected = 212;
        // use this method because float is not precise
        assertEquals("Conversion from celsius to fahrenheit failed", expected,
                actual, 0.001);
    }
}
