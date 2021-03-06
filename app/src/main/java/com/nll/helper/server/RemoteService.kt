package com.nll.helper.server

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.nll.helper.recorder.CLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.properties.Delegates

/**
 * Wrapper for IRemoteService_Proxy
 */
class RemoteService : LifecycleService() {
    private val logTag = "CR_RemoteService (${Integer.toHexString(System.identityHashCode(this))})"

    /**
     * We need to make sure we always provide same instance in case client re connects.
     * Without it (although RemoteServiceImpl receives callback and stops recording at RemoteServiceImpl#registerClientProcessDeath) we may have a dangling instance of this class recording continuously!
     */
    private val remoteServiceStub: IRemoteService_Stub by lazy {
        IRemoteService_Stub(RemoteServiceImpl(applicationContext))
    }

    override fun onCreate() {
        super.onCreate()
        CLog.log(logTag, "onCreate()")
    }

    override fun onDestroy() {
        super.onDestroy()
        CLog.log(logTag, "onDestroy()")
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        CLog.log(logTag, "onBind()")
        connectionCount++
        return remoteServiceStub
    }

    override fun onUnbind(intent: Intent): Boolean {
        connectionCount--
        CLog.log(logTag, "onUnbind()")
        return super.onUnbind(intent)
    }

    companion object {
        private val clientConnectionCount = MutableStateFlow(0)
        fun observeClientConnectionCount() = clientConnectionCount.asStateFlow()

        private var connectionCount by Delegates.observable(0) { _, oldValue, newValue ->
            clientConnectionCount.tryEmit(newValue)
        }

    }
}