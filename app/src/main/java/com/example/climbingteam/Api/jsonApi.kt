package com.example.climbingteam.Api

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date


data class FeatureCollection(
    val type: String,
    val name: String,
    val features: List<Feature>
)

data class Feature(
    val type: String,
    val properties: Properties,
    val geometry: Geometry
)

data class Properties(
    val INDICATIVO: String,
    val NOMBRE: String,
    val PROVINCIA: String,
    val ALTITUD: Double,
    val COORD_X: Double,
    val COORD_Y: Double,
    val VAR_OBSVER: String,
    val DATUM: String,
    val TIPO: String
)

data class Geometry(
    val type: String,
    val coordinates: List<Double>
)



object jsonApi {
    var gson = Gson()
    var json = ""
    lateinit var estaciones : FeatureCollection

    fun initData (context: Context){

        json = context.assets.open("Estaciones_Completas.geojson").bufferedReader().use { it.readText() }
        estaciones = gson.fromJson(json,FeatureCollection::class.java)
    }

    fun formatDateToUTCString(date: Date): String {
        // Convierte Date a Instant y luego a un ZonedDateTime en UTC
        val utcDateTime = date.toInstant().atOffset(ZoneOffset.UTC)
        // Crea un formateador que genere "yyyy-MM-dd'T'HH:mm:ss'UTC'"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'UTC'")
        return utcDateTime.format(formatter)
    }

    suspend fun consultarObservacionConvencional(idema: String, apiKey: String) {
        // 1) Construye la URL inicial (no lleva fechas explícitas):
        val urlInicial = "https://opendata.aemet.es/opendata/api/observacion/convencional/datos/estacion/$idema"
        Log.d("AEMET", "URL inicial (sin api_key): $urlInicial")

        val client = OkHttpClient()
        var response: Response? = null

        withContext(Dispatchers.IO) {
            try {
                // === Paso 1: pedimos JSON que contiene "datos" ===
                val requestInicial = Request.Builder()
                    .url(urlInicial)
                    .addHeader("Accept", "application/json")
                    .addHeader("api_key", apiKey)
                    .build()

                response = client.newCall(requestInicial).execute()
                if (!response!!.isSuccessful) {
                    Log.e("AEMET", "Error en petición inicial: HTTP ${response!!.code}")
                    return@withContext
                }

                val body1 = response!!.body?.string()
                if (body1.isNullOrBlank()) {
                    Log.e("AEMET", "Respuesta inicial vacía.")
                    return@withContext
                }

                // Parseamos JSON para extraer "datos"
                val json1 = JSONObject(body1)
                val estado = json1.optInt("estado", -1)
                if (estado != 200) {
                    Log.w(
                        "AEMET",
                        "Estado != 200 en JSON inicial: estado=$estado, descripción=\"${json1.optString("descripcion", "")}\""
                    )
                    return@withContext
                }

                val urlCorta = json1.optString("datos", null)
                if (urlCorta.isNullOrBlank()) {
                    Log.e("AEMET", "No se obtuvo campo 'datos' en JSON inicial.")
                    return@withContext
                }
                Log.d("AEMET", "URL corta (datos): $urlCorta")
                response!!.body?.close()

                // === Paso 2: peticion a la URL corta para descargar observaciones ===
                val requestDatos = Request.Builder()
                    .url(urlCorta)
                    .addHeader("Accept", "application/json")
                    .addHeader("api_key", apiKey)
                    .build()

                response = client.newCall(requestDatos).execute()
                if (!response!!.isSuccessful) {
                    Log.e("AEMET", "Error al pedir datos reales: HTTP ${response!!.code}")
                    return@withContext
                }

                // Si la API devolviera 204 No Content (poco probable aquí), lo manejamos:
                if (response!!.code == 204) {
                    Log.w("AEMET", "204 No Content: no hay datos para estación $idema.")
                    return@withContext
                }

                val body2 = response!!.body?.string()
                if (body2.isNullOrBlank()) {
                    Log.w("AEMET", "Cuerpo vacío en URL corta (posible falta de datos).")
                    return@withContext
                }

                // Aquí ya tienes el JSON definitivo con todas las observaciones de las últimas 24 h:
                Log.d("AEMET", "Observación convencional:\n$body2")
                // … y puedes parsear body2 con JSONObject, Moshi, Gson, etc. …

            } catch (e: java.io.EOFException) {
                Log.e("AEMET", "EOFException: respuesta truncada", e)
            } catch (e: Exception) {
                Log.e("AEMET", "Excepción al conectar a AEMET", e)
            } finally {
                response?.body?.close()
            }
        }
    }

}

