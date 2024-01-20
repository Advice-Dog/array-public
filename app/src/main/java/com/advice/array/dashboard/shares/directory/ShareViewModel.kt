package com.advice.array.dashboard.shares.directory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.advice.array.api.UnraidRepository
import com.advice.array.api.response.Response
import com.advice.array.models.Directory
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ShareViewModel : ViewModel(), KoinComponent {

    private val repository: UnraidRepository by inject()

    val progress: LiveData<Boolean>
        get() = _progress

    val list: LiveData<List<Directory>?>
        get() = _list

    private val _progress = MutableLiveData<Boolean>()
    private val _list = MutableLiveData<List<Directory>?>()

    fun fetch(dir: String) {
        _progress.value = true
        viewModelScope.launch {
            when (val result = repository.getShareDirectory("/mnt/user/$dir")) {
                is Response.Success -> {
                    _progress.postValue(false)
                    _list.postValue(result.data)
                }
                is Response.Error -> {
                    _progress.postValue(false)
                    _list.postValue(null)
                }
            }
        }
    }
}