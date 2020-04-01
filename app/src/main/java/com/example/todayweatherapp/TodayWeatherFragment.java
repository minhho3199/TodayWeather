package com.example.todayweatherapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todayweatherapp.Common.Common;
import com.example.todayweatherapp.Model.WeatherForecastResult;
import com.example.todayweatherapp.Model.WeatherResult;
import com.example.todayweatherapp.Retrofit.IOpenWeatherMap;
import com.example.todayweatherapp.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayWeatherFragment extends Fragment {

    static TodayWeatherFragment instance;
    ImageView img_weather;
    TextView txt_city_name, txt_humidity, txt_sunrise,
            txt_sunset, txt_pressure, txt_temperature,
            txt_date_time, txt_wind, txt_geo_coord, txt_description, txt_feels_like;
    LinearLayout weather_panel;
    LinearLayout details;
    ProgressBar loading;
    boolean current = true;
    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;


    public static TodayWeatherFragment getInstance() {
        if (instance == null) {
            instance = new TodayWeatherFragment();
        }
        return instance;
    }

    public TodayWeatherFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        //Retrofit generates an implementation of the OpenWeatherMap interface
        mService = retrofit.create(IOpenWeatherMap.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_today_weather, container, false);
        img_weather = itemView.findViewById(R.id.img_weather);
        txt_city_name = itemView.findViewById(R.id.txt_city_name);
        txt_humidity = itemView.findViewById(R.id.txt_humidity);
        txt_sunrise = itemView.findViewById(R.id.txt_sunrise);
        txt_sunset = itemView.findViewById(R.id.txt_sunset);
        txt_pressure = itemView.findViewById(R.id.txt_pressure);
        txt_temperature = itemView.findViewById(R.id.txt_temperature);
        txt_date_time = itemView.findViewById(R.id.txt_date_time);
        txt_wind = itemView.findViewById(R.id.txt_wind);
        txt_geo_coord = itemView.findViewById(R.id.txt_geo_coord);
        txt_description = itemView.findViewById(R.id.txt_description);
        txt_feels_like = itemView.findViewById(R.id.txt_feels_like);
        weather_panel = itemView.findViewById(R.id.weather_panel);
        details = itemView.findViewById(R.id.details);
        loading = itemView.findViewById(R.id.loading);
        getWeatherInformationByLatLong();
        return itemView;
    }


    public void getWeatherInformationByCity(String cityName) {
        compositeDisposable.add(
                mService.getWeatherByCityName(cityName, Common.APP_ID,"metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {
                        //Load image
                        displayWeatherInfo(weatherResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    public void getWeatherInformationByLatLong() {
        //This adds the whole Observable to a CompositeDisposable object so that the Observers can be removed from
        //the Observable when it is no longer needed
        compositeDisposable.add(
                mService.getWeatherByLatLong(
                String.valueOf(Common.CURRENT_LOCATION.getLatitude()),
                String.valueOf(Common.CURRENT_LOCATION.getLongitude()),
                Common.APP_ID,
                "metric")
                //This method tells the observer what thread to do the work on, in this case is the Schedulers.io thread
                .subscribeOn(Schedulers.io())
                //This method tells the observer what thread to display the results on, in this case is the main thread
                .observeOn(AndroidSchedulers.mainThread())
                //Consumer is an alternative to Observers that accepts a single value, which in this case is weatherResult
                //The subscribe method returns a Disposable object so that it can be added to CompositeDisposable
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {
                        displayWeatherInfo(weatherResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void displayWeatherInfo(WeatherResult weatherResult) {
        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(weatherResult.getWeather().get(0).getIcon())
                .append(".png").toString()).into(img_weather);

        //Load all the weather information
        txt_city_name.setText(weatherResult.getName() + ", " + weatherResult.getSys().getCountry());
        txt_description.setText(weatherResult.getWeather().get(0).getDescription().substring(0, 1).toUpperCase()
                + weatherResult.getWeather().get(0).getDescription().substring(1).toLowerCase());
        txt_feels_like.setText("Feels Like: " + weatherResult.getMain().getFeels_like() + "°C");
        txt_temperature.setText((int) weatherResult.getMain().getTemp() + "°C");
        txt_date_time.setText(Common.convertUnixToDate(weatherResult.getDt()));
        txt_wind.setText(weatherResult.getWind().getSpeed() + " m/s");
        txt_pressure.setText(weatherResult.getMain().getPressure() + " hpa");
        txt_humidity.setText(weatherResult.getMain().getHumidity() + " %");
        txt_sunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
        txt_sunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
        txt_geo_coord.setText(weatherResult.getCoord().toString());

        //Display the panel
        weather_panel.setVisibility(View.VISIBLE);
        details.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
