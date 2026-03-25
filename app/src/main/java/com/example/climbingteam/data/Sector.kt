package com.example.climbingteam.data

data class Sector(
    val nombre: String,
    val ubicacion: String,
    val ccaa: String,
    val estilo: String,
    val roca: String,
    val lat: Double?,
    val lon: Double?,
    val fuente: String
)

data class SectorResult(
    val sector: Sector,
    val distanceKm: Double?,
    val dailyForecast: List<DailyPoint>,
    val conditions: List<ClimbingCondition>,
    val bestCondition: ClimbingCondition
)
