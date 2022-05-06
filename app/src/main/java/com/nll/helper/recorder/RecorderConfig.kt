package com.nll.helper.recorder

import com.nll.helper.server.ServerRecorderListener
import java.io.File

data class RecorderConfig(
    val encoder: Int,
    val recordingFile: File,
    val audioChannels: Int,
    val encodingBitrate: Int,
    val audioSamplingRate: Int,
    val audioSource: Int,
    val mediaRecorderOutputFormat: Int,
    val mediaRecorderAudioEncoder: Int,
    val recordingGain: Int,
    val serverRecorderListener: ServerRecorderListener
) {
    companion object {
        fun fromPrimitives(
            encoder: Int,
            recordingFile: File,
            audioChannels: Int,
            encodingBitrate: Int,
            audioSamplingRate: Int,
            audioSource: Int,
            mediaRecorderOutputFormat: Int,
            mediaRecorderAudioEncoder: Int,
            recordingGain: Int,
            serverRecorderListener: ServerRecorderListener
        ) = RecorderConfig(
            encoder = encoder,
            recordingFile = recordingFile,
            audioChannels = audioChannels,
            encodingBitrate = encodingBitrate,
            audioSamplingRate = audioSamplingRate,
            audioSource = audioSource,
            mediaRecorderOutputFormat = mediaRecorderOutputFormat,
            mediaRecorderAudioEncoder = mediaRecorderAudioEncoder,
            recordingGain = recordingGain,
            serverRecorderListener = serverRecorderListener
        )
    }
}