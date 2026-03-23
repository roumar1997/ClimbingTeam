package com.example.climbingteam.repository

import com.example.climbingteam.data.ClimbingReview
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ReviewRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun reviewsCollection() = db.collection("reviews")

    suspend fun getReviewsForLocation(locationId: Long): List<ClimbingReview> {
        return try {
            // Simple query without orderBy to avoid needing a composite index
            val snapshot = reviewsCollection()
                .whereEqualTo("locationId", locationId)
                .limit(50)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    ClimbingReview(
                        id = doc.id,
                        locationId = doc.getLong("locationId") ?: 0,
                        locationName = doc.getString("locationName") ?: "",
                        sectorName = doc.getString("sectorName") ?: "",
                        userId = doc.getString("userId") ?: "",
                        userEmail = doc.getString("userEmail") ?: "",
                        userName = doc.getString("userName") ?: "",
                        userPhotoUrl = doc.getString("userPhotoUrl") ?: "",
                        timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        rockType = doc.getString("rockType") ?: "",
                        rockQuality = doc.getLong("rockQuality")?.toInt() ?: 0,
                        processionary = doc.getLong("processionary")?.toInt() ?: 0,
                        hasBeginnerRoutes = doc.getBoolean("hasBeginnerRoutes") ?: false,
                        hasIntermediateRoutes = doc.getBoolean("hasIntermediateRoutes") ?: false,
                        hasAdvancedRoutes = doc.getBoolean("hasAdvancedRoutes") ?: false,
                        hasExpertRoutes = doc.getBoolean("hasExpertRoutes") ?: false,
                        comment = doc.getString("comment") ?: ""
                    )
                } catch (e: Exception) { null }
            }.sortedByDescending { it.timestamp.toDate() } // Sort in memory instead
        } catch (e: Exception) {
            android.util.Log.e("ReviewRepo", "Error loading reviews", e)
            emptyList()
        }
    }

    suspend fun submitReview(review: ClimbingReview): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            // Get user profile to attach name and photo
            val profile = ProfileRepository.getProfile(user.uid)
            val data = review.copy(
                userId = user.uid,
                userEmail = user.email ?: "Anónimo",
                userName = profile?.displayName ?: user.email?.substringBefore("@") ?: "Anónimo",
                userPhotoUrl = profile?.photoUrl ?: "",
                timestamp = Timestamp.now()
            ).toMap()
            reviewsCollection().add(data).await()
            true
        } catch (e: Exception) {
            android.util.Log.e("ReviewRepo", "Error submitting review", e)
            false
        }
    }

    suspend fun deleteReview(reviewId: String): Boolean {
        return try {
            reviewsCollection().document(reviewId).delete().await()
            true
        } catch (e: Exception) { false }
    }

    fun currentUserId(): String? = auth.currentUser?.uid
}
