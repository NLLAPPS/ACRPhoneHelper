package com.nll.helper.server

sealed class ServerRecordingState {
    companion object {
        fun fromResponseCode(code: Int) = when (code) {
            RemoteResponseCodes.RECORDING_ERROR -> Error(RecorderError.RemoteError, Exception("Remote error"))
            RemoteResponseCodes.RECORDING_PAUSED -> Paused
            RemoteResponseCodes.RECORDING_RECORDING -> Recording
            RemoteResponseCodes.RECORDING_STOPPED -> Stopped
            else -> throw (java.lang.IllegalArgumentException("Unknown RemoteResponseCode ($code)"))

        }
    }

    fun asResponseCode() = when (this) {
        is Error -> RemoteResponseCodes.RECORDING_ERROR
        Paused -> RemoteResponseCodes.RECORDING_PAUSED
        Recording -> RemoteResponseCodes.RECORDING_RECORDING
        Stopped -> RemoteResponseCodes.RECORDING_STOPPED
        HelperIsNotRunning -> RemoteResponseCodes.HELPER_IS_NOT_RUNNING
        RemoteAIDLError -> RemoteResponseCodes.REMOTE_AIDL_ERROR
    }


    object Recording : ServerRecordingState()
    object Stopped : ServerRecordingState()
    object Paused : ServerRecordingState()
    object HelperIsNotRunning : ServerRecordingState()
    object RemoteAIDLError : ServerRecordingState()
    class Error(val recorderError: RecorderError, val exception: Exception) : ServerRecordingState()

    override fun toString(): String {
        return when (this) {
            Recording -> "Recording"
            Stopped -> "Stopped"
            Paused -> "Paused"
            HelperIsNotRunning -> "HelperIsNotRunning"
            RemoteAIDLError -> "RemoteAIDLError"
            is Error -> "Error ($exception)"
        }
    }
}