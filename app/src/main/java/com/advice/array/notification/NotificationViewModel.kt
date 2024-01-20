package com.advice.array.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.advice.array.api.UnraidRepository
import com.advice.array.api.response.Response
import com.advice.array.models.Notification
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationViewModel : ViewModel(), KoinComponent {

    private val repository by inject<UnraidRepository>()

    private val _notifications = MutableLiveData<NotificationScreenState>()

    init {
        refresh()
    }

    fun refresh() {
        val previous = _notifications.value ?: NotificationScreenState()

        _notifications.postValue(previous.copy(isLoading = true))

        viewModelScope.launch {
            when (val result = repository.getNotifications(filter = false)) {
                is Response.Success -> {
                    _notifications.postValue(NotificationScreenState(data = result.data))
                }
                is Response.Error -> {
                    _notifications.postValue(
                        NotificationScreenState(error = result.exception)
                    )
                }
            }
        }
    }

    fun dismiss(notification: Notification) {
        val previous = _notifications.value ?: NotificationScreenState()
        val list = previous.data.filter { it.file != notification.file }
        _notifications.postValue(previous.copy(data = list))

        viewModelScope.launch {
            repository.dismissNotification(notification.file)
        }
    }

    fun getState(): LiveData<NotificationScreenState> = _notifications

}

data class NotificationScreenState(
    val isLoading: Boolean = false,
    val data: List<Notification> = emptyList(),
    val error: Exception? = null
)

fun NotificationScreenState.copy(
    isLoading: Boolean = this.isLoading,
    data: List<Notification> = this.data,
    error: Exception? = this.error
) = NotificationScreenState(isLoading, data, error)