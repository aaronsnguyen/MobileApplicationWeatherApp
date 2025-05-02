package com.example.mobileapplicationweatherapp.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobileapplicationweatherapp.service.LocationService

class LocationViewModel : ViewModel() {
    private val _locationServiceBound = MutableLiveData(false)
    val locationServiceBound: LiveData<Boolean> = _locationServiceBound

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> = _currentLocation

    private var locationService: LocationService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocationServiceBinder
            locationService = binder.getService()
            _locationServiceBound.value = true

            // Get the current location if available
            updateCurrentLocation()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
            _locationServiceBound.value = false
        }
    }

    fun bindLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindLocationService(context: Context) {
        if (_locationServiceBound.value == true) {
            context.unbindService(serviceConnection)
            _locationServiceBound.value = false
        }
    }

    fun startLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        context.startService(intent)
        bindLocationService(context)
    }

    fun updateCurrentLocation() {
        locationService?.let { service ->
            _currentLocation.value = service.getCurrentLocation()
        }
    }

    fun getCurrentLocation(): Location? = locationService?.getCurrentLocation()

    override fun onCleared() {
        super.onCleared()
        // Service will be unbound by the unbindLocationService method called from Activity
    }
}