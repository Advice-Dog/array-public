package com.advice.array.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.advice.array.api.config.Config
import com.advice.array.api.response.LoginResponse
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import java.util.*

class Storage(
    context: Context,
    crashlytics: FirebaseCrashlytics,
    private val gson: Gson
) {

    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private val securePreferences: SharedPreferences =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val build = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            try {
                EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFERENCES_FILENAME,
                    build,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (ex: Exception) {
                crashlytics.log("Could not create EncryptedSharedPreferences.")
                crashlytics.recordException(ex)
                preferences
            }
        } else {
            preferences
        }

    var config: Config?
        get() {
            val string = preferences.getString("config", null) ?: return null
            return gson.fromJson(string, Config::class.java)
        }
        set(value) {
            preferences.edit().putString("config", gson.toJson(value)).apply()
        }

    var cookies: ArrayList<Cookie>?
        get() {
            val string = preferences.getString("cookies", null) ?: return null
            val type = object : TypeToken<ArrayList<Cookie>>() {}.type
            return gson.fromJson(string, type)
        }
        set(value) {
            preferences.edit().putString("cookies", gson.toJson(value)).apply()
        }

    var address: String?
        get() = securePreferences.getString(KEY_ADDRESS, null)
        set(value) {
            securePreferences.edit().putString(KEY_ADDRESS, value).apply()
        }

    var username: String?
        get() = securePreferences.getString(KEY_USERNAME, null)
        set(value) {
            securePreferences.edit().putString(KEY_USERNAME, value).apply()
        }

    var password: String?
        get() = securePreferences.getString(KEY_PASSWORD, null)
        set(value) {
            securePreferences.edit().putString(KEY_PASSWORD, value).apply()
        }

    var server: LoginResponse?
        get() {
            val string = preferences.getString("server_config", null) ?: return null
            return gson.fromJson(string, LoginResponse::class.java)
        }
        set(value) {
            preferences.edit().putString("server_config", gson.toJson(value)).apply()
        }

    var lastNotification: String?
        get() = preferences.getString("last_notification", null)
        set(value) = preferences.edit().putString("last_notification", value).apply()

    var theme: Int
        get() = preferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = preferences.edit().putInt("theme", value).apply()

    var showCoreList: Boolean
        get() = preferences.getBoolean("show_core_list", false)
        set(value) = preferences.edit().putBoolean("show_core_list", value).apply()

    val uuid: String
        get() {
            val string = preferences.getString("uuid", null)
            if (string != null) {
                return string
            }

            // generate a new uuid
            val uuid = UUID.randomUUID().toString()
            preferences.edit().putString("uuid", uuid).apply()
            return uuid
        }


    companion object {
        private const val SECURE_PREFERENCES_FILENAME = "shared_preferences_filename"
        private const val KEY_ADDRESS = "address"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
    }
}