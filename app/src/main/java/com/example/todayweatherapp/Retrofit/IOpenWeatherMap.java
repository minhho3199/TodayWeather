package com.example.todayweatherapp.Retrofit;

import com.example.todayweatherapp.Model.WeatherForecastResult;
import com.example.todayweatherapp.Model.WeatherResult;

import io.reactivex.Observable;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IOpenWeatherMap {
    @GET("weather")
    Observable<WeatherResult> getWeatherByLatLong(@Query("lat") String lat,
                                                  @Query("lon") String lon,
                                                  @Query("appid") String appid,
                                                  @Query("units") String unit);
    @GET("weather")
    Observable<WeatherResult> getWeatherByCityName(@Query("q") String city,
                                                  @Query("appid") String appid,
                                                  @Query("units") String unit);
    @GET("forecast")
    Observable<WeatherForecastResult> getForecastWeatherByLatLong(@Query("lat") String lat,
                                                                 @Query("lon") String lon,
                                                                 @Query("appid") String appid,
                                                                 @Query("units") String unit);
    @GET("forecast")
    Observable<WeatherForecastResult> getForecastWeatherByCityName(@Query("q") String city,
                                                                   @Query("appid") String appid,
                                                                   @Query("units") String unit);
}
