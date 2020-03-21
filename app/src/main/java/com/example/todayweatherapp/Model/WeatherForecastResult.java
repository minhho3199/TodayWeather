package com.example.todayweatherapp.Model;

import java.util.List;

public class WeatherForecastResult {
    private String cod;
    private List<WeatherList> list;
    private City city;

    public WeatherForecastResult() {
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
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
