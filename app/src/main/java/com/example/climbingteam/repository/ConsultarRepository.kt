package com.example.climbingteam.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

object ConsultaRepository {

    private val db = FirebaseFirestore.getInstance()

     suspend fun guardarConsulta( datos: Map<String, Any>) {
        val fechaActual = Timestamp.now()
        val fechaString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(fechaActual.toDate())
        val userId = getUserId_()
        val docRef = db.collection("usuarios")
            .document(userId)
            .collection("consultas")
            .document(fechaString)

        docRef.set(datos)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("Firestore", "Consulta guardada correctamente.")
                    eliminarConsultasAntiguas()
                }


            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al guardar consulta: ${it.message}")
            }
    }

    private suspend fun eliminarConsultasAntiguas() {
        val userId = getUserId_()
        db.collection("usuarios")
            .document(userId)
            .collection("consultas")
            .get()
            .addOnSuccessListener { result ->
                val documentos = result.documents.sortedBy { it.id }
                if (documentos.size > 4) {
                    documentos.dropLast(4).forEach { it.reference.delete() }
                }
            }
    }

   suspend fun obtenerUltimasConsultas(

        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (String) -> Unit
    ) {
       val userId = getUserId_()
        db.collection("usuarios")
            .document(userId)
            .collection("consultas")
            .orderBy("timestamp")
            .limitToLast(4)
            .get()
            .addOnSuccessListener { result ->
                val datos = result.documents.mapNotNull { it.data }
                onSuccess(datos)
            }
            .addOnFailureListener {
                onFailure(it.localizedMessage ?: "Error desconocido")
            }
    }
    //crear usuario para fireStore
    fun CreateUser (
        email: String
    )
    {
        val data = mapOf(
            "email" to email
        )
        db.collection("usuarios")
            .add(data)
            .addOnSuccessListener {
                Log.d("firebase", "usuario creado")
            }
            .addOnFailureListener {e ->
                Log.w("firebase", "error", e)
            }

        val docRef = db.collection("usuarios")
            .whereEqualTo("email", email)

    }


   suspend fun getUserId (
        email: String):String{
        val docRef = db.collection("usuarios")
            .whereEqualTo("email", email)
            .get().await()
        return docRef.documents.firstOrNull()!!.id ?: ""
    }



    suspend fun getUserId_ ():String{
        val auth = FirebaseAuth.getInstance()
        val email = auth.currentUser!!.email
        return ConsultaRepository.getUserId(email!!)

    }

}




