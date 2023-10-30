package com.example.weatheraapp.services

import com.example.weatheraapp.model.VisualCrossingTimelineData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VisualCrossingTimelineService {
    @GET("timeline/{location}")
    fun getWeather(
        @Path("location") location: String,
        @Query("key") apiKey: String
    ): Call<VisualCrossingTimelineData>
}