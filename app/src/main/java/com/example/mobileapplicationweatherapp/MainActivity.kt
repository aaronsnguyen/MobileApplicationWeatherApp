package com.example.mobileapplicationweatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobileapplicationweatherapp.data.WeatherResponse
import com.example.mobileapplicationweatherapp.ui.ForecastScreen
import com.example.mobileapplicationweatherapp.ui.theme.MobileApplicationWeatherAppTheme
import com.example.mobileapplicationweatherapp.utils.WeatherIcons
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

                    WeatherAppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun WeatherAppNavigation(viewModel: WeatherViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "weather") {
        composable("weather") {
            WeatherScreen(viewModel, navController)
        }
        composable("forecast") {
            ForecastScreen(viewModel) {
                navController.popBackStack()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel, navController: NavController) {
    val weatherData by viewModel.weatherData.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = true)
    val error by viewModel.error.observeAsState()

    var zipCode by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

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
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = weatherData?.name ?: stringResource(R.string.location),
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = Color.White
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
                    CircularProgressIndicator(
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading),
                        color = Color.White
                    )
                }
            }
        }
        // Error dialog
        else if (error != null && showErrorDialog) {
            ErrorDialog(
                errorMessage = error ?: "Unknown error",
                onDismiss = {
                    showErrorDialog = false
                    viewModel.clearError()
                }
            )
        }
        // Weather content
        else if (weatherData != null) {
            WeatherContent(
                weatherData = weatherData,
                zipCode = zipCode,
                onZipCodeChanged = { zipCode = it },
                onSearchClicked = {
                    if (viewModel.isValidZipCode(zipCode)) {
                        viewModel.fetchWeatherForZip(zipCode)
                    } else {
                        showErrorDialog = true
                        viewModel.clearError()
                        viewModel._error.value = "Please enter a valid 5-digit zip code"
                    }
                },
                onForecastClicked = { navController.navigate("forecast") }
            )
        } else {
            // Initial empty state
            WeatherScreenPlaceholder()
        }
    }
}

@Composable
fun ErrorDialog(errorMessage: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.error_loading_weather)) },
        text = { Text(errorMessage) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.retry))
            }
        }
    )
}

@Composable
fun WeatherContent(
    weatherData: WeatherResponse?,
    zipCode: String,
    onZipCodeChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onForecastClicked: () -> Unit
) {
    if (weatherData == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Weather info card
        WeatherInfoCard(weatherData = weatherData)

        Spacer(modifier = Modifier.height(16.dp))

        // ZIP Code search
        ZipCodeSearch(
            zipCode = zipCode,
            onZipCodeChanged = onZipCodeChanged,
            onSearchClicked = onSearchClicked
        )

        Spacer(modifier = Modifier.weight(1f))

        // Forecast button
        Button(
            onClick = onForecastClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3D5AFE)
            )
        ) {
            Text(
                text = stringResource(R.string.view_forecast),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun WeatherInfoCard(weatherData: WeatherResponse) {
    // Weather tip card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A3341)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.update_time),
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weather tip based on conditions
            if (weatherData.weather.isNotEmpty()) {
                val iconCode = weatherData.weather.first().icon
                val tip = stringResource(
                    WeatherIcons.getWeatherTip(iconCode, weatherData.main.temperature)
                )

                Text(
                    text = tip,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Current temperature card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A3341)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weather icon
            if (weatherData.weather.isNotEmpty()) {
                val iconCode = weatherData.weather.first().icon
                Image(
                    painter = painterResource(id = WeatherIcons.getIconResource(iconCode)),
                    contentDescription = stringResource(R.string.weather_icon_description),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Temperature
                Text(
                    text = "${weatherData.main.temperature.roundToInt()}°",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Weather description
                Text(
                    text = weatherData.weather.first().description.capitalize(),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather details in row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Feels like
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.feels_like),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${weatherData.main.feelsLike.roundToInt()}°",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                // Humidity
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.humidity),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${weatherData.main.humidity}%",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                // Wind
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.wind),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${weatherData.wind.speed.roundToInt()} mph",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.next_week),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )

    // Preview of forecast
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A3341)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.view_full_forecast),
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZipCodeSearch(
    zipCode: String,
    onZipCodeChanged: (String) -> Unit,
    onSearchClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = zipCode,
            onValueChange = {
                if (it.length <= 5 && it.all { char -> char.isDigit() }) {
                    onZipCodeChanged(it)
                }
            },
            label = { Text(stringResource(R.string.enter_zip_code), color = Color.Gray) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFF2A3341),
                textColor = Color.White,
                cursorColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onSearchClicked,
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3D5AFE)
            )
        ) {
            Text(stringResource(R.string.search))
        }
    }
}

@Composable
fun WeatherScreenPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Location
        Text(
            text = stringResource(R.string.location),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Weather Icon
        Image(
            painter = painterResource(id = R.drawable.ic_sunny),
            contentDescription = stringResource(R.string.weather_icon_description),
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current Temperature
        Text(
            text = stringResource(R.string.current_temp),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Feels like temperature
        Text(
            text = stringResource(R.string.feels_like_temp),
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Weather details placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A3341)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.low_temp),
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Text(
                        text = stringResource(R.string.high_temp),
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.humidity),
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Text(
                        text = stringResource(R.string.pressure),
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Forecast button placeholder
        Button(
            onClick = { /* Not clickable in placeholder */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = false,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3D5AFE)
            )
        ) {
            Text(
                text = stringResource(R.string.view_forecast),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun String.capitalize(): String {
    return if (this.isNotEmpty()) {
        this[0].uppercase() + this.substring(1)
    } else {
        this
    }
}