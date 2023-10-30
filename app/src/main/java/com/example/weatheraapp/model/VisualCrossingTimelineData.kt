package com.example.weatheraapp.model

data class VisualCrossingTimelineData(
    val description: String,
    val currentConditions: CurrentConditions
)

data class CurrentConditions(
    val datetime: String,
    val temp: Double,
    val feelslike: Double,
    val humidity: Double,
    val precip: Double,
    val snow: Double,
    val windgust: Double,
    val windspeed: Double,
    val cloudcover: Double
)
