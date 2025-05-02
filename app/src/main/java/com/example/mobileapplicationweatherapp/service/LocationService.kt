package com.example.mobileapplicationweatherapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mobileapplicationweatherapp.MainActivity
import com.example.mobileapplicationweatherapp.R
import com.example.mobileapplicationweatherapp.WeatherApplication
import com.example.mobileapplicationweatherapp.data.WeatherResponse
import com.example.mobileapplicationweatherapp.repository.WeatherRepository
import com.example.mobileapplicationweatherapp.utils.WeatherIcons
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationService : Service() {
    private val serviceBinder = LocationServiceBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var weatherRepository: WeatherRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var currentLocation: Location? = null
    private var apiKey: String = ""
    private var currentWeather: WeatherResponse? = null

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10 * 60 * 1000 // 10 minutes
    ).build()

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "weather_channel"
    }

    inner class LocationServiceBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the WeatherRepository
        val application = applicationContext as? WeatherApplication
        if (application != null) {
            weatherRepository = application.weatherRepository
            apiKey = application.apiKey
        } else {
            Log.e(TAG, "Failed to get application for repository")
        }

        // Create notification channel
        createNotificationChannel()

        // Set up location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // We got a location update
                    currentLocation = location
                    Log.d(TAG, "Location update: ${location.latitude}, ${location.longitude}")

                    // Fetch weather data for this location
                    fetchWeatherForLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        // Start as a foreground service with a notification
        startForeground(NOTIFICATION_ID, createNotification(null))

        // Start location updates
        startLocationUpdates()

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return serviceBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            // Request a single location update immediately
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = it
                        fetchWeatherForLocation(it.latitude, it.longitude)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get last location", e)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission denied", e)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun fetchWeatherForLocation(latitude: Double, longitude: Double) {
        serviceScope.launch {
            try {
                weatherRepository.getWeatherByCoordinates(latitude, longitude, apiKey)
                    .onSuccess { weatherResponse ->
                        currentWeather = weatherResponse
                        // Update the notification with new weather data
                        updateNotification(weatherResponse)
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to fetch weather", error)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception when fetching weather", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(weatherData: WeatherResponse?): android.app.Notification {
        // Create an intent that opens the app when the notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Default notification content
        var contentTitle = getString(R.string.notification_title)
        var contentText = getString(R.string.notification_loading)
        var smallIcon = R.drawable.ic_notification_default

        // Update with weather data if available
        weatherData?.let { weather ->
            contentTitle = weather.name
            contentText = "${weather.main.temperature.toInt()}° - ${weather.weather.firstOrNull()?.description?.capitalize() ?: ""}"

            // Set icon based on weather condition - This is where we call getNotificationIcon
            weather.weather.firstOrNull()?.let { condition ->
                smallIcon = WeatherIcons.getNotificationIcon(condition.icon)
            }
        }

        // Build the notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(weatherData: WeatherResponse) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(weatherData))
    }

    // Public method to get current location
    fun getCurrentLocation(): Location? = currentLocation

    // Public method to get current weather
    fun getCurrentWeather(): WeatherResponse? = currentWeather

    // Helper function
    private fun String.capitalize(): String {
        return if (this.isNotEmpty()) {
            this[0].uppercase() + this.substring(1)
        } else {
            this
        }
    }
}