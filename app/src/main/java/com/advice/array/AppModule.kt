package com.advice.array

import com.advice.array.analytics.AnalyticsManager
import com.advice.array.analytics.LogManager
import com.advice.array.api.CustomURLInterceptor
import com.advice.array.api.JsoupConverterFactory
import com.advice.array.api.LocalCookieJar
import com.advice.array.api.TimberLogger
import com.advice.array.api.UnraidRepository
import com.advice.array.api.UnraidService
import com.advice.array.api.UnsafeTrustManager
import com.advice.array.api.config.ConfigManager
import com.advice.array.api.converters.DashboardResponseConverter
import com.advice.array.api.converters.DockerContainerConverter
import com.advice.array.api.converters.LoginResponseConverter
import com.advice.array.api.converters.ShareConverter
import com.advice.array.api.converters.UPSConverter
import com.advice.array.api.converters.VirtualMachineConverter
import com.advice.array.api.sockets.SocketFactory
import com.advice.array.login.LoginViewModel
import com.advice.array.utils.Storage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.SecureRandom
import javax.net.ssl.SSLContext

val appModule = module {

    single { Storage(androidContext(), get(), Gson()) }

    single { LocalCookieJar(get()) }
    single { ConfigManager(get(), get()) }
    single { SocketFactory(get(), get(), get()) }

    // repo
    single { UnraidRepository(get(), get(), get(), get(), get(), get()) }

    // Converters
    single { LoginResponseConverter() }
    single { DockerContainerConverter() }
    single { UPSConverter() }
    single { ShareConverter() }
    single { DashboardResponseConverter() }
    single { VirtualMachineConverter() }

    single { Gson() }

    // Firebase
    single { FirebaseCrashlytics.getInstance() }
    single { FirebaseAnalytics.getInstance(androidContext()) }
    single { AnalyticsManager(get()) }
    single { LogManager() }

    single {
        val httpLoggingInterceptor = HttpLoggingInterceptor(TimberLogger())
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        // todo: remove unsafe trust manager
        val trustManager = UnsafeTrustManager()
        val sslSocketFactory = SSLContext.getInstance("SSL").apply {
            init(null, arrayOf(trustManager), SecureRandom())
        }.socketFactory

        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .cookieJar(get<LocalCookieJar>())
            .addInterceptor(CustomURLInterceptor(get()))
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val gson = GsonBuilder()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(CustomURLInterceptor.BASE_URL)
            .addConverterFactory(JsoupConverterFactory)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()

        retrofit.create(UnraidService::class.java)
    }

    viewModel { LoginViewModel() }
}