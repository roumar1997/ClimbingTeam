// src/main/kotlin/com/example/climbingteam/Api/ObservacionEstacion.kt
package com.example.climbingteam.Api

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

//mapeo completo de los metadatos de aemet
data class ObservacionEstacion(
    @SerializedName("idema") val idema: String,
    @SerializedName("fint") val fechaRaw: String,      // fecha en formato "2025-06-04T06:00:00+0000"
    @SerializedName("ta") val temperatura: Double?,    // temperatura real en ºC
    @SerializedName("hr") val humedad: Double?,        // humedad en %, como Double
    @SerializedName("vv") val vientoVelocidad: Double?,// velocidad del viento en km/h
    @SerializedName("dv") val vientoDireccion: Double? // dirección del viento en grados
) {
    //la hora, da fallo por que aemet no permite pasarlo a formato 24horas aunque probé todos los date_time posibles
    fun getFechaDateTime(): OffsetDateTime? = try {
        // El sufijo “+0000” no es ISO estándar; lo convertimos a “Z”
        val iso = fechaRaw.removeSuffix("+0000") + "Z"
        OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    } catch (e: Exception) {
        null
    }

    //temperatura, le pasa igual que a humedad
    fun getTempDouble(): Double? = temperatura

    //humedad en % no siempre se puede parsear
    fun getHumedadDouble(): Double? = humedad

   //velocidad del viento
    fun getVelVientoDouble(): Double? = vientoVelocidad

   //direccion del viento
    fun getDirVientoGrados(): Double? = vientoDireccion


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
            // 3) Entre 10 y 27°C: devolvemos T sin cambios
            else -> Tiempo
        }
    }
}
