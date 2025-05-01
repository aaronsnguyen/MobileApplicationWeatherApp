package com.example.mobileapplicationweatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapplicationweatherapp.ui.theme.MobileApplicationWeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileApplicationWeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen()
                }
            }
        }
    }
}

@Composable
fun WeatherScreen() {
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
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    MobileApplicationWeatherAppTheme {
        WeatherScreen()
    }
}