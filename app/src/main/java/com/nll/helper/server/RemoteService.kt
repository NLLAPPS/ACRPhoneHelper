package com.nll.helper.server

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.properties.Delegates

/**
 *
 * Server files should be within app-recorder and copied to acr module app-helper-client.
 *
 * Spent a day trying to move client files to its own module like server
 *
 * Had to include server files and consequently included manifest declarations for the server
 *
 * Spend 3 hour figuring out ACR PHone was actually connection to itself because server files were packaged!!
 *
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
        Log.i(logTag, "onCreate()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(logTag, "onDestroy()")
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.i(logTag, "onBind()")
        connectionCount++
        return remoteServiceStub
    }

    override fun onUnbind(intent: Intent): Boolean {
        connectionCount--
        Log.i(logTag, "onUnbind()")
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