package com.example.climbingteam.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "climbing_favorites")

class FavoritesManager(private val context: Context) {
    private val gson = Gson()
    private val FAVORITES_KEY = stringPreferencesKey("favorites")

    val favorites: Flow<List<SavedLocation>> = context.dataStore.data.map { prefs ->
        val json = prefs[FAVORITES_KEY] ?: "[]"
        val type = object : TypeToken<List<SavedLocation>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun addFavorite(location: GeoLocation) {
        context.dataStore.edit { prefs ->
            val current = getCurrentList(prefs)
            if (current.none { it.id == location.id }) {
                val saved = SavedLocation(
                    id = location.id,
                    name = location.name,
                    displayName = location.displayName,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    elevation = location.elevation
                )
                val updated = current + saved
                prefs[FAVORITES_KEY] = gson.toJson(updated)
            }
        }
    }

    suspend fun removeFavorite(locationId: Long) {
        context.dataStore.edit { prefs ->
            val current = getCurrentList(prefs)
            val updated = current.filter { it.id != locationId }
            prefs[FAVORITES_KEY] = gson.toJson(updated)
        }
    }

    suspend fun isFavorite(locationId: Long): Boolean {
        var result = false
        context.dataStore.edit { prefs ->
            result = getCurrentList(prefs).any { it.id == locationId }
        }
        return result
    }

    private fun getCurrentList(prefs: Preferences): List<SavedLocation> {
        val json = prefs[FAVORITES_KEY] ?: "[]"
        val type = object : TypeToken<List<SavedLocation>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
