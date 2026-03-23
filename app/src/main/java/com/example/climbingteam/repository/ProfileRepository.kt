package com.example.climbingteam.repository

import android.content.Context
import android.net.Uri
import com.example.climbingteam.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object ProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun profilesCollection() = db.collection("profiles")

    fun currentUserId(): String? = auth.currentUser?.uid

    suspend fun getProfile(userId: String): UserProfile? {
        return try {
            val doc = profilesCollection().document(userId).get().await()
            if (doc.exists()) {
                UserProfile(
                    userId = doc.getString("userId") ?: userId,
                    email = doc.getString("email") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    favoriteSector = doc.getString("favoriteSector") ?: "",
                    favoriteRockType = doc.getString("favoriteRockType") ?: "",
                    maxClimbingGrade = doc.getString("maxClimbingGrade") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0
                )
            } else null
        } catch (e: Exception) {
            android.util.Log.e("ProfileRepo", "Error getting profile", e)
            null
        }
    }

    suspend fun getMyProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        val existing = getProfile(uid)
        if (existing != null) {
            // Always refresh review count
            val count = countUserReviews(uid)
            return if (count != existing.reviewCount) {
                val updated = existing.copy(reviewCount = count)
                saveProfile(updated)
                updated
            } else existing
        }
        // Create default profile
        val count = countUserReviews(uid)
        val defaultProfile = UserProfile(
            userId = uid,
            email = auth.currentUser?.email ?: "",
            reviewCount = count
        )
        saveProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun saveProfile(profile: UserProfile): Boolean {
        return try {
            val uid = auth.currentUser?.uid ?: return false
            profilesCollection().document(uid).set(profile.toMap()).await()
            true
        } catch (e: Exception) {
            android.util.Log.e("ProfileRepo", "Error saving profile", e)
            false
        }
    }

    /**
     * Copy image from picker URI to app internal storage and save path in Firestore.
     * This avoids needing Firebase Storage (which requires extra console setup).
     */
    suspend fun saveProfilePhoto(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val uid = auth.currentUser?.uid ?: return@withContext null

                // Create profile photos directory
                val photosDir = File(context.filesDir, "profile_photos")
                if (!photosDir.exists()) photosDir.mkdirs()

                // Delete old photo if exists
                photosDir.listFiles()?.filter { it.name.startsWith(uid) }?.forEach { it.delete() }

                // Copy image to internal storage
                val extension = "jpg"
                val photoFile = File(photosDir, "${uid}_${UUID.randomUUID()}.$extension")
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    photoFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext null

                val localPath = photoFile.absolutePath

                // Update Firestore with local path
                profilesCollection().document(uid).update("photoUrl", localPath).await()

                localPath
            } catch (e: Exception) {
                android.util.Log.e("ProfileRepo", "Error saving photo", e)
                null
            }
        }
    }

    /**
     * Count user's reviews from Firestore
     */
    suspend fun countUserReviews(userId: String): Int {
        return try {
            val snapshot = db.collection("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}
