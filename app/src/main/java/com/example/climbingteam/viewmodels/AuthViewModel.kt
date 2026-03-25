package com.example.climbingteam.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climbingteam.repository.ConsultaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    // Extra state for the login screen flow
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(
        email: String,
        password: String,
        onSucces: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser
                if (user?.isEmailVerified == true) {
                    _user.value = user
                    _authState.value = AuthState.Idle
                    onSucces()
                } else {
                    // Signed in but email not verified – sign out and inform user
                    auth.signOut()
                    _authState.value = AuthState.NeedsVerification(email)
                    onError("Debes verificar tu email antes de entrar.\nRevisa tu bandeja de entrada.")
                }
            }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Error al iniciar sesión")
            }
    }

    fun register(
        email: String,
        password: String,
        onSucces: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                // Send verification email before granting access
                result.user?.sendEmailVerification()
                    ?.addOnSuccessListener {
                        auth.signOut() // Don't log in until verified
                        _authState.value = AuthState.VerificationSent(email)
                        onSucces() // caller shows "check your email" message
                        ConsultaRepository.CreateUser(email)
                    }
                    ?.addOnFailureListener {
                        // Verification email failed – still let user in
                        _user.value = auth.currentUser
                        onSucces()
                        ConsultaRepository.CreateUser(email)
                    }
            }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Error al crear la cuenta")
            }
    }

    /** Resend verification email to the given address */
    fun resendVerificationEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                auth.currentUser?.sendEmailVerification()
                    ?.addOnSuccessListener { auth.signOut(); onResult(true) }
                    ?.addOnFailureListener { auth.signOut(); onResult(false) }
            }
            .addOnFailureListener { onResult(false) }
    }

    /** Sign in with a Google ID token obtained from the Credential Manager */
    fun signInWithGoogle(
        idToken: String,
        onSucces: () -> Unit,
        onError: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _user.value = auth.currentUser
                _authState.value = AuthState.Idle
                onSucces()
            }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Error con Google Sign-In")
            }
    }

    fun logout() {
        auth.signOut()
        _user.value = null
        _authState.value = AuthState.Idle
    }

    fun clearAuthState() {
        _authState.value = AuthState.Idle
    }

    suspend fun guardarConsultaClima(datos: Map<String, Any>) {
        auth.currentUser?.uid ?: return
        ConsultaRepository.guardarConsulta(datos)
    }
}

sealed class AuthState {
    object Idle : AuthState()
    data class VerificationSent(val email: String) : AuthState()
    data class NeedsVerification(val email: String) : AuthState()
}
