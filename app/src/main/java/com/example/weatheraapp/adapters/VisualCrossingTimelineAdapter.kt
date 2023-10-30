package com.example.weatheraapp.adapters

import com.example.weatheraapp.model.VisualCrossingTimelineData
import com.example.weatheraapp.model.WeatherData

class VisualCrossingTimelineAdapter: WeatherDataAdapter {
    override fun convertToStandardFormat(response: Any): WeatherData {
        val apiResponse = response as VisualCrossingTimelineData
        return WeatherData(
            weatherDescription = apiResponse.description,
            temp = fahrenheitToCelsius(apiResponse.currentConditions.temp).toInt(),
            humidity = apiResponse.currentConditions.humidity.toInt()
        )
    }

    private fun fahrenheitToCelsius(fahrenheit: Double): Double {
        return (fahrenheit - 32.0) * 5.0 / 9.0
    }
}