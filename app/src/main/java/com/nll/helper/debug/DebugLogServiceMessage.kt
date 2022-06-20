package com.nll.helper.debug

import java.util.*

sealed class DebugLogServiceMessage {
    override fun toString(): String {
        return when (this) {
            is Saved -> "Saved(success: $success, path: $path)"
            is Started -> "Started(currentLogs: ${currentLogs.size})"
            Stopped -> "Stopped"
        }
    }

    class Started(val currentLogs: LinkedList<String>) : DebugLogServiceMessage()
    object Stopped : DebugLogServiceMessage()
    class Saved(val success: Boolean, val path: String?) : DebugLogServiceMessage()
}