package com.example.mobileapplicationweatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileapplicationweatherapp.ui.theme.MobileApplicationWeatherAppTheme
import com.example.mobileapplicationweatherapp.viewmodel.WeatherViewModel
import com.example.mobileapplicationweatherapp.viewmodel.WeatherViewModelFactory
import kotlin.math.roundToInt

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: Starting MainActivity")

        setContent {
            MobileApplicationWeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val application = context.applicationContext as WeatherApplication

                    Log.d(TAG, "API Key being used: ${application.apiKey}")

                    val viewModelFactory = WeatherViewModelFactory(
                        application.weatherRepository,
                        application.apiKey
                    )

                    val viewModel: WeatherViewModel = viewModel(
                        factory = viewModelFactory
                    )

                    // Trigger weather fetch with the most reliable method (coordinates)
                    LaunchedEffect(key1 = true) {
                        Log.d(TAG, "LaunchedEffect: Fetching weather for St. Paul, MN coordinates")
                        viewModel.fetchWeatherForCoordinates(44.9537, -93.0900)
                    }

                    WeatherScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherData by viewModel.weatherData.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = true)
    val error by viewModel.error.observeAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                modifier = Modifier.offset(x = 15.dp)
            )
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading weather data...")
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
                        text = "Error loading weather data",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        viewModel.fetchWeatherForCoordinates(44.9537, -93.0900)
                    }) {
                        Text(text = "Retry")
                    }
                }
            }
        }
        // Weather content
        else if (weatherData != null) {
            // Weather Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Location
                Text(
                    text = weatherData?.name ?: stringResource(R.string.location),
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Temperature with Weather Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Temperature
                    Text(
                        text = weatherData?.main?.temperature?.roundToInt()?.let {
                            "$it°"
                        } ?: stringResource(R.string.current_temp),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(x = (-35).dp)
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    // Weather Icon
                    Image(
                        painter = painterResource(id = R.drawable.ic_sunny),
                        contentDescription = stringResource(R.string.sunny_icon_description),
                        modifier = Modifier
                            .size(64.dp)
                            .offset(x = 35.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feels like temperature
                Text(
                    text = weatherData?.main?.feelsLike?.roundToInt()?.let {
                        "Feels like $it°"
                    } ?: stringResource(R.string.feels_like_temp),
                    fontSize = 16.sp,
                    modifier = Modifier.offset(x = (-70).dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Weather details aligned to start
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 64.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = weatherData?.main?.tempMin?.roundToInt()?.let {
                            "Low $it°"
                        } ?: stringResource(R.string.low_temp),
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = weatherData?.main?.tempMax?.roundToInt()?.let {
                            "High $it°"
                        } ?: stringResource(R.string.high_temp),
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = weatherData?.main?.humidity?.let {
                            "Humidity $it%"
                        } ?: stringResource(R.string.humidity),
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = weatherData?.main?.pressure?.let {
                            "Pressure $it hPa"
                        } ?: stringResource(R.string.pressure),
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            // Initial empty state
            WeatherScreenPlaceholder()
        }
    }
}

@Composable
fun WeatherScreenPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Location
        Text(
            text = stringResource(R.string.location),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current Temperature with Sun Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Temperature
            Text(
                text = stringResource(R.string.current_temp),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = (-35).dp)
            )

            Spacer(modifier = Modifier.width(24.dp))

            // Sun Icon
            Image(
                painter = painterResource(id = R.drawable.ic_sunny),
                contentDescription = stringResource(R.string.sunny_icon_description),
                modifier = Modifier
                    .size(64.dp)
                    .offset(x = 35.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Feels like temperature
        Text(
            text = stringResource(R.string.feels_like_temp),
            fontSize = 16.sp,
            modifier = Modifier.offset(x = (-70).dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Weather details aligned to start
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.low_temp),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.high_temp),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.humidity),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.pressure),
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    MobileApplicationWeatherAppTheme {
        WeatherScreenPlaceholder()
    }
}