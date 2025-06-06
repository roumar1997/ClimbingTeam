package com.example.climbingteam.viewmodels

import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.lifecycle.ViewModel
import com.example.climbingteam.repository.ConsultaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _user =  MutableStateFlow<FirebaseUser?>(auth.currentUser)

    val user: StateFlow<FirebaseUser?> = _user.asStateFlow();

    fun login(email: String, password: String, onSucces: ()-> Unit, onError: (String)-> Unit ){
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _user.value = auth.currentUser
                onSucces()
            }
            .addOnFailureListener{
                onError(it.localizedMessage)
            }
    }

    fun logout(){
        auth.signOut()
        _user.value = null
    }

    fun register(email: String, password: String, onSucces: () -> Unit, onError: (String) -> Unit){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _user.value = auth.currentUser
                onSucces()
                ConsultaRepository.CreateUser(email)
            }
            .addOnFailureListener{
                onError(it.localizedMessage)
            }
    }
// consultar repositorio

   suspend fun guardarConsultaClima(datos: Map<String, Any>) {
        val userId = auth.currentUser?.uid ?: return
        ConsultaRepository.guardarConsulta(datos)
    }





}