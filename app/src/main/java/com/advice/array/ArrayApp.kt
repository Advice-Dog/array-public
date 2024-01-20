package com.advice.array

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.advice.array.analytics.ArrayEmailFeedbackCollector
import com.advice.array.utils.Storage
import com.github.stkent.amplify.feedback.GooglePlayStoreFeedbackCollector
import com.github.stkent.amplify.tracking.Amplify
import com.github.stkent.amplify.tracking.PromptInteractionEvent
import com.github.stkent.amplify.tracking.rules.MaximumCountRule
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber

class ArrayApp : Application() {

    companion object {
        const val SUPPORT_EMAIL = "chrisporter0111@gmail.com"

        lateinit var instance: ArrayApp
    }

    private val storage: Storage by inject()

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidContext(this@ArrayApp)
            modules(appModule)
        }

        Timber.plant(Timber.DebugTree())

        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
            setUserId(storage.uuid)
            setCustomKey("version", storage.config?.version ?: "0.0.0")
        }

        Amplify.initSharedInstance(this)
            .applyAllDefaultRules()
            .setPositiveFeedbackCollectors(GooglePlayStoreFeedbackCollector())
            .setCriticalFeedbackCollectors(ArrayEmailFeedbackCollector(SUPPORT_EMAIL))
            .setInstallTimeCooldownDays(5)
            .setLastUpdateTimeCooldownDays(7)
            .setLastCrashTimeCooldownDays(7)
            .addTotalEventCountRule(
                PromptInteractionEvent.USER_GAVE_POSITIVE_FEEDBACK,
                MaximumCountRule(1)
            )

        AppCompatDelegate.setDefaultNightMode(storage.theme)
    }
}