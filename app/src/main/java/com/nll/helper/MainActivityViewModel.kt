package com.nll.helper

import android.app.Application
import androidx.lifecycle.*
import com.nll.helper.recorder.CLog
import com.nll.helper.server.RemoteService
import com.nll.helper.support.AccessibilityCallRecordingService
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MainActivityViewModel(app: Application) : AndroidViewModel(app) {
    private val logTag = "CR_MainActivityViewModel"

    private val _accessibilityServicesChanged = MutableLiveData<Boolean>()
    fun observeAccessibilityServicesChanges(): LiveData<Boolean> = _accessibilityServicesChanged

    private val _clientConnected = MutableLiveData<Boolean>()
    fun observeClientConnected(): LiveData<Boolean> = _clientConnected


    init {



        val isHelperServiceEnabledOnInit = AccessibilityCallRecordingService.isHelperServiceEnabled(app)
        CLog.log(logTag, "init() -> isHelperServiceEnabledOnInit: $isHelperServiceEnabledOnInit")

        _accessibilityServicesChanged.postValue(isHelperServiceEnabledOnInit)

        AccessibilityCallRecordingService.observeAccessibilityServicesChangesLiveData().observeForever { isEnabled ->
            CLog.log(logTag, "observeAccessibilityServicesChangesLiveData() -> isEnabled: $isEnabled")
            _accessibilityServicesChanged.postValue(isEnabled)
        }

        RemoteService.observeClientConnectionCount().onEach { connectionCount ->
            CLog.log(logTag, "observeClientConnectionCount() -> connectionCount: $connectionCount")
            _clientConnected.postValue(connectionCount > 0)
        }.launchIn(viewModelScope)
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainActivityViewModel(application) as T
        }
    }
}