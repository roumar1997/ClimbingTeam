package com.example.climbingteam.repository

import android.net.Uri
import com.example.climbingteam.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object ProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
        if (existing != null) return existing
        // Create default profile
        val defaultProfile = UserProfile(
            userId = uid,
            email = auth.currentUser?.email ?: ""
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

    suspend fun uploadProfilePhoto(imageUri: Uri): String? {
        return try {
            val uid = auth.currentUser?.uid ?: return null
            val ref = storage.reference.child("profiles/$uid/avatar.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            // Update photo URL in profile doc
            profilesCollection().document(uid).update("photoUrl", url).await()
            url
        } catch (e: Exception) {
            android.util.Log.e("ProfileRepo", "Error uploading photo", e)
            null
        }
    }
}
