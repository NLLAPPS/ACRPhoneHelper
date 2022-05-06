package com.nll.helper.server

interface ServerRecorderListener {
    fun onRecordingStateChange(newState: ServerRecordingState)
}