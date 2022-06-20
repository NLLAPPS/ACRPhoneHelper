package com.nll.helper.debug

sealed class DebugLogServiceCommand {

    object Stop : DebugLogServiceCommand()
    object Save : DebugLogServiceCommand()
    object Clear : DebugLogServiceCommand()

    override fun toString(): String {
        return when (this) {
            is Stop -> "Stop"
            is Save -> "Save"
            is Clear -> "Clear"
        }
    }
}