package com.advice.array.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.advice.array.api.LocalCookieJar
import com.advice.array.api.UnraidRepository
import com.advice.array.api.response.Response
import com.advice.array.notification.NotificationHelper
import com.advice.array.utils.Storage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class NotificationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {

    private val storage: Storage by inject()
    private val repository: UnraidRepository by inject()
    private val localCookieJar: LocalCookieJar by inject()

    override suspend fun doWork(): Result {
        val cookies = storage.cookies ?: return Result.failure()
        localCookieJar.setCookies(cookies)

        // Login
        when (val result = repository.relog()) {
            is Response.Error -> {
                return Result.failure()
            }
        }

        // Fetch the notifications
        return when (val result = repository.getNotifications()) {
            is Response.Success -> {
                NotificationHelper.clearNotifications(applicationContext, result.data.map { it.id })

                result.data.forEach {
                    NotificationHelper.postNotification(applicationContext, it)
                }
                Result.success()
            }
            is Response.Error -> {
                Result.failure()
            }
        }
    }

    companion object {
        const val NOTIFICATION_WORK_TAG = "notifications"
    }
}