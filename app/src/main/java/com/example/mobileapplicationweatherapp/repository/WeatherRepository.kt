package com.example.mobileapplicationweatherapp.repository

import com.example.mobileapplicationweatherapp.api.WeatherApiService
import com.example.mobileapplicationweatherapp.data.ForecastResponse
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

    // Add forecast methods
    suspend fun getForecastByCity(city: String, apiKey: String): Result<ForecastResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getForecast(city, apiKey)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getForecastByZip(zipCode: String, apiKey: String): Result<ForecastResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getForecastByZip(zipCode, apiKey)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getForecastByCoordinates(
        latitude: Double,
        longitude: Double,
        apiKey: String
    ): Result<ForecastResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getForecastByCoordinates(latitude, longitude, apiKey)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}