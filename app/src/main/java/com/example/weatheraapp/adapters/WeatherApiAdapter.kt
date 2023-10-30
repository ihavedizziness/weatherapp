package com.example.weatheraapp.adapters

import com.example.weatheraapp.model.WeatherApiData
import com.example.weatheraapp.model.WeatherData

class WeatherApiAdapter: WeatherDataAdapter {
    override fun convertToStandardFormat(response: Any): WeatherData {
        val apiResponse = response as WeatherApiData
        return WeatherData(
            weatherDescription = apiResponse.current.condition.text,
            temp = apiResponse.current.temp_c.toInt(),
            humidity = apiResponse.current.humidity
        )
    }
}