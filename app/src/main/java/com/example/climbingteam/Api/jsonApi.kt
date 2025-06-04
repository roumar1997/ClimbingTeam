// src/main/kotlin/com/example/climbingteam/Api/jsonApi.kt
package com.example.climbingteam.Api

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.EOFException

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
    private val gson = Gson()
    private var json = ""
    lateinit var estaciones: FeatureCollection

    /** Carga el GeoJSON de estaciones desde “assets/Estaciones_Completas.geojson” */
    fun initData(context: Context) {
        json = context.assets.open("Estaciones_Completas.geojson")
            .bufferedReader()
            .use { it.readText() }
        estaciones = gson.fromJson(json, FeatureCollection::class.java)
    }

    /** Convierte Date a formato “yyyy-MM-dd'T'HH:mm:ss'UTC'” */
    fun formatDateToUTCString(date: java.util.Date): String {
        val utcDateTime = date.toInstant().atOffset(java.time.ZoneOffset.UTC)
        val formatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'UTC'")
        return utcDateTime.format(formatter)
    }

    /**
     * Recupera la última observación convencional de AEMET para “idema” (INDICATIVO).
     * → Si la primera llamada devuelve 404 o `datos` = null, se reintenta una vez tras 500 ms.
     * → Devuelve `ObservacionEstacion?` o `null` en caso de error definitivo.
     */
    suspend fun consultarObservacionConvencional(
        idema: String,
        apiKey: String
    ): ObservacionEstacion? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val urlMeta =
            "https://opendata.aemet.es/opendata/api/observacion/convencional/datos/estacion/$idema?api_key=$apiKey"
        Log.d("AEMET_URL_META", urlMeta)

        // Intentar hasta 2 veces (0 o 1 reintento tras 500 ms) antes de dar null:
        repeat(2) { intento ->
            try {
                // === Paso 1: pedimos metadatos ===
                val requestMeta = Request.Builder()
                    .url(urlMeta)
                    .get()
                    .build()

                val metaResponse: Response = client.newCall(requestMeta).execute()
                if (metaResponse.code != 200) {
                    Log.e("AEMET", "Intento $intento: error en petición meta: HTTP ${metaResponse.code}")
                    metaResponse.close()
                    if (intento == 0) {
                        Thread.sleep(500)
                        return@repeat
                    } else {
                        return@withContext null
                    }
                }

                val bodyMeta = metaResponse.body?.string().orEmpty()
                metaResponse.close()
                if (bodyMeta.isBlank()) {
                    Log.e("AEMET", "Intento $intento: respuesta meta vacía")
                    if (intento == 0) {
                        Thread.sleep(500)
                        return@repeat
                    } else {
                        return@withContext null
                    }
                }

                // Parseamos {estado, datos} solamente
                val metaObj = gson.fromJson(bodyMeta, MetaResponse::class.java)
                if (metaObj.estado != 200 || metaObj.datos.isNullOrBlank()) {
                    Log.e("AEMET", "Intento $intento: estado != 200 o 'datos' vacío (estado=${metaObj.estado})")
                    if (intento == 0) {
                        Thread.sleep(500)
                        return@repeat
                    } else {
                        return@withContext null
                    }
                }

                // === Paso 2: pedimos JSON real de observaciones ===
                val urlDatos = metaObj.datos!!
                Log.d("AEMET_URL_DATOS", urlDatos)
                val requestDatos = Request.Builder()
                    .url(urlDatos)
                    .get()
                    .build()

                // Intento inicial
                val dataResponse: Response = try {
                    client.newCall(requestDatos).execute()
                } catch (e: EOFException) {
                    Log.w("AEMET", "Intento $intento: EOFException al leer datos. Reintentando en 500 ms…")
                    Thread.sleep(500)
                    client.newCall(requestDatos).execute()
                }

                if (dataResponse.code != 200) {
                    Log.e("AEMET", "Intento $intento: error en petición datos finales: HTTP ${dataResponse.code}")
                    dataResponse.close()
                    if (intento == 0) {
                        Thread.sleep(500)
                        return@repeat
                    } else {
                        return@withContext null
                    }
                }

                val bodyDatos = dataResponse.body?.string().orEmpty()
                dataResponse.close()
                if (bodyDatos.isBlank()) {
                    Log.e("AEMET", "Intento $intento: cuerpo de datos final vacío")
                    if (intento == 0) {
                        Thread.sleep(500)
                        return@repeat
                    } else {
                        return@withContext null
                    }
                }

                // bodyDatos = ARRAY JSON de ObservacionEstacion[]
                val listType = com.google.gson.reflect.TypeToken
                    .getParameterized(List::class.java, ObservacionEstacion::class.java)
                    .type

                val listaObservaciones: List<ObservacionEstacion> =
                    gson.fromJson(bodyDatos, listType)

                if (listaObservaciones.isEmpty()) {
                    Log.w("AEMET", "Intento $intento: lista de observaciones vacía")
                    if (intento == 0) {
                        Thread.sleep(500)
                        return@repeat
                    } else {
                        return@withContext null
                    }
                }

                // Devolvemos la primera (más reciente) y salimos
                return@withContext listaObservaciones.first()

            } catch (e: Exception) {
                Log.e("AEMET", "Intento $intento: excepción conectando a AEMET", e)
                if (intento == 0) {
                    Thread.sleep(500)
                    return@repeat
                } else {
                    return@withContext null
                }
            }
        }

        // Si falló ambos intentos:
        null
    }

    private data class MetaResponse(
        val descripcion: String?,
        val estado: Int,
        val metadatos: String?,
        val datos: String?
    )
}
