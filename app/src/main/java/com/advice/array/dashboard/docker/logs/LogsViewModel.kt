package com.advice.array.dashboard.docker.logs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.advice.array.api.UnraidRepository
import com.advice.array.api.response.Response
import com.advice.array.models.DockerContainer
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LogsViewModel(val container: DockerContainer) : ViewModel(), KoinComponent {

    private val repository by inject<UnraidRepository>()

    val logs: LiveData<List<String>>
        get() = _logs

    private val _logs = MutableLiveData<List<String>>()

    init {
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            when (val result = repository.getDockerContainerLogs(container.id)) {
                is Response.Success -> {
                    _logs.postValue(result.data)
                }
                is Response.Error -> {
                    _logs.postValue(null)
                }
            }
        }
    }


}