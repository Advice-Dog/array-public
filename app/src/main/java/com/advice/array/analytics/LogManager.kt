package com.advice.array.analytics

import android.util.Log
import com.advice.array.api.JsoupConverterFactory
import com.advice.array.utils.Storage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.*

class LogManager : KoinComponent {

    private val storage by inject<Storage>()

    fun sendLogs() {
        val uuid = storage.uuid

        val db = Firebase.firestore

        val language = Locale.getDefault().language

        val version = storage.config?.version ?: "0.0.0"
        val log = hashMapOf(
            "version" to version,
            "language" to language
        )

        db.collection("users")
            .document(storage.uuid)
            .set(log)
            .addOnSuccessListener { ref ->
                Timber.d("DocumentSnapshot added with ID:")
            }.addOnFailureListener { e ->
                Log.w("LoginResponseConverter", "Error adding document", e)
            }

        val converters = JsoupConverterFactory.converters

        converters.forEach {
            it.sendLog(uuid, version, language)
        }
    }

    fun log(message: String) {
        println(message)
    }
}