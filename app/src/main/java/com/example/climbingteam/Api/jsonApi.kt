package com.example.climbingteam.Api

import android.content.Context
import com.google.gson.Gson
import java.io.File


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

}