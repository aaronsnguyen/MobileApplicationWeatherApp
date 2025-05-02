package com.example.mobileapplicationweatherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobileapplicationweatherapp.data.WeatherResponse
import com.example.mobileapplicationweatherapp.ui.ForecastScreen
import com.example.mobileapplicationweatherapp.ui.theme.MobileApplicationWeatherAppTheme
import com.example.mobileapplicationweatherapp.utils.WeatherIcons
import com.example.mobileapplicationweatherapp.viewmodel.LocationViewModel
import com.example.mobileapplicationweatherapp.viewmodel.WeatherViewModel
import com.example.mobileapplicationweatherapp.viewmodel.WeatherViewModelFactory
import kotlin.math.roundToInt

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var locationViewModel: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: Starting MainActivity")

        // Initialize ViewModels
        val application = applicationContext as WeatherApplication
        val viewModelFactory = WeatherViewModelFactory(
            application.weatherRepository,
            application.apiKey
        )

        weatherViewModel = viewModelFactory.create(WeatherViewModel::class.java)
        locationViewModel = LocationViewModel()

        setContent {
            MobileApplicationWeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: WeatherViewModel = viewModel(
                        factory = viewModelFactory
                    )

                    // Trigger weather fetch with the most reliable method (coordinates)
                    LaunchedEffect(key1 = true) {
                        Log.d(TAG, "LaunchedEffect: Fetching weather for St. Paul, MN coordinates")
                        viewModel.fetchWeatherForCoordinates(44.9537, -93.0900)
                    }

                    WeatherAppNavigation(viewModel, locationViewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to location service if it's running
        locationViewModel.bindLocationService(this)
    }

    override fun onStop() {
        super.onStop()
        // Unbind from location service when activity is stopped
        locationViewModel.unbindLocationService(this)
    }
}

@Composable
fun WeatherAppNavigation(viewModel: WeatherViewModel, locationViewModel: LocationViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "weather") {
        composable("weather") {
            WeatherScreen(viewModel, locationViewModel, navController)
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
fun WeatherScreen(
    viewModel: WeatherViewModel,
    locationViewModel: LocationViewModel,
    navController: NavController
) {
    val weatherData by viewModel.weatherData.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = true)
    val error by viewModel.error.observeAsState()

    var zipCode by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Define gradient background - dark themed to match reference image
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E2530),
            Color(0xFF252D3A)
        )
    )

    // Fix for syntax errors - declare notificationPermissionLauncher before using it
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Start location service regardless of notification permission
        // If notification permission is denied, the service will still work
        // but won't show notifications
        locationViewModel.startLocationService(context)
    }

    // Permission launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationPermissionsGranted = permissions.entries.all { it.value }

        if (locationPermissionsGranted) {
            // Location permissions granted, now check notification permission
            if (Build.VERSION.SDK_INT >= 33) { // Tiramisu is API 33
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // For older versions, notification permission is not needed
                // Start location service directly
                locationViewModel.startLocationService(context)
            }
        } else {
            // Show rationale if permissions were denied
            showPermissionRationale = true
        }
    }

    // Permission rationale dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text(stringResource(R.string.location_permission_title)) },
            text = { Text(stringResource(R.string.location_permission_rationale)) },
            confirmButton = {
                Button(onClick = {
                    showPermissionRationale = false
                    // Open app settings so user can grant permissions
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                Button(onClick = { showPermissionRationale = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

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
                onLocationClicked = {
                    // Check and request location permissions
                    val locationPermissions = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )

                    // Check if we have permissions
                    val hasLocationPermissions = locationPermissions.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }

                    if (hasLocationPermissions) {
                        // We already have location permissions, check notification permission
                        if (Build.VERSION.SDK_INT >= 33) { // Tiramisu is API 33
                            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasNotificationPermission) {
                                // We have all permissions, start the service
                                locationViewModel.startLocationService(context)
                            } else {
                                // Request notification permission
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            // For older versions, notification permission is not needed
                            locationViewModel.startLocationService(context)
                        }
                    } else {
                        // Request location permissions
                        locationPermissionLauncher.launch(locationPermissions)
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
    onLocationClicked: () -> Unit,
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

        // ZIP Code search with location button
        ZipCodeSearchWithLocation(
            zipCode = zipCode,
            onZipCodeChanged = onZipCodeChanged,
            onSearchClicked = onSearchClicked,
            onLocationClicked = onLocationClicked
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
                text = stringResource(R.string.view_weekly_forecast),
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
fun ZipCodeSearchWithLocation(
    zipCode: String,
    onZipCodeChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onLocationClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ZIP code input field
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

        // Search button
        Button(
            onClick = onSearchClicked,
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3D5AFE)
            )
        ) {
            Text(stringResource(R.string.search))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // My Location button (new)
        IconButton(
            onClick = onLocationClicked,
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFF3D5AFE), shape = RoundedCornerShape(4.dp))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_my_location),
                contentDescription = stringResource(R.string.my_location),
                tint = Color.White
            )
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
                text = stringResource(R.string.view_weekly_forecast),
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