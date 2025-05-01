package com.example.mobileapplicationweatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapplicationweatherapp.ui.theme.MobileApplicationWeatherAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


// a test class to test key is active
class TestApiActivity : ComponentActivity() {
    private val TAG = "TestApiActivity"
    private val API_KEY = "73936049c862d1a66c440363f33f98e2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileApplicationWeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestApiScreen(API_KEY)
                }
            }
        }
    }
}

@Composable
fun TestApiScreen(apiKey: String) {
    val coroutineScope = rememberCoroutineScope()

    var resultText by remember { mutableStateOf("Press the button to test API calls\nAPI Key: $apiKey") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                isLoading = true
                coroutineScope.launch {
                    resultText = "Testing API calls...\nAPI Key: $apiKey"

                    // Test city name
                    val resultByCity = testCityNameCall("Saint Paul", apiKey)
                    resultText += "\n\nBy City Name: $resultByCity"

                    // Test ZIP code
                    val resultByZip = testZipCall("55101,us", apiKey)
                    resultText += "\n\nBy ZIP: $resultByZip"

                    // Test coordinates
                    val resultByCoords = testCoordinatesCall(44.9537, -93.0900, apiKey)
                    resultText += "\n\nBy Coordinates: $resultByCoords"

                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Test API Calls")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "API Test Results",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = resultText,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

suspend fun testCityNameCall(city: String, apiKey: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=imperial"

            Log.d("TestApiActivity", "Testing URL: $url")

            val request = Request.Builder()
                .url(url)
                .build()

            val response: Response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: "No response body"

            Log.d("TestApiActivity", "Response code: ${response.code}")
            Log.d("TestApiActivity", "Response body: $responseBody")

            if (response.isSuccessful) {
                "Success (${response.code})"
            } else {
                "Failed (${response.code}): $responseBody"
            }
        } catch (e: IOException) {
            Log.e("TestApiActivity", "Error in API call", e)
            "Error: ${e.message}"
        }
    }
}

suspend fun testZipCall(zip: String, apiKey: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "https://api.openweathermap.org/data/2.5/weather?zip=$zip&appid=$apiKey&units=imperial"

            Log.d("TestApiActivity", "Testing URL: $url")

            val request = Request.Builder()
                .url(url)
                .build()

            val response: Response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: "No response body"

            Log.d("TestApiActivity", "Response code: ${response.code}")
            Log.d("TestApiActivity", "Response body: $responseBody")

            if (response.isSuccessful) {
                "Success (${response.code})"
            } else {
                "Failed (${response.code}): $responseBody"
            }
        } catch (e: IOException) {
            Log.e("TestApiActivity", "Error in API call", e)
            "Error: ${e.message}"
        }
    }
}

suspend fun testCoordinatesCall(lat: Double, lon: Double, apiKey: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=imperial"

            Log.d("TestApiActivity", "Testing URL: $url")

            val request = Request.Builder()
                .url(url)
                .build()

            val response: Response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: "No response body"

            Log.d("TestApiActivity", "Response code: ${response.code}")
            Log.d("TestApiActivity", "Response body: $responseBody")

            if (response.isSuccessful) {
                "Success (${response.code})"
            } else {
                "Failed (${response.code}): $responseBody"
            }
        } catch (e: IOException) {
            Log.e("TestApiActivity", "Error in API call", e)
            "Error: ${e.message}"
        }
    }
}