package com.example.climbingteam.data

import com.google.gson.annotations.SerializedName

// --- Geocoding models ---
data class GeocodingResponse(
    val results: List<GeoLocation>?
)

data class GeoLocation(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val country: String?,
    @SerializedName("admin1") val region: String?,
    @SerializedName("admin2") val province: String?,
    val timezone: String?
) {
    val displayName: String
        get() = buildString {
            append(name)
            if (!province.isNullOrBlank() && province != name) append(", $province")
            else if (!region.isNullOrBlank() && region != name) append(", $region")
        }
}

// --- Open-Meteo forecast models ---
data class ForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val timezone: String?,
    val current: CurrentWeather?,
    val hourly: HourlyData?,
    val daily: DailyData?
)

data class CurrentWeather(
    val time: String?,
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Double?,
    val precipitation: Double?,
    val rain: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("wind_direction_10m") val windDirection: Double?,
    @SerializedName("wind_gusts_10m") val windGusts: Double?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    val visibility: Double?
)

data class HourlyData(
    val time: List<String>?,
    @SerializedName("temperature_2m") val temperature: List<Double?>?,
    @SerializedName("relative_humidity_2m") val humidity: List<Double?>?,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Double?>?,
    val precipitation: List<Double?>?,
    @SerializedName("weather_code") val weatherCode: List<Int?>?,
    @SerializedName("wind_speed_10m") val windSpeed: List<Double?>?,
    @SerializedName("wind_direction_10m") val windDirection: List<Double?>?,
    val visibility: List<Double?>?
)

data class DailyData(
    val time: List<String>?,
    @SerializedName("weather_code") val weatherCode: List<Int?>?,
    @SerializedName("temperature_2m_max") val tempMax: List<Double?>?,
    @SerializedName("temperature_2m_min") val tempMin: List<Double?>?,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double?>?,
    @SerializedName("precipitation_probability_max") val precipitationProbMax: List<Double?>?,
    @SerializedName("wind_speed_10m_max") val windSpeedMax: List<Double?>?
)

// --- App domain models ---
data class LocationWeather(
    val location: GeoLocation,
    val current: CurrentWeather?,
    val hourlyForecast: List<HourlyPoint>,
    val dailyForecast: List<DailyPoint>,
    val elevation: Double?,
    val climbingCondition: ClimbingCondition
)

data class HourlyPoint(
    val time: String,
    val temperature: Double?,
    val humidity: Double?,
    val precipProbability: Double?,
    val precipitation: Double?,
    val weatherCode: Int?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val visibility: Double?
)

data class DailyPoint(
    val date: String,
    val weatherCode: Int?,
    val tempMax: Double?,
    val tempMin: Double?,
    val precipSum: Double?,
    val precipProbMax: Double?,
    val windSpeedMax: Double?
)

enum class ClimbingCondition(val label: String, val colorHex: Long) {
    OPTIMO("Óptimo", 0xFF4CAF50),
    ACEPTABLE("Aceptable", 0xFFFFA726),
    ADVERSO("Adverso", 0xFFEF5350);
}

data class SavedLocation(
    val id: Long,
    val name: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?
)

// Weather code descriptions
fun getWeatherDescription(code: Int?): String = when (code) {
    0 -> "Despejado"
    1 -> "Mayormente despejado"
    2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Niebla"
    51, 53, 55 -> "Llovizna"
    56, 57 -> "Llovizna helada"
    61, 63, 65 -> "Lluvia"
    66, 67 -> "Lluvia helada"
    71, 73, 75 -> "Nieve"
    77 -> "Granizo"
    80, 81, 82 -> "Chubascos"
    85, 86 -> "Chubascos de nieve"
    95 -> "Tormenta"
    96, 99 -> "Tormenta con granizo"
    else -> "Desconocido"
}

fun getWeatherEmoji(code: Int?): String = when (code) {
    0 -> "☀️"
    1 -> "🌤"
    2 -> "⛅"
    3 -> "☁️"
    45, 48 -> "🌫"
    51, 53, 55, 61, 63, 65 -> "🌧"
    56, 57, 66, 67 -> "🌧"
    71, 73, 75, 77 -> "❄️"
    80, 81, 82 -> "🌦"
    85, 86 -> "🌨"
    95, 96, 99 -> "⛈"
    else -> "🌡"
}
