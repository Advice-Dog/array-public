package com.advice.array.api.converters

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Converter
import timber.log.Timber
import java.util.Locale

abstract class UnraidConverter<T> : Converter<ResponseBody, T> {

    private var previousValue: String? = null
    var baseUri: String? = null

    override fun convert(value: ResponseBody): T? {
        val string = value.string()
        previousValue = string
        return convert(baseUri, string)
    }

    fun sendLog(uuid: String, version: String, language: String) {
        val db = Firebase.firestore

        val log = hashMapOf(
            "value" to getValue(),
            "version" to version,
            "language" to language,
            "app_version" to BuildConfig.VERSION_NAME
        )

        val document = this.javaClass.simpleName.lowercase(Locale.getDefault())
            .replace("response", "")
            .replace("converter", "") + "-$language" + "-$version"

        db.collection("users")
            .document(uuid)
            .collection("logs")
            .document(document)
            .set(log)
            .addOnSuccessListener { ref ->
                Timber.d("DocumentSnapshot added with ID:")
            }.addOnFailureListener { e ->
                Timber.w("Error adding document", e)
            }
    }

    abstract fun convert(baseUri: String?, string: String): T

    // allows classes that use static functions to override this and return a different value
    open fun getValue() = previousValue
}