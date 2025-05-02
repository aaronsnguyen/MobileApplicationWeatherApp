package com.example.mobileapplicationweatherapp.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapplicationweatherapp.R
import com.example.mobileapplicationweatherapp.data.ForecastItem
import com.example.mobileapplicationweatherapp.utils.WeatherIcons
import com.example.mobileapplicationweatherapp.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ForecastScreen"

@Composable
fun ForecastScreen(
    viewModel: WeatherViewModel,
    onBackClick: () -> Unit
) {
    val forecastData by viewModel.forecastData.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = true)
    val error by viewModel.error.observeAsState()
    val currentLocation by viewModel.currentLocation.observeAsState("")

    // Define gradient background - dark themed to match reference image
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E2530),
            Color(0xFF252D3A)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }

            Text(
                text = stringResource(R.string.forecast),
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.loading), color = Color.White)
                }
            }
        }
        // Error state
        else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.error_loading_weather),
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error ?: "Unknown error occurred",
                        color = Color.Red,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.clearError()
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D5AFE)
                        )
                    ) {
                        Text(text = stringResource(R.string.back))
                    }
                }
            }
        }
        // Forecast content
        else if (forecastData != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Location
                    Text(
                        text = currentLocation,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Today's forecast title
                    Text(
                        text = stringResource(R.string.next_week),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Get one forecast per day (at noon) for the next 5-7 days
                val dailyForecasts = forecastData?.list?.groupBy {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdf.format(Date(it.dateTime * 1000))
                }?.map { (_, items) ->
                    // Find forecast closest to noon for each day
                    items.minByOrNull {
                        val hour = SimpleDateFormat("HH", Locale.getDefault())
                            .format(Date(it.dateTime * 1000)).toInt()
                        Math.abs(hour - 12)
                    } ?: items.first()
                }

                if (dailyForecasts != null) {
                    items(dailyForecasts) { forecast ->
                        WeekdayForecastItem(forecast)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No forecast data available",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun WeekdayForecastItem(forecast: ForecastItem) {
    val dateFormatter = SimpleDateFormat("EEEE", Locale.getDefault()) // "Sunday", "Monday", etc.
    val date = dateFormatter.format(Date(forecast.dateTime * 1000))
    val iconCode = forecast.weather.firstOrNull()?.icon ?: "01d"
    val tempMax = forecast.main.tempMax.toInt()
    val tempMin = forecast.main.tempMin.toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A3341)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day of week
            Text(
                text = date,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.width(110.dp)
            )

            // Temperature with dots in between (like in reference image)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Max temp
                Text(
                    text = "$tempMax°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Dots between temperatures
                Text(
                    text = " • ",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                // Min temp
                Text(
                    text = "$tempMin°",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            // Weather icon
            Image(
                painter = painterResource(id = WeatherIcons.getIconResource(iconCode)),
                contentDescription = stringResource(R.string.weather_icon_description),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}