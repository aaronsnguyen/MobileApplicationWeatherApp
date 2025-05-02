package com.example.mobileapplicationweatherapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    @SerialName("city") val city: City,
    @SerialName("cod") val cod: String,
    @SerialName("message") val message: Double,
    @SerialName("cnt") val count: Int,
    @SerialName("list") val list: List<ForecastItem>
)

@Serializable
data class City(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("coord") val coordinates: Coordinates,
    @SerialName("country") val country: String,
    @SerialName("population") val population: Int,
    @SerialName("timezone") val timezone: Int
)

@Serializable
data class ForecastItem(
    @SerialName("dt") val dateTime: Long,
    @SerialName("main") val main: Main,
    @SerialName("weather") val weather: List<Weather>,
    @SerialName("clouds") val clouds: Clouds,
    @SerialName("wind") val wind: Wind,
    @SerialName("visibility") val visibility: Int,
    @SerialName("pop") val probabilityOfPrecipitation: Double,
    @SerialName("dt_txt") val dateTimeText: String,
    @SerialName("rain") val rain: Rain? = null,
    @SerialName("snow") val snow: Snow? = null
)

@Serializable
data class Rain(
    @SerialName("3h") val threeHour: Double? = null
)

@Serializable
data class Snow(
    @SerialName("3h") val threeHour: Double? = null
)