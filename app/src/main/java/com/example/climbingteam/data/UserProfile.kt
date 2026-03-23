package com.example.climbingteam.data

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val favoriteSector: String = "",
    val favoriteRockType: String = "",
    val maxClimbingGrade: String = "",
    val photoUrl: String = "",
    val reviewCount: Int = 0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "email" to email,
        "displayName" to displayName,
        "favoriteSector" to favoriteSector,
        "favoriteRockType" to favoriteRockType,
        "maxClimbingGrade" to maxClimbingGrade,
        "photoUrl" to photoUrl,
        "reviewCount" to reviewCount
    )
}

val CLIMBING_GRADES = listOf(
    "3", "4a", "4b", "4c",
    "5a", "5b", "5c",
    "6a", "6a+", "6b", "6b+", "6c", "6c+",
    "7a", "7a+", "7b", "7b+", "7c", "7c+",
    "8a", "8a+", "8b", "8b+", "8c", "8c+",
    "9a", "9a+", "9b", "9b+", "9c"
)
