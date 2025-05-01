package com.example.mobileapplicationweatherapp.repository

import com.example.mobileapplicationweatherapp.api.WeatherApiService
import com.example.mobileapplicationweatherapp.data.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val apiService: WeatherApiService) {

    suspend fun getWeatherByCity(city: String, apiKey: String): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentWeather(city, apiKey)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getWeatherByZip(zipCode: String, apiKey: String): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentWeatherByZip(zipCode, apiKey)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        apiKey: String
    ): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentWeatherByCoordinates(latitude, longitude, apiKey)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}