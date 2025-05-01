package com.example.mobileapplicationweatherapp

import android.app.Application
import com.example.mobileapplicationweatherapp.di.NetworkModule
import com.example.mobileapplicationweatherapp.repository.WeatherRepository

class WeatherApplication : Application() {

    // Updated API key from OpenWeatherMap
    val apiKey = "73936049c862d1a66c440363f33f98e2"

    // Create repository instance
    val weatherRepository by lazy {
        WeatherRepository(NetworkModule.weatherApiService)
    }
}