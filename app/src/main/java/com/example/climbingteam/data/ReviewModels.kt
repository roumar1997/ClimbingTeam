package com.example.climbingteam.data

import com.google.firebase.Timestamp

data class ClimbingReview(
    val id: String = "",
    val locationId: Long = 0,
    val locationName: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val timestamp: Timestamp = Timestamp.now(),

    // Nombre del sector de escalada
    val sectorName: String = "",

    // Valoracion general (1-5 estrellas)
    val rating: Int = 0,

    // Tipo de roca
    val rockType: String = "",

    // Calidad de la roca (1-5): 1 = se rompe muy facil, 5 = muy solida
    val rockQuality: Int = 0,

    // Presencia de procesionaria (0 = no, 1 = poca, 2 = moderada, 3 = mucha)
    val processionary: Int = 0,

    // Niveles de dificultad disponibles
    val hasBeginnerRoutes: Boolean = false,
    val hasIntermediateRoutes: Boolean = false,
    val hasAdvancedRoutes: Boolean = false,
    val hasExpertRoutes: Boolean = false,

    // Comentario libre
    val comment: String = ""
) {
    // Firestore requires a no-arg constructor
    fun toMap(): Map<String, Any> = mapOf(
        "locationId" to locationId,
        "locationName" to locationName,
        "sectorName" to sectorName,
        "userId" to userId,
        "userEmail" to userEmail,
        "userName" to userName,
        "userPhotoUrl" to userPhotoUrl,
        "timestamp" to timestamp,
        "rating" to rating,
        "rockType" to rockType,
        "rockQuality" to rockQuality,
        "processionary" to processionary,
        "hasBeginnerRoutes" to hasBeginnerRoutes,
        "hasIntermediateRoutes" to hasIntermediateRoutes,
        "hasAdvancedRoutes" to hasAdvancedRoutes,
        "hasExpertRoutes" to hasExpertRoutes,
        "comment" to comment
    )

    val rockQualityLabel: String
        get() = when (rockQuality) {
            1 -> "Muy fr\u00e1gil"
            2 -> "Fr\u00e1gil"
            3 -> "Normal"
            4 -> "S\u00f3lida"
            5 -> "Muy s\u00f3lida"
            else -> "Sin valorar"
        }

    val processionaryLabel: String
        get() = when (processionary) {
            0 -> "No se ha visto"
            1 -> "Poca presencia"
            2 -> "Presencia moderada"
            3 -> "Abundante, precauci\u00f3n"
            else -> "Sin informaci\u00f3n"
        }

    val difficultyLevelsText: String
        get() {
            val levels = mutableListOf<String>()
            if (hasBeginnerRoutes) levels.add("Iniciaci\u00f3n")
            if (hasIntermediateRoutes) levels.add("Intermedio")
            if (hasAdvancedRoutes) levels.add("Avanzado")
            if (hasExpertRoutes) levels.add("Experto")
            return if (levels.isEmpty()) "Sin especificar" else levels.joinToString(" \u00b7 ")
        }
}

val ROCK_TYPES = listOf(
    "Caliza", "Granito", "Arenisca", "Conglomerado",
    "Basalto", "Gneis", "Cuarcita", "Pizarra", "Otro"
)
