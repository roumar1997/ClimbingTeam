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

    // Sector preview (used when tapping a sector card)
    private val _sectorPreview = MutableStateFlow<LocationWeather?>(null)
    val sectorPreview: StateFlow<LocationWeather?> = _sectorPreview.asStateFlow()

    fun loadSectorPreview(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            _sectorPreview.value = null   // reset to show loading
            val geo = GeoLocation(
                id = 0L, name = name,
                latitude = lat, longitude = lon,
                elevation = null, country = null,
                region = null, province = null, timezone = null
            )
            val forecast = OpenMeteoApi.getForecast(lat, lon, 7)
            if (forecast != null) {
                val hourly = OpenMeteoApi.parseHourlyData(forecast.hourly)
                val daily  = OpenMeteoApi.parseDailyData(forecast.daily)
                val condition = OpenMeteoApi.evaluateClimbingCondition(forecast.current)
                _sectorPreview.value = LocationWeather(
                    location  = geo,
                    current   = forecast.current,
                    hourlyForecast = hourly,
                    dailyForecast  = daily,
                    elevation = forecast.elevation ?: geo.elevation,
                    climbingCondition = condition
                )
            }
        }
    }

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

    // ── Climbing condition scoring ─────────────────────────────

    /** Calculates approximate dew point spread (temp - dewpoint) from temp and relative humidity.
     *  Formula: dewpoint ≈ temp - ((100 - RH) / 5)  → spread = (100 - RH) / 5
     *  > 6°C = rock very dry, 4–6°C = OK, 2–4°C = borderline, < 2°C = damp rock */
    private fun dewSpread(temp: Double, humidity: Double): Double = (100.0 - humidity) / 5.0

    /** Weather codes that mean active rain/precipitation */
    private fun isRainingCode(code: Int?): Boolean = code in setOf(
        51, 53, 55, 56, 57,          // drizzle
        61, 63, 65, 66, 67,          // rain
        71, 73, 75, 77,              // snow
        80, 81, 82, 85, 86           // showers
    )

    /** Weather codes that mean thunderstorm → hard discard */
    private fun isThunderstormCode(code: Int?): Boolean = (code ?: 0) >= 95

    /**
     * Returns 5 bar-chart scores (each 0–100) for the bar chart:
     * [0] Temp, [1] Viento, [2] Lluvia, [3] Humedad, [4] Rocío (dewSpread)
     * These use STRICT climbing criteria.
     */
    fun getClimbingScores(weather: LocationWeather): List<Int> {
        val c = weather.current ?: return listOf(0, 0, 0, 0, 0)
        val temp     = c.temperature ?: 15.0
        val wind     = c.windSpeed   ?: 0.0
        val precip   = c.precipitation ?: 0.0
        val humidity = c.humidity    ?: 50.0
        val code     = c.weatherCode

        // Hard discard: thunderstorm or heavy rain → everything 0
        if (isThunderstormCode(code)) return listOf(0, 0, 0, 0, 0)

        // Temp: weighted average of the 3 disciplines' ideal ranges
        // Boulder 4-10, Vía 8-16, Larga 10-18 → combined sweet spot ~8-14
        val tempScore = when {
            temp in 8.0..14.0  -> 100
            temp in 4.0..18.0  -> 75
            temp in 0.0..22.0  -> 45
            temp in -5.0..28.0 -> 20
            else               -> 0
        }

        // Wind: strictest discipline is boulder (<12 km/h)
        val windScore = when {
            wind <= 6   -> 100
            wind <= 12  -> 80
            wind <= 15  -> 60
            wind <= 20  -> 35
            wind <= 30  -> 10
            else        -> 0
        }

        // Rain: any precipitation is a strong negative signal for rock condition
        val rainScore = when {
            precip == 0.0 && !isRainingCode(code) -> 100
            precip == 0.0                          -> 60   // code says rain but meter shows 0
            precip < 0.3                           -> 30
            precip < 1.0                           -> 10
            else                                   -> 0
        }

        // Humidity: ideal 35-60% for all disciplines
        val humidityScore = when {
            humidity in 35.0..60.0 -> 100
            humidity in 25.0..65.0 -> 70
            humidity in 20.0..75.0 -> 40
            humidity in 15.0..85.0 -> 15
            else                   -> 0
        }

        // DewSpread: how dry the rock surface feels
        val dew = dewSpread(temp, humidity)
        val dewScore = when {
            dew > 8  -> 100
            dew > 6  -> 85
            dew > 4  -> 60   // minimum acceptable
            dew > 2  -> 25
            else     -> 0
        }

        return listOf(tempScore, windScore, rainScore, humidityScore, dewScore)
    }

    /**
     * Returns per-discipline scores: [boulder, via, viaLarga] each 0–100.
     * Based on real climbing criteria:
     *  Boulder:   4-10°C · 35-60% HR · <12 km/h · dewSpread>4
     *  Vía:       8-16°C · 35-60% HR · <15 km/h · dewSpread>4
     *  Vía Larga: 10-18°C · 30-65% HR · <20 km/h · no tormenta
     */
    fun getDisciplineScores(weather: LocationWeather): Triple<Int, Int, Int> {
        val c = weather.current ?: return Triple(0, 0, 0)
        val temp     = c.temperature   ?: 15.0
        val wind     = c.windSpeed     ?: 0.0
        val precip   = c.precipitation ?: 0.0
        val humidity = c.humidity      ?: 50.0
        val code     = c.weatherCode

        // Hard discard conditions
        if (isThunderstormCode(code)) return Triple(0, 0, 0)
        val raining = isRainingCode(code) || precip >= 1.0
        if (raining) return Triple(0, 0, 0)

        val dew = dewSpread(temp, humidity)
        val goodDew = dew > 4.0   // rock surface dry enough
        val lightPrecip = precip in 0.01..0.99  // light drizzle → reduce score

        fun score(
            tempIdeal: ClosedFloatingPointRange<Double>,
            tempOk: ClosedFloatingPointRange<Double>,
            hrIdeal: ClosedFloatingPointRange<Double>,
            hrOk: ClosedFloatingPointRange<Double>,
            windMax: Double
        ): Int {
            // Temperature (40 pts)
            val t = when {
                temp in tempIdeal -> 40
                temp in tempOk    -> 24
                else              -> 0
            }
            // Humidity (25 pts)
            val h = when {
                humidity in hrIdeal -> 25
                humidity in hrOk    -> 14
                else                -> 0
            }
            // Wind (20 pts)
            val w = when {
                wind <= windMax * 0.4  -> 20
                wind <= windMax        -> 13
                wind <= windMax * 1.3  -> 5
                else                   -> 0
            }
            // DewSpread (15 pts)
            val d = when {
                dew > 6 -> 15
                dew > 4 -> 10
                dew > 2 -> 4
                else    -> 0
            }
            val base = t + h + w + d
            // Light drizzle: -30%
            return if (lightPrecip) (base * 0.70).toInt() else base
        }

        val boulder  = score(4.0..10.0, 0.0..14.0,  35.0..60.0, 25.0..70.0, 12.0)
        val via      = score(8.0..16.0, 4.0..20.0,  35.0..60.0, 25.0..72.0, 15.0)
        val viaLarga = score(10.0..18.0, 5.0..24.0, 30.0..65.0, 20.0..75.0, 20.0)

        return Triple(boulder, via, viaLarga)
    }

    /** Overall best score = best of the 3 disciplines */
    fun getTotalScore(weather: LocationWeather): Int {
        val (b, v, l) = getDisciplineScores(weather)
        return maxOf(b, v, l)
    }

    private fun updateBestLocation() {
        val data = _weatherData.value
        val validIndices = data.indices.filter { data[it] != null }
        if (validIndices.size < 2) {
            _bestLocationIndex.value = null
            return
        }
        _bestLocationIndex.value = validIndices.maxByOrNull { idx ->
            getTotalScore(data[idx]!!)
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
