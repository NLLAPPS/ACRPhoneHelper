package com.nll.helper.server

import android.content.Context

/**
 * Must be copy of IRemoteService
 *
 * Implementation for server is provided.
 * Client should implement no-op version of this interface in the same package
 *
 * While it won't be used, due to nature of AIDL, server package has to be in client code.
 * We use bridge package to provide no-op version of this interface to client app.
 */
interface IRecorderBridge {
    fun startRecording(
        context: Context,
        encoder: Int,
        recordingFile: String,
        audioChannels: Int,
        encodingBitrate: Int,
        audioSamplingRate: Int,
        audioSource: Int,
        mediaRecorderOutputFormat: Int,
        mediaRecorderAudioEncoder: Int,
        recordingGain: Int,
        serverRecorderListener: ServerRecorderListener
    ): Int


    fun stopRecording()
    fun pauseRecording()
    fun resumeRecording()

    fun showNeedsAudioRecordPermissionNotification(context: Context)
}