package com.example.climbingteam.api

import android.util.Log
import com.example.climbingteam.data.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object OpenMeteoApi {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun searchLocations(query: String): List<GeoLocation> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext emptyList()
        try {
            val url = "https://geocoding-api.open-meteo.com/v1/search" +
                    "?name=${java.net.URLEncoder.encode(query, "UTF-8")}" +
                    "&count=10&language=es&format=json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            response.close()
            val result = gson.fromJson(body, GeocodingResponse::class.java)
            result.results ?: emptyList()
        } catch (e: Exception) {
            Log.e("OpenMeteo", "Error searching locations", e)
            emptyList()
        }
    }

    suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        forecastDays: Int = 10
    ): ForecastResponse? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$latitude&longitude=$longitude" +
                    "&current=temperature_2m,relative_humidity_2m,precipitation,rain," +
                    "weather_code,wind_speed_10m,wind_direction_10m,wind_gusts_10m," +
                    "apparent_temperature" +
                    "&hourly=temperature_2m,relative_humidity_2m,precipitation_probability," +
                    "precipitation,weather_code,wind_speed_10m,wind_direction_10m,visibility" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min," +
                    "precipitation_sum,precipitation_probability_max,wind_speed_10m_max" +
                    "&forecast_days=$forecastDays" +
                    "&timezone=auto"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            response.close()
            if (body.isBlank()) return@withContext null
            gson.fromJson(body, ForecastResponse::class.java)
        } catch (e: Exception) {
            Log.e("OpenMeteo", "Error fetching forecast", e)
            null
        }
    }

    fun parseHourlyData(hourly: HourlyData?): List<HourlyPoint> {
        if (hourly?.time == null) return emptyList()
        return hourly.time.indices.map { i ->
            HourlyPoint(
                time = hourly.time[i],
                temperature = hourly.temperature?.getOrNull(i),
                humidity = hourly.humidity?.getOrNull(i),
                precipProbability = hourly.precipitationProbability?.getOrNull(i),
                precipitation = hourly.precipitation?.getOrNull(i),
                weatherCode = hourly.weatherCode?.getOrNull(i),
                windSpeed = hourly.windSpeed?.getOrNull(i),
                windDirection = hourly.windDirection?.getOrNull(i),
                visibility = hourly.visibility?.getOrNull(i)
            )
        }
    }

    fun parseDailyData(daily: DailyData?): List<DailyPoint> {
        if (daily?.time == null) return emptyList()
        return daily.time.indices.map { i ->
            DailyPoint(
                date = daily.time[i],
                weatherCode = daily.weatherCode?.getOrNull(i),
                tempMax = daily.tempMax?.getOrNull(i),
                tempMin = daily.tempMin?.getOrNull(i),
                precipSum = daily.precipitationSum?.getOrNull(i),
                precipProbMax = daily.precipitationProbMax?.getOrNull(i),
                windSpeedMax = daily.windSpeedMax?.getOrNull(i)
            )
        }
    }

    fun evaluateClimbingCondition(current: CurrentWeather?): ClimbingCondition {
        if (current == null) return ClimbingCondition.ADVERSO
        val temp = current.temperature ?: return ClimbingCondition.ADVERSO
        val wind = current.windSpeed ?: 0.0
        val precip = current.precipitation ?: 0.0
        val humidity = current.humidity ?: 50.0
        val code = current.weatherCode ?: 0

        var score = 100

        // Temperature scoring
        when {
            temp < 0 -> score -= 40
            temp < 5 -> score -= 25
            temp < 10 -> score -= 10
            temp in 10.0..25.0 -> score -= 0
            temp in 25.0..32.0 -> score -= 15
            temp > 32 -> score -= 35
        }

        // Wind scoring
        when {
            wind > 50 -> score -= 40
            wind > 35 -> score -= 25
            wind > 20 -> score -= 10
            wind > 10 -> score -= 5
        }

        // Precipitation scoring
        when {
            precip > 5 -> score -= 40
            precip > 1 -> score -= 25
            precip > 0 -> score -= 10
        }

        // Humidity scoring
        when {
            humidity > 90 -> score -= 15
            humidity > 75 -> score -= 5
        }

        // Weather code scoring (storms, rain, snow)
        when (code) {
            in 95..99 -> score -= 30 // storms
            in 61..67 -> score -= 20 // rain
            in 71..77 -> score -= 20 // snow
            in 80..82 -> score -= 15 // showers
            in 51..57 -> score -= 10 // drizzle
            45, 48 -> score -= 10     // fog
        }

        return when {
            score >= 65 -> ClimbingCondition.OPTIMO
            score >= 40 -> ClimbingCondition.ACEPTABLE
            else -> ClimbingCondition.ADVERSO
        }
    }

    fun generateClimbingRecommendation(weather: LocationWeather): String {
        val current = weather.current ?: return "Sin datos disponibles."
        val condition = weather.climbingCondition
        val temp = current.temperature?.let { "${it.toInt()}°C" } ?: "N/D"
        val wind = current.windSpeed?.let { "${it.toInt()} km/h" } ?: "N/D"
        val humidity = current.humidity?.let { "${it.toInt()}%" } ?: "N/D"

        val hourly = weather.hourlyForecast.take(12)
        val rainHours = hourly.filter { (it.precipProbability ?: 0.0) > 50 }
        val rainInfo = if (rainHours.isNotEmpty()) {
            "Se esperan lluvias en las próximas horas."
        } else {
            "No se esperan lluvias significativas."
        }

        return when (condition) {
            ClimbingCondition.OPTIMO ->
                "Condiciones óptimas para escalar. Temp: $temp, viento: $wind, humedad: $humidity. $rainInfo Salida recomendada."

            ClimbingCondition.ACEPTABLE ->
                "Condiciones aceptables. Temp: $temp, viento: $wind. $rainInfo Precaución con los cambios de tiempo."

            ClimbingCondition.ADVERSO ->
                "Condiciones adversas para escalar. Temp: $temp, viento: $wind, humedad: $humidity. $rainInfo Se recomienda posponer la salida."
        }
    }
}
