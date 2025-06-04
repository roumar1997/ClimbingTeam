// src/main/kotlin/com/example/climbingteam/Api/ObservacionEstacion.kt
package com.example.climbingteam.Api

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

/**
 * Mapea cada objeto dentro del array que AEMET devuelve en "observación convencional".
 * Ahora la API ha cambiado nombres de campos:
 *  - “fint” en lugar de “fecha”
 *  - “ta”              → temperatura (idem antes)
 *  - “hr” en lugar de “hu”              → humedad en %
 *  - “vv” en lugar de “ff”              → velocidad de viento en km/h
 *  - “dv” nuevo campo                  → dirección del viento en grados
 */
data class ObservacionEstacion(
    @SerializedName("idema") val idema: String,
    @SerializedName("fint") val fechaRaw: String,      // fecha en formato "2025-06-04T06:00:00+0000"
    @SerializedName("ta") val temperatura: Double?,    // temperatura real en ºC
    @SerializedName("hr") val humedad: Double?,        // humedad en %, como Double
    @SerializedName("vv") val vientoVelocidad: Double?,// velocidad del viento en km/h
    @SerializedName("dv") val vientoDireccion: Double? // dirección del viento en grados
) {
    /**
     * Convierte “fint” (p.ej. "2025-06-04T06:00:00+0000") a OffsetDateTime.
     * Luego, si quieres, puedes extraer la hora con toLocalTime().
     */
    fun getFechaDateTime(): OffsetDateTime? = try {
        // El sufijo “+0000” no es ISO estándar; lo convertimos a “Z”
        val iso = fechaRaw.removeSuffix("+0000") + "Z"
        OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    } catch (e: Exception) {
        null
    }

    /** Temperatura como Double (null si no se puede parsear) */
    fun getTempDouble(): Double? = temperatura

    /** Humedad en porcentaje, como Double (null si no se pudo parsear) */
    fun getHumedadDouble(): Double? = humedad

    /** Velocidad de viento en km/h */
    fun getVelVientoDouble(): Double? = vientoVelocidad

    /** Dirección del viento en grados (0=N, 90=E, 180=S, 270=O) */
    fun getDirVientoGrados(): Double? = vientoDireccion

    /**
     * (Opcional) Convierte grados a palabras (N, NE, E, SE, S, SW, W, NW).
     * Si prefieres mostrar el valor en grados, puedes no usar esto.
     */
    fun getDirVientoCardinal(): String? {
        val deg = vientoDireccion ?: return null
        return when {
            deg < 22.5 || deg >= 337.5 -> "N"
            deg < 67.5  -> "NE"
            deg < 112.5 -> "E"
            deg < 157.5 -> "SE"
            deg < 202.5 -> "S"
            deg < 247.5 -> "SW"
            deg < 292.5 -> "W"
            else        -> "NW"
        }
    }

    /**
     * Calcula la sensación térmica en ºC aproximada.
     * - Si T ≤ 10°C: calcula Wind Chill (sens. térmica por frío).
     * - Si T ≥ 27°C: calcula Heat Index (sensación por calor/humedad).
     * - Si 10 < T < 27: devuelve T sin cambios.
     */
    fun getSensacionTermica(): Double? {
        val T = getTempDouble() ?: return null
        val RH = getHumedadDouble() ?: return null
        val v = getVelVientoDouble() ?: return null

        return when {
            // 1) Wind Chill (temperaturas ≤ 10°C)
            T <= 10.0 && v > 4.8 -> {
                13.12 + 0.6215 * T - 11.37 * v.pow(0.16) + 0.3965 * T * v.pow(0.16)
            }
            // 2) Heat Index (temperaturas ≥ 27°C)
            T >= 27.0 -> {
                val t2 = T * T
                val rh2 = RH * RH
                (
                        -8.784695 +
                                1.61139411 * T +
                                2.33854900 * RH -
                                0.14611605 * T * RH -
                                0.012308094 * t2 -
                                0.016424828 * rh2 +
                                0.002211732 * t2 * RH +
                                0.00072546 * T * rh2 -
                                0.000003582 * t2 * rh2
                        )
            }
            // 3) Entre 10 y 27°C: devolvemos T sin cambios
            else -> T
        }
    }
}
