package com.example.mobileapplicationweatherapp.utils

import com.example.mobileapplicationweatherapp.R

object WeatherIcons {
    fun getIconResource(iconCode: String): Int {
        return when (iconCode) {
            // Clear
            "01d" -> R.drawable.ic_clear_day  // Clear day
            "01n" -> R.drawable.ic_clear_night  // Clear night

            // Few clouds
            "02d" -> R.drawable.ic_partly_cloudy_day  // Few clouds day
            "02n" -> R.drawable.ic_partly_cloudy_night  // Few clouds night

            // Scattered clouds
            "03d", "03n" -> R.drawable.ic_cloudy  // Scattered clouds

            // Broken clouds
            "04d", "04n" -> R.drawable.ic_overcast  // Broken clouds

            // Shower rain
            "09d", "09n" -> R.drawable.ic_rain  // Shower rain

            // Rain
            "10d" -> R.drawable.ic_rain_day  // Rain day
            "10n" -> R.drawable.ic_rain_night  // Rain night

            // Thunderstorm
            "11d", "11n" -> R.drawable.ic_thunderstorm  // Thunderstorm

            // Snow
            "13d", "13n" -> R.drawable.ic_snow  // Snow

            // Mist
            "50d", "50n" -> R.drawable.ic_fog  // Mist

            // Default
            else -> R.drawable.ic_sunny
        }
    }

    fun getWeatherTip(iconCode: String, temp: Double): Int {
        // Basic logic for weather tips based on weather conditions and temperature
        return when {
            // Sunny/Clear conditions
            iconCode.startsWith("01") && temp > 85 -> R.string.tips_hot
            iconCode.startsWith("01") -> R.string.tips_sunny

            // Rainy conditions
            iconCode.startsWith("09") || iconCode.startsWith("10") -> R.string.tips_rainy

            // Cloudy conditions
            iconCode.startsWith("02") || iconCode.startsWith("03") || iconCode.startsWith("04") -> R.string.tips_cloudy

            // Snow
            iconCode.startsWith("13") -> R.string.tips_snow

            // Thunderstorm
            iconCode.startsWith("11") -> R.string.tips_thunderstorm

            // Temperature-based tips
            temp < 32 -> R.string.tips_cold

            // Default
            else -> R.string.tips_sunny
        }
    }
}