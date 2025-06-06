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




    //calcular sensacion termica aproximada con los metadatos
    //humedad,sensacion termica,etc
    fun getSensacionTermica(): Double? {
        val Tiempo = getTempDouble() ?: return null
        val hume = getHumedadDouble() ?: return null
        val viento = getVelVientoDouble() ?: return null

        return when {

            Tiempo   <= 10.0 && viento > 4.8 -> {
                13.12 + 0.6215 * Tiempo - 11.37 * viento.pow(0.16) + 0.3965 * Tiempo * viento.pow(0.16)
            }

            Tiempo >= 27.0 -> {
                val t2 = Tiempo * Tiempo
                val rh2 = hume * hume
                (
                        -8.784695 +
                                1.61139411 * Tiempo +
                                2.33854900 * hume -
                                0.14611605 * Tiempo * hume -
                                0.012308094 * t2 -
                                0.016424828 * rh2 +
                                0.002211732 * t2 * hume +
                                0.00072546 * Tiempo * rh2 -
                                0.000003582 * t2 * rh2
                        )
            }

            else -> Tiempo
        }
    }
}
