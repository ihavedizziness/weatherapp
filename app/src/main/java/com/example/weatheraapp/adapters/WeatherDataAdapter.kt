package com.example.weatheraapp.adapters

import com.example.weatheraapp.model.WeatherData

interface WeatherDataAdapter {
    fun convertToStandardFormat(response: Any): WeatherData
}