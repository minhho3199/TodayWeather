package com.example.todayweatherapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todayweatherapp.Adapter.WeatherForecastAdapter;
import com.example.todayweatherapp.Common.Common;
import com.example.todayweatherapp.Model.WeatherForecastResult;
import com.example.todayweatherapp.Retrofit.IOpenWeatherMap;
import com.example.todayweatherapp.Retrofit.RetrofitClient;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

    static ForecastFragment instance;
    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;
    TextView txt_city_name;
    RecyclerView recyclerView;
    WeatherForecastResult weatherForecastResult;
    WeatherForecastAdapter adapter;
    public static ForecastFragment getInstance() {
        if (instance == null) {
            instance = new ForecastFragment();
        }
        return instance;
    }

    public ForecastFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_forecast, container, false);
        txt_city_name = itemView.findViewById(R.id.txt_city_name);
        recyclerView = itemView.findViewById(R.id.recycler_forecast);
        weatherForecastResult = new WeatherForecastResult();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        getForecastWeatherInformationByLatLong();
        return itemView;
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

    public void getForecastWeatherInformationByCityName(String cityName) {
        compositeDisposable.add(mService.getForecastWeatherByCityName(
                cityName,
                Common.APP_ID, "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherForecastResult>() {
                    @Override
                    public void accept(WeatherForecastResult weatherForecastResult) throws Exception {
                        displayForecastWeather(weatherForecastResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }));
    }
    public void getForecastWeatherInformationByLatLong() {
        compositeDisposable.add(mService.getForecastWeatherByLatLong(
                String.valueOf(Common.CURRENT_LOCATION.getLatitude()),
                String.valueOf(Common.CURRENT_LOCATION.getLongitude()),
                Common.APP_ID, "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherForecastResult>() {
                    @Override
                    public void accept(WeatherForecastResult weatherForecastResult) throws Exception {
                        displayForecastWeather(weatherForecastResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }));
    }

    private void displayForecastWeather(WeatherForecastResult weatherForecastResult) {
        txt_city_name.setText(new StringBuilder(weatherForecastResult.getCity().getName()));
        adapter = new WeatherForecastAdapter(getContext(), weatherForecastResult);
        recyclerView.setAdapter(adapter);
    }
    }
