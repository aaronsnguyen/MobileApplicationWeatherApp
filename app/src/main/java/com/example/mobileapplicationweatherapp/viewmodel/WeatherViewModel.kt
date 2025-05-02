package com.example.mobileapplicationweatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplicationweatherapp.data.ForecastResponse
import com.example.mobileapplicationweatherapp.data.WeatherResponse
import com.example.mobileapplicationweatherapp.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val apiKey: String
) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> = _weatherData

    private val _forecastData = MutableLiveData<ForecastResponse>()
    val forecastData: LiveData<ForecastResponse> = _forecastData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Make _error public so it can be set directly in the UI
    val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // To store the current location name
    private val _currentLocation = MutableLiveData<String>()
    val currentLocation: LiveData<String> = _currentLocation

    // Zip code validation
    private val zipCodeRegex = Regex("^\\d{5}$")

    fun isValidZipCode(zipCode: String): Boolean {
        return zipCode.matches(zipCodeRegex)
    }

    fun fetchWeatherForCity(cityName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getWeatherByCity(cityName, apiKey)
                .onSuccess {
                    _weatherData.value = it
                    _currentLocation.value = it.name
                    _isLoading.value = false
                    // Also fetch forecast for the same city
                    fetchForecastForCity(cityName)
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
        }
    }

    fun fetchWeatherForZip(zipCode: String) {
        if (!isValidZipCode(zipCode)) {
            _error.value = "Please enter a valid 5-digit zip code"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            repository.getWeatherByZip("$zipCode,us", apiKey)
                .onSuccess {
                    _weatherData.value = it
                    _currentLocation.value = it.name
                    _isLoading.value = false
                    // Also fetch forecast for the same zip
                    fetchForecastForZip(zipCode)
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error occurred"
                    _isLoading
                    _error.value = it.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
        }
    }

    fun fetchWeatherForCoordinates(latitude: Double, longitude: Double) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getWeatherByCoordinates(latitude, longitude, apiKey)
                .onSuccess {
                    _weatherData.value = it
                    _currentLocation.value = it.name
                    _isLoading.value = false
                    // Also fetch forecast for the same coordinates
                    fetchForecastForCoordinates(latitude, longitude)
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
        }
    }

    // Forecast methods
    private fun fetchForecastForCity(cityName: String) {
        viewModelScope.launch {
            repository.getForecastByCity(cityName, apiKey)
                .onSuccess {
                    _forecastData.value = it
                }
                .onFailure {
                    // We don't set error here as we already have current weather
                    // Just log it or handle silently
                }
        }
    }

    private fun fetchForecastForZip(zipCode: String) {
        viewModelScope.launch {
            repository.getForecastByZip("$zipCode,us", apiKey)
                .onSuccess {
                    _forecastData.value = it
                }
                .onFailure {
                    // We don't set error here as we already have current weather
                    // Just log it or handle silently
                }
        }
    }

    private fun fetchForecastForCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.getForecastByCoordinates(latitude, longitude, apiKey)
                .onSuccess {
                    _forecastData.value = it
                }
                .onFailure {
                    // We don't set error here as we already have current weather
                    // Just log it or handle silently
                }
        }
    }

    // Clear error state
    fun clearError() {
        _error.value = null
    }
}