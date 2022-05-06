package com.nll.helper.server

import android.content.Context

/**
 * Must be copy of IRemoteService
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