package com.example.mobileapplicationweatherapp.api

import com.example.mobileapplicationweatherapp.data.ForecastResponse
import com.example.mobileapplicationweatherapp.data.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): WeatherResponse

    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByZip(
        @Query("zip") zipCode: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): WeatherResponse

    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): WeatherResponse

    // Add forecast endpoint (16 day / daily forecast)
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): ForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByZip(
        @Query("zip") zipCode: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): ForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): ForecastResponse
}