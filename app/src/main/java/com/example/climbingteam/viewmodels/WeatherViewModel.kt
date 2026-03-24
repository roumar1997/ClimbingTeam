package com.example.climbingteam.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.climbingteam.api.OpenMeteoApi
import com.example.climbingteam.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application)

    // Search state for 3 slots
    private val _searchQuery = MutableStateFlow(arrayOf("", "", ""))
    val searchQuery: StateFlow<Array<String>> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(arrayOf<List<GeoLocation>>(emptyList(), emptyList(), emptyList()))
    val searchResults: StateFlow<Array<List<GeoLocation>>> = _searchResults.asStateFlow()

    private val _selectedLocations = MutableStateFlow(arrayOfNulls<GeoLocation>(3))
    val selectedLocations: StateFlow<Array<GeoLocation?>> = _selectedLocations.asStateFlow()

    // Weather data for 3 slots
    private val _weatherData = MutableStateFlow(arrayOfNulls<LocationWeather>(3))
    val weatherData: StateFlow<Array<LocationWeather?>> = _weatherData.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSearching = MutableStateFlow(booleanArrayOf(false, false, false))
    val isSearching: StateFlow<BooleanArray> = _isSearching.asStateFlow()

    // Best location index
    private val _bestLocationIndex = MutableStateFlow<Int?>(null)
    val bestLocationIndex: StateFlow<Int?> = _bestLocationIndex.asStateFlow()

    // Forecast days
    private val _forecastDays = MutableStateFlow(14)
    val forecastDays: StateFlow<Int> = _forecastDays.asStateFlow()

    // Favorites
    val favorites: StateFlow<List<SavedLocation>> = favoritesManager.favorites
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var searchJobs = arrayOfNulls<Job>(3)

    fun updateSearchQuery(slot: Int, query: String) {
        val current = _searchQuery.value.copyOf()
        current[slot] = query
        _searchQuery.value = current

        searchJobs[slot]?.cancel()
        if (query.length >= 2) {
            val searching = _isSearching.value.copyOf()
            searching[slot] = true
            _isSearching.value = searching

            searchJobs[slot] = viewModelScope.launch {
                val results = OpenMeteoApi.searchLocations(query)
                val currentResults = _searchResults.value.copyOf()
                currentResults[slot] = results
                _searchResults.value = currentResults

                val s = _isSearching.value.copyOf()
                s[slot] = false
                _isSearching.value = s
            }
        } else {
            val currentResults = _searchResults.value.copyOf()
            currentResults[slot] = emptyList()
            _searchResults.value = currentResults
        }
    }

    fun selectLocation(slot: Int, location: GeoLocation) {
        val current = _selectedLocations.value.copyOf()
        current[slot] = location
        _selectedLocations.value = current

        val query = _searchQuery.value.copyOf()
        query[slot] = location.displayName
        _searchQuery.value = query

        val results = _searchResults.value.copyOf()
        results[slot] = emptyList()
        _searchResults.value = results

        // Auto-fetch weather for this slot
        fetchWeatherForSlot(slot, location)
    }

    fun selectFromFavorite(slot: Int, saved: SavedLocation) {
        val geo = GeoLocation(
            id = saved.id,
            name = saved.name,
            latitude = saved.latitude,
            longitude = saved.longitude,
            elevation = saved.elevation,
            country = null,
            region = null,
            province = null,
            timezone = null
        )
        selectLocation(slot, geo)
    }

    fun clearSlot(slot: Int) {
        val locations = _selectedLocations.value.copyOf()
        locations[slot] = null
        _selectedLocations.value = locations

        val weather = _weatherData.value.copyOf()
        weather[slot] = null
        _weatherData.value = weather

        val query = _searchQuery.value.copyOf()
        query[slot] = ""
        _searchQuery.value = query

        updateBestLocation()
    }

    private fun fetchWeatherForSlot(slot: Int, location: GeoLocation) {
        viewModelScope.launch {
            _isLoading.value = true
            val forecast = OpenMeteoApi.getForecast(
                location.latitude,
                location.longitude,
                _forecastDays.value
            )
            if (forecast != null) {
                val hourly = OpenMeteoApi.parseHourlyData(forecast.hourly)
                val daily = OpenMeteoApi.parseDailyData(forecast.daily)
                val condition = OpenMeteoApi.evaluateClimbingCondition(forecast.current)

                val locationWeather = LocationWeather(
                    location = location,
                    current = forecast.current,
                    hourlyForecast = hourly,
                    dailyForecast = daily,
                    elevation = forecast.elevation ?: location.elevation,
                    climbingCondition = condition
                )

                val weather = _weatherData.value.copyOf()
                weather[slot] = locationWeather
                _weatherData.value = weather

                updateBestLocation()
            }
            _isLoading.value = _weatherData.value.any { it == null } &&
                    _selectedLocations.value.any { it != null && _weatherData.value[_selectedLocations.value.indexOf(it)] == null }
            _isLoading.value = false
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedLocations.value.forEachIndexed { i, loc ->
                if (loc != null) {
                    fetchWeatherForSlot(i, loc)
                }
            }
            _isLoading.value = false
        }
    }

    fun setForecastDays(days: Int) {
        _forecastDays.value = days
        refreshAll()
    }

    private fun updateBestLocation() {
        val data = _weatherData.value
        val validIndices = data.indices.filter { data[it] != null }
        if (validIndices.size < 2) {
            _bestLocationIndex.value = null
            return
        }

        _bestLocationIndex.value = validIndices.maxByOrNull { idx ->
            val w = data[idx]!!
            val current = w.current ?: return@maxByOrNull -100
            var score = 0
            val temp = current.temperature ?: 15.0
            val wind = current.windSpeed ?: 0.0
            val precip = current.precipitation ?: 0.0
            val humidity = current.humidity ?: 50.0

            // Temperature ideal: 12-22
            score += when {
                temp in 12.0..22.0 -> 30
                temp in 8.0..28.0 -> 20
                temp in 5.0..32.0 -> 10
                else -> 0
            }
            // Low wind
            score += when {
                wind < 10 -> 25
                wind < 20 -> 15
                wind < 30 -> 5
                else -> 0
            }
            // No rain
            score += when {
                precip == 0.0 -> 25
                precip < 1 -> 15
                precip < 3 -> 5
                else -> 0
            }
            // Humidity
            score += when {
                humidity in 30.0..70.0 -> 20
                humidity in 20.0..80.0 -> 10
                else -> 0
            }
            score
        }
    }

    // Favorites
    fun toggleFavorite(location: GeoLocation) {
        viewModelScope.launch {
            val isFav = favoritesManager.isFavorite(location.id)
            if (isFav) {
                favoritesManager.removeFavorite(location.id)
            } else {
                favoritesManager.addFavorite(location)
            }
        }
    }

    fun removeFavorite(locationId: Long) {
        viewModelScope.launch {
            favoritesManager.removeFavorite(locationId)
        }
    }
}
