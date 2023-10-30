package com.example.weatheraapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Constants {

    const val METRIC_UNIT = "metric"

    const val OPEN_WEATHER_MAP_API_URL = "https://api.openweathermap.org/data/"
    const val OPEN_WEATHER_MAP_API_KEY = "65e7e965ba26acc552b32af6b7c1bb6a"

    const val VISUAL_CROSSING_TIMELINE_WEATHER_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/"
    const val VISUAL_CROSSING_TIMELINE_WEATHER_KEY = "GKE7KR7BCDGYCUV5BK7SL4XQX"

    const val WEATHER_API_URL = "http://api.weatherapi.com/v1/"
    const val WEATHER_API_KEY = "1de5770a76ef4fc8b33205052233010"

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }


}