package com.example.mobileapplicationweatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplicationweatherapp.data.WeatherResponse
import com.example.mobileapplicationweatherapp.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val apiKey: String
) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> = _weatherData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchWeatherForCity(cityName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getWeatherByCity(cityName, apiKey)
                .onSuccess {
                    _weatherData.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
        }
    }

    fun fetchWeatherForZip(zipCode: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getWeatherByZip(zipCode, apiKey)
                .onSuccess {
                    _weatherData.value = it
                    _isLoading.value = false
                }
                .onFailure {
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
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
        }
    }
}