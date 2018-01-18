package com.samer.funshine;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.samer.funshine.model.DailyWeatherReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class WeatherActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
    String URL_COORD = "?lat=%1$f&lon=%2$f";
    String URL_UNITS = "&units=metric";
    final String URL_API_KEY = "&APPID=192c5b88f8b88c6a457d0990fc2fbe25";
    // http://api.openweathermap.org/data/2.5/forecast?lat=35.282749&lon=-120.659622&units=metric&APPID=192c5b88f8b88c6a457d0990fc2fbe25

    final int PERMISSION_LOCATION = 99;

    private GoogleApiClient mGoogleApiClient;

    private ArrayList<DailyWeatherReport> weatherReportList, previousWeatherReportList;

    private WeatherAdapter adapter;


    private ImageView weatherIcon, weatherIconMini;
    private TextView weatherDate, currentTemp, lowTemp, cityCountry, weatherDescription;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherIcon = findViewById(R.id.weatherIcon);
        weatherIconMini = findViewById(R.id.weatherIconMini);
        weatherDate = findViewById(R.id.weatherDate);
        currentTemp = findViewById(R.id.currentTemp);
        lowTemp = findViewById(R.id.lowTemp);
        cityCountry = findViewById(R.id.cityCountry);
        weatherDescription = findViewById(R.id.weatherDescription);
        recyclerView = findViewById(R.id.recycler_list);


        mGoogleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        weatherReportList = new ArrayList<>();
        previousWeatherReportList = new ArrayList<>();


        adapter = new WeatherAdapter(previousWeatherReportList);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);



        Location location = new Location("dummyProvider");
        location.setLatitude(35.282749);
        location.setLongitude(-120.659622);
        downloadWeatherData(location);

    }


    public void downloadWeatherData(Location location) {
        final String URL_ALL = URL_BASE + String.format(URL_COORD, location.getLatitude(), location.getLongitude())
                + URL_UNITS + URL_API_KEY;

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_ALL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("Weather_", "Response: " + response.toString());

                        try {
                            JSONObject city = response.getJSONObject("city");
                            String cityName = city.getString("name");
                            String countryName = city.getString("country");

                            JSONArray array = response.getJSONArray("list");
                            JSONObject obj, objMain, objWeather;
                            JSONArray arrayWeather;
                            double currentTemp = 0, minTemp = 0, maxTemp = 0;
                            String rawDate = "", weatherType = "";

                            DailyWeatherReport dailyWeatherReport;


                            for(int i = 0; i < 5; i++) { // only first 4 objects
                                obj = array.getJSONObject(i);

                                rawDate = obj.getString("dt_txt");

                                objMain = obj.getJSONObject("main");
                                currentTemp = objMain.getDouble("temp");
                                minTemp = objMain.getDouble("temp_min");
                                maxTemp = objMain.getDouble("temp_max");

                                arrayWeather = obj.getJSONArray("weather");
                                objWeather = arrayWeather.getJSONObject(0);
                                weatherType = objWeather.getString("main");

                                dailyWeatherReport = new DailyWeatherReport(cityName, countryName, (int)currentTemp,
                                        (int)minTemp, (int)maxTemp, rawDate, weatherType);

                                weatherReportList.add(dailyWeatherReport);

                                if(i != 0)
                                    previousWeatherReportList.add(dailyWeatherReport);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.v("Weather_", "Error: " + e.getMessage());
                        }

                        updateUI();
                        adapter.notifyDataSetChanged();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("Weather_", "Error: " + error.getLocalizedMessage());
                    }
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }


    private void updateUI() {

        if(weatherReportList.size() > 0) {
            DailyWeatherReport dallyWeatherReport = weatherReportList.get(0);

            weatherDate.setText("Today, " + dallyWeatherReport.getFormattedDateMonthAndDay());
            //String current = dallyWeatherReport.getCurrentTemp() + "°";
            currentTemp.setText(dallyWeatherReport.getCurrentTemp() + "°");
            lowTemp.setText(dallyWeatherReport.getMinTemp() + "°");
            cityCountry.setText(dallyWeatherReport.getCityName() + ", " + dallyWeatherReport.getCountryName());
            weatherDescription.setText(dallyWeatherReport.getWeatherType());

            switch (dallyWeatherReport.getWeatherType()) {
                case DailyWeatherReport.WEATHER_TYPE_CLEAR:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_SNOW:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.snow_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_PARTIALLY_CLOUDS:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.partially_cloudy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.partially_cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_THUNDER_LIGHTNING:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.thunder_lightning));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.thunder_lightning_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_WIND:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
                    break;
            }
        }

    }




    public class WeatherViewHolder extends RecyclerView.ViewHolder {

        private ImageView weatherImg;
        private TextView weatherDescription;
        private TextView weatherDay;
        private TextView tempHigh;
        private TextView tempLow;

        public WeatherViewHolder(View itemView) {
            super(itemView);

            weatherImg = itemView.findViewById(R.id.weatherImg);
            weatherDescription = itemView.findViewById(R.id.weatherDescription);
            weatherDay = itemView.findViewById(R.id.weatherDay);
            tempHigh = itemView.findViewById(R.id.tempHigh);
            tempLow = itemView.findViewById(R.id.tempLow);
        }

        public void updateUI(DailyWeatherReport report) {

            Log.v("KEY", "Weather: " + report.getWeatherType());

            switch (report.getWeatherType()) {
                case DailyWeatherReport.WEATHER_TYPE_CLEAR:
                    weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));;
                    break;
                default:
                    weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
            }

            weatherDescription.setText(report.getWeatherType());
            weatherDay.setText(report.getFormattedDateDay());
            tempHigh.setText(report.getMaxTemp() + "°");
            tempLow.setText(report.getMinTemp() + "°");
        }
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherViewHolder> {

        private ArrayList<DailyWeatherReport> list;

        public WeatherAdapter(ArrayList<DailyWeatherReport> list) {
            this.list = list;
        }

        @Override
        public void onBindViewHolder(WeatherViewHolder holder, int position) {
            DailyWeatherReport report = list.get(position);
            holder.updateUI(report);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public WeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherViewHolder(card);
        }

    }





    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        } else {
            startLocationServices();
        }
    }


    private void startLocationServices() {
        LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationServices();
                } else {
                    Toast.makeText(this, "Can't run the application until allow the permissions", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        downloadWeatherData(location);
    }
}




