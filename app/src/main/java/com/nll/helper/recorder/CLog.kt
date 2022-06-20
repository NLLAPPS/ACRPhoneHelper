package com.nll.helper.recorder

import com.nll.helper.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

object CLog {
    private const val logTag = "CLog"
    private val loggerDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT)
    private val loggerScope: CoroutineScope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
    private var _debug = true//BuildConfig.DEBUG
    private val _observableLog = MutableSharedFlow<String>()
    fun observableLog() = _observableLog.asSharedFlow()

    fun isDebug() = _debug
    fun enableDebug() {
        _debug = true
    }

    fun disableDebug() {
        _debug = false
    }


    fun logPrintStackTrace(e: Throwable) {
        //We do not want to print stack trace in to log if it is debug build. This would create douple printing of stack traces to logcat
        val shouldLog = isDebug() && !BuildConfig.DEBUG
        if (shouldLog) {
            log(logTag, e.stackTraceToString())
        }
        e.printStackTrace()
    }

    fun log(extraTag: String, message: String) {
        android.util.Log.d("CR_$extraTag", message)
        loggerScope.launch {
            _observableLog.emit("[${loggerDateFormat.format(System.currentTimeMillis())}] [CB_$extraTag] => $message")
        }
    }


}