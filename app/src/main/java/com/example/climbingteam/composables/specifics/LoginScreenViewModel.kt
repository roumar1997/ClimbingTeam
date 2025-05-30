package com.example.climbingteam.composables.specifics

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class LoginScreenViewModel: ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val loading = MutableLiveData(false)


    fun SingInWithEmailAndPassword(email: String,password:String,home: ()-> Unit)
    = viewModelScope.launch {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {task->
                if (task.isSuccessful){
                    Log.d("ClimbingTeam", "signWithEmailAndPassword logueado ")
                    home()
                }
                    else{
                    Log.d("ClimbingTeam", "signWithEmailAndPassword: ${task.result.toString()} ")

                    }
                }
        }
        catch (ex:Exception){
            Log.d("ClimbingTeam", "signWithEmailAndPassword: ${ex.message} ")
        }
    }
}