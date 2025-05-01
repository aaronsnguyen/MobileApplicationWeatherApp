package com.example.mobileapplicationweatherapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("coord") val coordinates: Coordinates,
    @SerialName("weather") val weather: List<Weather>,
    @SerialName("main") val main: Main,
    @SerialName("wind") val wind: Wind,
    @SerialName("clouds") val clouds: Clouds,
    @SerialName("dt") val dateTime: Long,
    @SerialName("sys") val sys: Sys,
    @SerialName("timezone") val timezone: Int,
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("cod") val cod: Int
)

@Serializable
data class Coordinates(
    @SerialName("lon") val longitude: Double,
    @SerialName("lat") val latitude: Double
)

@Serializable
data class Weather(
    @SerialName("id") val id: Int,
    @SerialName("main") val main: String,
    @SerialName("description") val description: String,
    @SerialName("icon") val icon: String
)

@Serializable
data class Main(
    @SerialName("temp") val temperature: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    @SerialName("pressure") val pressure: Int,
    @SerialName("humidity") val humidity: Int
)

@Serializable
data class Wind(
    @SerialName("speed") val speed: Double,
    @SerialName("deg") val degrees: Int
)

@Serializable
data class Clouds(
    @SerialName("all") val all: Int
)

@Serializable
data class Sys(
    @SerialName("type") val type: Int,
    @SerialName("id") val id: Int,
    @SerialName("country") val country: String,
    @SerialName("sunrise") val sunrise: Long,
    @SerialName("sunset") val sunset: Long
)