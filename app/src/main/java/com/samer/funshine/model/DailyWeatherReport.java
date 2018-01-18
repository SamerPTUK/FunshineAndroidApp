package com.samer.funshine.model;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Samer AlShurafa on 1/17/2018.
 */

public class DailyWeatherReport {


    public static final String WEATHER_TYPE_CLOUDS = "Clouds";
    public static final String WEATHER_TYPE_PARTIALLY_CLOUDS = "Partially Cloudy";
    public static final String WEATHER_TYPE_CLEAR = "Clear";
    public static final String WEATHER_TYPE_RAIN = "Rain";
    public static final String WEATHER_TYPE_WIND = "Wind";
    public static final String WEATHER_TYPE_SNOW =  "Snow";
    public static final String WEATHER_TYPE_THUNDER_LIGHTNING =  "Thunder Lightning";


    private String cityName;
    private String countryName;
    private int currentTemp;
    private int minTemp;
    private int maxTemp;
    private String rawDate;
    private String weatherType;

    public DailyWeatherReport() {}


    public DailyWeatherReport(String cityName, String countryName, int currentTemp, int minTemp, int maxTemp,
                              String rawDate, String weatherType) {
        this.cityName = cityName;
        this.countryName = countryName;
        this.currentTemp = currentTemp;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.rawDate = rawDate;
        this.weatherType = weatherType;
    }



    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public String getWeatherType() {
        return weatherType;
    }

    public String getRawDate() {
        return rawDate;
    }


    public String getFormattedDateMonthAndDay() {
        return formattedDateMonthAndDay(getRawDate());
    }

    public String getFormattedDateDay() {
        return formattedDateDay(getRawDate());
    }


    private String formattedDateMonthAndDay(String rawDate) {
        // convert raw date to formatted date Monthe and day (May 1)
        String outputDate = "";
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).parse(rawDate);
            outputDate = new SimpleDateFormat("MMM dd", Locale.US).format(date).toUpperCase(Locale.ROOT);
            //String stringTime = new SimpleDateFormat("HH:mm", Locale.ROOT).format(date);
            //Log.v("ToFormattedDate", "Date: " + outputDate);
        } catch (ParseException e) {
            Log.v("ToFormattedDate", "Exception: " + e.getLocalizedMessage());
        }

        return outputDate;
    }

    private String formattedDateDay(String rawDate) {
        // convert raw date to formatted date day only (Monday)
        String outputDate = "";

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).parse(rawDate);
            outputDate = new SimpleDateFormat("EEEE", Locale.US).format(date).toUpperCase(Locale.ROOT);
            //String stringTime = new SimpleDateFormat("HH:mm", Locale.ROOT).format(date);
            //Log.v("ToFormattedDate", "Date: " + outputDate);
        } catch (ParseException e) {
            Log.v("ToFormattedDate", "Exception: " + e.getLocalizedMessage());
        }

        return outputDate;
    }

}




