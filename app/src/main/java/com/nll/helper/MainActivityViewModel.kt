package com.nll.helper

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.nll.helper.server.RemoteService
import com.nll.helper.support.AccessibilityCallRecordingService
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MainActivityViewModel(app: Application) : AndroidViewModel(app) {
    private val logTag = "CR_MainActivityViewModel"

    private val _accessibilityServicesChanged = LiveEvent<Boolean>()
    fun observeAccessibilityServicesChanges() = _accessibilityServicesChanged

    private val _clientConnected = MutableLiveData<Boolean>()
    fun observeClientConnected(): LiveData<Boolean> = _clientConnected


    init {
        Log.i(logTag, "init()")


        val isEnabledOnInit = AccessibilityCallRecordingService.isHelperServiceEnabled(app)
        _accessibilityServicesChanged.postValue(isEnabledOnInit)

        AccessibilityCallRecordingService.observeAccessibilityServicesChangesLiveData().observeForever { isEnabled ->
            Log.i(logTag, "observeAccessibilityServicesChangesLiveData() -> isEnabled: $isEnabled")
            _accessibilityServicesChanged.postValue(isEnabled)
        }

        RemoteService.observeClientConnectionCount().onEach { connectionCount ->
            Log.i(logTag, "observeClientConnectionCount() -> connectionCount: $connectionCount")
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