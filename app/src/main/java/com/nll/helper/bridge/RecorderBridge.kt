package com.nll.helper.bridge

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.nll.helper.App
import com.nll.helper.recorder.AndroidMediaAudioRecorder
import com.nll.helper.recorder.AudioRecordPermissionNotification
import com.nll.helper.recorder.CLog
import com.nll.helper.recorder.CacheFileProvider
import com.nll.helper.recorder.Encoder
import com.nll.helper.recorder.MediaCodecAudioRecorder2
import com.nll.helper.recorder.Recorder
import com.nll.helper.recorder.RecorderConfig
import com.nll.helper.server.IRecorderBridge
import com.nll.helper.server.RemoteResponseCodes
import com.nll.helper.server.ServerRecorderListener
import com.nll.helper.server.ServerRecordingState


class RecorderBridge : IRecorderBridge {

    private val logTag = "CR_RecorderBridge"
    private var recorder: Recorder? = null

    private fun needsAudioRecordPermission(context: Context) = ContextCompat.checkSelfPermission(context.applicationContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
    override fun startRecording(context: Context, encoder: Int, recordingFile: String, audioChannels: Int, encodingBitrate: Int, audioSamplingRate: Int, audioSource: Int, mediaRecorderOutputFormat: Int, mediaRecorderAudioEncoder: Int, recordingGain: Int, serverRecorderListener: ServerRecorderListener): Int {

        return if (needsAudioRecordPermission(context)) {
            CLog.log(logTag, "startRecording() -> needsAudioRecordPermission")
            showNeedsAudioRecordPermissionNotification(context)
            //Return stopped as we have not yet started and anything else would result error warning on in call screen where user cannot resume recording
            RemoteResponseCodes.RECORDING_STOPPED
        } else {
            val realRecordingFile = CacheFileProvider.provideCacheFile(context, recordingFile)
            /*
                Rather than altering everything, we simply check if we have CaptureAudioOutput permission (meaning app is installed with Magisk module) and pretend that accessibility service is running
                This saves us a lot of time we may spend changing whole structure of APH.
                Perhaps we can re-visit this and change the structure to introduces different call recording modes such as, root, accessibility etc
             */
            val realAudioSource = if (App.hasCaptureAudioOutputPermission()) {
                CLog.log(logTag, "startRecording() -> hasCaptureAudioOutputPermission() is true. Changing audioSource to MediaRecorder.AudioSource.VOICE_CALL")
                MediaRecorder.AudioSource.VOICE_CALL
            } else {
                audioSource
            }
            val recorderConfig = RecorderConfig.fromPrimitives(encoder, realRecordingFile, audioChannels, encodingBitrate, audioSamplingRate, realAudioSource, mediaRecorderOutputFormat, mediaRecorderAudioEncoder, recordingGain, serverRecorderListener)

            CLog.log(logTag, "startRecording() -> is recorder null ${recorder == null}, is recorder recording ${recorder?.getState() == ServerRecordingState.Recording}")

            CLog.log(logTag, "startRecording() -> Calling stopRecording() just in case we have a dangling recorder. IPC communication is quite complicated and fragile.")

            stopRecording()

            //Now start fresh
            recorder = when (Encoder.fromIdOrDefault(encoder)) {
                Encoder.AndroidMediaRecorder -> {
                    if (CLog.isDebug()) {
                        CLog.log(logTag, "startRecording() -> This is an a normal call and encoder is AndroidMediaRecorder. Returning AndroidMediaAudioRecorder")
                    }
                    AndroidMediaAudioRecorder(recorderConfig)
                }

                Encoder.MediaCodec -> {
                    if (CLog.isDebug()) {
                        CLog.log(logTag, "startRecording() -> This is an a normal call and encoder is MediaCodec. Returning MediaCodecAudioRecorder2")
                    }
                    MediaCodecAudioRecorder2(recorderConfig)
                }
            }
            requireNotNull(recorder).startRecording()
            RemoteResponseCodes.RECORDING_RECORDING
        }
    }

    override fun stopRecording() {
        CLog.log(logTag, "stopRecording()")
        recorder?.stopRecording()
        recorder = null
    }

    override fun pauseRecording() {
        CLog.log(logTag, "pauseRecording()")
        recorder?.pauseRecording()

    }

    override fun resumeRecording() {
        CLog.log(logTag, "resumeRecording()")
        recorder?.resumeRecording()
    }

    override fun showNeedsAudioRecordPermissionNotification(context: Context) {
        CLog.log(logTag, "showNeedsAudioRecordPermissionNotification()")
        AudioRecordPermissionNotification.show(context)
    }


}