package com.nll.helper.recorder

import com.nll.helper.BuildConfig

object CLog {
    private const val logTag = "CLog"
    private var _debug = BuildConfig.DEBUG


    fun isDebug() = _debug
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
    }


}