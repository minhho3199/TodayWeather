package com.example.todayweatherapp.Model;

import java.util.List;

public class WeatherForecastResult {
    private List<WeatherList> list;
    private City city;

    public WeatherForecastResult() {
    }

    public List<WeatherList> getList() {
        return list;
    }

    public void setList(List<WeatherList> list) {
        this.list = list;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}
