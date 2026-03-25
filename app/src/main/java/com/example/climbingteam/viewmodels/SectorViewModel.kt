package com.example.climbingteam.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.climbingteam.api.OpenMeteoApi
import com.example.climbingteam.data.*
import com.example.climbingteam.repository.SectorRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class DisciplineSort(val label: String, val icon: String) {
    GENERAL("General",   "🎯"),
    BOULDER("Boulder",   "🧗"),
    VIA("Vía",           "🪨"),
    VIA_LARGA("Larga",   "🏔")
}

class SectorViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _allSectors = MutableStateFlow<List<Sector>>(emptyList())

    private val _results = MutableStateFlow<List<SectorResult>>(emptyList())
    val results: StateFlow<List<SectorResult>> = _results.asStateFlow()

    private val _selectedSector = MutableStateFlow<SectorResult?>(null)
    val selectedSector: StateFlow<SectorResult?> = _selectedSector.asStateFlow()

    fun selectSector(result: SectorResult) { _selectedSector.value = result }
    fun clearSelectedSector() { _selectedSector.value = null }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _loadingTotal = MutableStateFlow(0)
    val loadingTotal: StateFlow<Int> = _loadingTotal.asStateFlow()

    // Filter state
    private val _selectedRocas = MutableStateFlow<Set<String>>(emptySet())
    val selectedRocas: StateFlow<Set<String>> = _selectedRocas.asStateFlow()

    private val _selectedEstilos = MutableStateFlow<Set<String>>(emptySet())
    val selectedEstilos: StateFlow<Set<String>> = _selectedEstilos.asStateFlow()

    private val _maxDistanceKm = MutableStateFlow<Double?>(null)
    val maxDistanceKm: StateFlow<Double?> = _maxDistanceKm.asStateFlow()

    private val _forecastDays = MutableStateFlow(7)
    val forecastDays: StateFlow<Int> = _forecastDays.asStateFlow()

    private val _conditionFilter = MutableStateFlow<ClimbingCondition?>(null)
    val conditionFilter: StateFlow<ClimbingCondition?> = _conditionFilter.asStateFlow()

    private val _disciplineSort = MutableStateFlow(DisciplineSort.GENERAL)
    val disciplineSort: StateFlow<DisciplineSort> = _disciplineSort.asStateFlow()

    /** Sorted view: sectors with weather sorted by selected discipline score, rest at bottom */
    val sortedResults: StateFlow<List<SectorResult>> = combine(_results, _disciplineSort) { list, disc ->
        val withWeather    = list.filter { it.dailyForecast.isNotEmpty() }
            .sortedByDescending { getBestDisciplineScore(it, disc) }
        val withoutWeather = list.filter { it.dailyForecast.isEmpty() }
        withWeather + withoutWeather
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // User location
    private val _userLat = MutableStateFlow<Double?>(null)
    private val _userLon = MutableStateFlow<Double?>(null)
    val userLat: StateFlow<Double?> = _userLat.asStateFlow()
    val userLon: StateFlow<Double?> = _userLon.asStateFlow()

    val availableRocas = listOf(
        "Caliza", "Granito", "Arenisca", "Conglomerado",
        "Cuarcita", "Volcánica", "Basalto", "Pizarra", "Otra / muro barrenado"
    )
    val availableEstilos = listOf("Vía", "Bloque", "Mixta", "Bloque y vía")

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            _allSectors.value = SectorRepository.loadSectors(context)
            search()
        }
    }

    fun setUserLocation(lat: Double, lon: Double) {
        _userLat.value = lat
        _userLon.value = lon
        search()
    }

    fun toggleRoca(roca: String) {
        val current = _selectedRocas.value.toMutableSet()
        if (roca in current) current.remove(roca) else current.add(roca)
        _selectedRocas.value = current
        search()
    }

    fun toggleEstilo(estilo: String) {
        val current = _selectedEstilos.value.toMutableSet()
        if (estilo in current) current.remove(estilo) else current.add(estilo)
        _selectedEstilos.value = current
        search()
    }

    fun setMaxDistance(km: Double?) {
        _maxDistanceKm.value = km
        search()
    }

    fun setForecastDays(days: Int) {
        _forecastDays.value = days
        search()
    }

    fun setConditionFilter(condition: ClimbingCondition?) {
        _conditionFilter.value = condition
        search()
    }

    fun setDisciplineSort(discipline: DisciplineSort) {
        _disciplineSort.value = discipline
    }

    // ── Discipline scoring from daily forecast data ──────────────────────────

    /** Best single-day discipline score across the forecast window (0–100). */
    fun getBestDisciplineScore(result: SectorResult, discipline: DisciplineSort): Int {
        if (result.dailyForecast.isEmpty()) return 0
        return result.dailyForecast.maxOfOrNull { getDisciplineScoreFromDaily(it, discipline) } ?: 0
    }

    /** Score a single daily forecast point for a given discipline (0–100). */
    fun getDisciplineScoreFromDaily(day: DailyPoint, discipline: DisciplineSort): Int {
        val tempMax  = day.tempMax  ?: 15.0
        val tempMin  = day.tempMin  ?: (tempMax - 6.0)
        val tempAvg  = (tempMax + tempMin) / 2.0
        val wind     = day.windSpeedMax ?: 0.0
        val precip   = day.precipSum    ?: 0.0
        val code     = day.weatherCode  ?: 0

        // Hard discard: thunderstorm or heavy rain
        if (code >= 95) return 0
        val rainCodes = setOf(61,63,65,66,67,80,81,82,85,86)
        if (precip >= 1.0 || code in rainCodes) return 0

        val lightPrecip = precip in 0.01..0.99

        fun calc(
            tempIdeal: ClosedFloatingPointRange<Double>,
            tempOk: ClosedFloatingPointRange<Double>,
            windMax: Double
        ): Int {
            val t = when {
                tempAvg in tempIdeal -> 40
                tempAvg in tempOk    -> 22
                else                 -> 0
            }
            val w = when {
                wind <= windMax * 0.4  -> 30
                wind <= windMax        -> 18
                wind <= windMax * 1.3  -> 7
                else                   -> 0
            }
            // Rain / sky (30 pts)
            val r = when {
                precip == 0.0 && code < 3  -> 30   // clear
                precip == 0.0 && code < 51 -> 25   // cloudy but dry
                precip == 0.0              -> 15   // dry but bad code
                else                       -> 5    // light drizzle
            }
            val base = t + w + r
            return if (lightPrecip) (base * 0.65).toInt() else base
        }

        return when (discipline) {
            DisciplineSort.BOULDER   -> calc(4.0..10.0,  0.0..14.0,  12.0)
            DisciplineSort.VIA       -> calc(8.0..16.0,  4.0..22.0,  15.0)
            DisciplineSort.VIA_LARGA -> calc(10.0..18.0, 5.0..25.0,  20.0)
            DisciplineSort.GENERAL   -> when (evaluateConditionFromDaily(day)) {
                ClimbingCondition.OPTIMO   -> 85
                ClimbingCondition.ACEPTABLE -> 50
                ClimbingCondition.ADVERSO  ->  5
            }
        }
    }

    fun clearFilters() {
        _selectedRocas.value = emptySet()
        _selectedEstilos.value = emptySet()
        _maxDistanceKm.value = null
        _conditionFilter.value = null
        search()
    }

    fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _loadingProgress.value = 0

            val filtered = SectorRepository.filterSectors(
                all = _allSectors.value,
                rocaFilter = _selectedRocas.value,
                estiloFilter = _selectedEstilos.value,
                userLat = _userLat.value,
                userLon = _userLon.value,
                maxDistanceKm = _maxDistanceKm.value
            )

            // Sort: sectors with GPS first, then by distance if location available
            val sorted = if (_userLat.value != null) {
                filtered.sortedWith(
                    compareBy(
                        { it.lat == null },  // GPS sectors first
                        { SectorRepository.distanceKm(_userLat.value, _userLon.value, it) ?: Double.MAX_VALUE }
                    )
                )
            } else {
                filtered.sortedBy { it.lat == null }
            }

            val withGps = sorted.filter { it.lat != null && it.lon != null }.take(20)
            val withoutGps = sorted.filter { it.lat == null || it.lon == null }

            val days = _forecastDays.value
            _loadingTotal.value = withGps.size

            // Fetch weather concurrently for sectors with GPS
            val weatherResults = withGps.mapIndexed { idx, sector ->
                async(Dispatchers.IO) {
                    val forecast = OpenMeteoApi.getForecast(sector.lat!!, sector.lon!!, 14)
                    val daily = OpenMeteoApi.parseDailyData(forecast?.daily).take(days)
                    val conditions = daily.map { evaluateConditionFromDaily(it) }
                    val best = conditions.minByOrNull { it.ordinal } ?: ClimbingCondition.ADVERSO
                    val dist = SectorRepository.distanceKm(_userLat.value, _userLon.value, sector)
                    _loadingProgress.value = idx + 1
                    SectorResult(
                        sector = sector,
                        distanceKm = dist,
                        dailyForecast = daily,
                        conditions = conditions,
                        bestCondition = best
                    )
                }
            }.awaitAll()

            // Sectors without GPS – no weather data
            val noWeatherResults = withoutGps.map { sector ->
                val dist = SectorRepository.distanceKm(_userLat.value, _userLon.value, sector)
                SectorResult(
                    sector = sector,
                    distanceKm = dist,
                    dailyForecast = emptyList(),
                    conditions = emptyList(),
                    bestCondition = ClimbingCondition.ADVERSO
                )
            }

            var allResults = weatherResults + noWeatherResults

            // Apply condition filter (only to sectors that have weather)
            val condFilter = _conditionFilter.value
            if (condFilter != null) {
                allResults = allResults.filter { result ->
                    // Sectors without weather data are excluded when a condition filter is active
                    if (result.dailyForecast.isEmpty()) return@filter false
                    when (condFilter) {
                        ClimbingCondition.OPTIMO ->
                            result.conditions.any { it == ClimbingCondition.OPTIMO }
                        ClimbingCondition.ACEPTABLE ->
                            result.conditions.any {
                                it == ClimbingCondition.OPTIMO || it == ClimbingCondition.ACEPTABLE
                            }
                        ClimbingCondition.ADVERSO -> true
                    }
                }
            }

            _results.value = allResults
            _isLoading.value = false
        }
    }

    private fun evaluateConditionFromDaily(day: DailyPoint): ClimbingCondition {
        val tempMax = day.tempMax ?: 15.0
        val wind = day.windSpeedMax ?: 0.0
        val precip = day.precipSum ?: 0.0
        val precipProb = day.precipProbMax ?: 0.0
        val code = day.weatherCode ?: 0

        // Rain → always adverso
        if (precip > 1.5) return ClimbingCondition.ADVERSO
        if (code in 61..67 || code in 80..82 || code in 95..99) return ClimbingCondition.ADVERSO

        var score = 100

        // Temperature: cool-to-mild is ideal for climbing (5–20°C)
        score += when {
            tempMax < 0   -> -30
            tempMax < 3   -> -15
            tempMax < 5   -> -5
            tempMax <= 20 -> 0      // optimal range
            tempMax <= 25 -> -8
            tempMax <= 30 -> -22
            else          -> -38
        }

        // Wind: only penalize above 50 km/h (below that, ventoso but fine)
        score += when {
            wind > 70 -> -35
            wind > 50 -> -20
            else      -> 0
        }

        // Light rain / drizzle
        score += when {
            precip > 0.5 -> -30
            precip > 0.0 -> -15
            else         -> 0
        }

        // Precipitation probability as humidity proxy
        score += when {
            precipProb > 80 -> -20
            precipProb > 60 -> -12
            precipProb > 40 -> -5
            else            -> 0
        }

        // Weather codes: snow, drizzle, fog (humidity indicator)
        score += when (code) {
            in 71..77 -> -25    // snow
            in 51..57 -> -18    // drizzle
            45, 48    -> -12    // fog = high humidity
            3         -> -3     // overcast
            else      -> 0
        }

        return when {
            score >= 70 -> ClimbingCondition.OPTIMO
            score >= 45 -> ClimbingCondition.ACEPTABLE
            else        -> ClimbingCondition.ADVERSO
        }
    }
}
