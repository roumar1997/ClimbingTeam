package com.example.climbingteam.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.climbingteam.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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
            Log.e("ProfileRepo", "Error getting profile", e)
            null
        }
    }

    suspend fun getMyProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        val existing = getProfile(uid)
        if (existing != null) {
            val count = countUserReviews(uid)
            return if (count != existing.reviewCount) {
                val updated = existing.copy(reviewCount = count)
                saveProfile(updated)
                updated
            } else existing
        }
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
            Log.e("ProfileRepo", "Error saving profile", e)
            false
        }
    }

    /**
     * Compresses the photo to 200×200px JPEG and stores it as a base64 data URI
     * directly in Firestore — free, no Firebase Storage needed, visible to all users.
     */
    suspend fun saveProfilePhoto(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val uid = auth.currentUser?.uid ?: return@withContext null

                // 1. Decode original bitmap
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext null
                val original = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (original == null) return@withContext null

                // 2. Scale down to max 200×200 keeping aspect ratio
                val maxSize = 200
                val scale = maxSize.toFloat() / maxOf(original.width, original.height).toFloat()
                val newW = (original.width * scale).toInt().coerceAtLeast(1)
                val newH = (original.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(original, newW, newH, true)
                original.recycle()

                // 3. Compress to JPEG quality 65 → roughly 8–20KB
                val output = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 65, output)
                scaled.recycle()

                // 4. Encode to base64 data URI
                val bytes = output.toByteArray()
                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val dataUri = "data:image/jpeg;base64,$b64"

                // 5. Save to Firestore (free, visible to all users)
                profilesCollection().document(uid).update("photoUrl", dataUri).await()

                dataUri
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Error saving photo", e)
                null
            }
        }
    }

    /**
     * Search users by display name (case-insensitive prefix match).
     * Firestore doesn't support true full-text search, so we fetch all profiles
     * and filter locally. For a small user base this is fine.
     */
    suspend fun searchUsers(query: String): List<UserProfile> {
        if (query.length < 2) return emptyList()
        val myId = currentUserId()
        return try {
            val snapshot = profilesCollection().get().await()
            val q = query.lowercase()
            snapshot.documents.mapNotNull { doc ->
                val uid = doc.getString("userId") ?: doc.id
                if (uid == myId) return@mapNotNull null
                val name = doc.getString("displayName") ?: ""
                val email = doc.getString("email") ?: ""
                val matchesName = name.lowercase().contains(q)
                val matchesEmail = email.lowercase().contains(q)
                if (!matchesName && !matchesEmail) return@mapNotNull null
                UserProfile(
                    userId = uid,
                    email = email,
                    displayName = name,
                    favoriteSector = doc.getString("favoriteSector") ?: "",
                    favoriteRockType = doc.getString("favoriteRockType") ?: "",
                    maxClimbingGrade = doc.getString("maxClimbingGrade") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error searching users", e)
            emptyList()
        }
    }

    suspend fun countUserReviews(userId: String): Int {
        return try {
            db.collection("reviews").whereEqualTo("userId", userId).get().await().size()
        } catch (_: Exception) { 0 }
    }
}
