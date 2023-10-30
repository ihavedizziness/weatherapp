package com.example.weatheraapp.adapters

import com.example.weatheraapp.model.OpenWeatherMapData
import com.example.weatheraapp.model.WeatherData

class OpenWeatherMapAdapter: WeatherDataAdapter {
    override fun convertToStandardFormat(response: Any): WeatherData {
        val apiResponse = response as OpenWeatherMapData
        return WeatherData(
            weatherDescription = apiResponse.weather[0].description,
            temp = apiResponse.main.temp.toInt(),
            humidity = apiResponse.main.humidity
        )
    }
}