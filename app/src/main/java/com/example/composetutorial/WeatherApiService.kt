package com.example.composetutorial

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") latitude: Double = 65.0121, // Oulu
        @Query("longitude") longitude: Double = 25.4651,
        @Query("current") current: String = "temperature_2m,wind_speed_10m,weather_code"
    ): WeatherResponse
}

data class WeatherResponse(
    val current: CurrentWeather
)

data class CurrentWeather(
    val temperature_2m: Double,
    val wind_speed_10m: Double,
    val weather_code: Int
)
