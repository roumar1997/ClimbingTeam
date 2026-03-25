package com.example.climbingteam.repository

import android.content.Context
import com.example.climbingteam.data.Sector
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.*

object SectorRepository {

    private var cachedSectors: List<Sector>? = null

    fun loadSectors(context: Context): List<Sector> {
        cachedSectors?.let { return it }
        return try {
            val json = context.assets.open("sectores.json").bufferedReader().readText()
            val type = object : TypeToken<List<Sector>>() {}.type
            val list: List<Sector> = Gson().fromJson(json, type)
            cachedSectors = list
            list
        } catch (e: Exception) {
            android.util.Log.e("SectorRepo", "Error loading sectors", e)
            emptyList()
        }
    }

    fun filterSectors(
        all: List<Sector>,
        rocaFilter: Set<String>,
        estiloFilter: Set<String>,
        userLat: Double?,
        userLon: Double?,
        maxDistanceKm: Double?
    ): List<Sector> {
        return all.filter { sector ->
            // Rock type filter (startsWith match so "Caliza" matches "Caliza / calcoarenita")
            if (rocaFilter.isNotEmpty() && !rocaFilter.any { filter ->
                    sector.roca.startsWith(filter, ignoreCase = true)
                }) return@filter false

            // Style filter
            if (estiloFilter.isNotEmpty()) {
                val matches = estiloFilter.any { filter ->
                    when (filter) {
                        "Vía" -> sector.estilo.contains("vía", ignoreCase = true) ||
                                sector.estilo.contains("via", ignoreCase = true)
                        "Bloque" -> sector.estilo.contains("bloque", ignoreCase = true)
                        else -> sector.estilo.equals(filter, ignoreCase = true)
                    }
                }
                if (!matches) return@filter false
            }

            // Distance filter (only applies if location AND sector have coords)
            if (maxDistanceKm != null && userLat != null && userLon != null &&
                sector.lat != null && sector.lon != null
            ) {
                val dist = haversineKm(userLat, userLon, sector.lat, sector.lon)
                if (dist > maxDistanceKm) return@filter false
            }

            true
        }
    }

    fun distanceKm(userLat: Double?, userLon: Double?, sector: Sector): Double? {
        if (userLat == null || userLon == null || sector.lat == null || sector.lon == null) return null
        return haversineKm(userLat, userLon, sector.lat, sector.lon)
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
