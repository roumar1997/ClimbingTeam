package com.example.climbingteam.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

object ConsultaRepository {

    private val db = FirebaseFirestore.getInstance()

    fun guardarConsulta(userId: String, datos: Map<String, Any>) {
        val fechaActual = Timestamp.now()
        val fechaString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(fechaActual.toDate())

        val docRef = db.collection("usuarios")
            .document(userId)
            .collection("consultas")
            .document(fechaString)

        docRef.set(datos)
            .addOnSuccessListener {
                Log.d("Firestore", "Consulta guardada correctamente.")
                eliminarConsultasAntiguas(userId)
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al guardar consulta: ${it.message}")
            }
    }

    private fun eliminarConsultasAntiguas(userId: String) {
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

    fun obtenerUltimasConsultas(
        userId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (String) -> Unit
    ) {
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
}
