package com.example.weatheraapp.services

import com.example.weatheraapp.model.WeatherApiData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("current.json")
    fun getWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") aqi: String
    ): Call<WeatherApiData>
}
