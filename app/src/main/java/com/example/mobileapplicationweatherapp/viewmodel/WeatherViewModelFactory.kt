package com.example.mobileapplicationweatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobileapplicationweatherapp.repository.WeatherRepository

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val apiKey: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(repository, apiKey) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}