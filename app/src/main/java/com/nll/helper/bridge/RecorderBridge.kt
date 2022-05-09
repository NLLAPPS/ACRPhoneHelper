package com.nll.helper.bridge

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.nll.helper.Constants
import com.nll.helper.MainActivity
import com.nll.helper.R
import com.nll.helper.recorder.*
import com.nll.helper.server.IRecorderBridge
import com.nll.helper.server.RemoteResponseCodes
import com.nll.helper.server.ServerRecorderListener
import com.nll.helper.server.ServerRecordingState
import io.karn.notify.Notify
import io.karn.notify.entities.Payload


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
            val recorderConfig = RecorderConfig.fromPrimitives(encoder, realRecordingFile, audioChannels, encodingBitrate, audioSamplingRate, audioSource, mediaRecorderOutputFormat, mediaRecorderAudioEncoder, recordingGain, serverRecorderListener)

            CLog.log(logTag, "startRecording() -> is recorder null ${recorder == null}, is recorder recording ${recorder?.getState() == ServerRecordingState.Recording}")

            /**
             * Just in case we have a dangling recorder.
             * IPC communication is quite complicated and frigate.
             */
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
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingOpenIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alertPayload = getChannel(context)

        Notify.with(context)
            .alerting(alertPayload.channelKey) {
                lockScreenVisibility = alertPayload.lockScreenVisibility
                channelName = alertPayload.channelName
                channelDescription = alertPayload.channelDescription
                channelImportance = alertPayload.channelImportance
            }
            .header {
                icon = R.drawable.ic_warning_24
                showTimestamp = true
            }
            .meta {
                group = "helper_permission_notification"
                sticky = true
                cancelOnClick = true
                clickIntent = pendingOpenIntent
            }
            .content {
                title = context.getString(R.string.audio_record_permission)
                text = context.getString(R.string.call_rec_permissions_message)
            }.show(Constants.permissionNotificationId)
    }

    private fun getChannel(context: Context) = Payload.Alerts(
        channelKey = "helper_permission_notification",
        lockScreenVisibility = NotificationCompat.VISIBILITY_PUBLIC,
        channelName = context.getString(R.string.permissions_title),
        channelDescription = context.getString(R.string.permissions_title),
        channelImportance = Notify.IMPORTANCE_HIGH,
        showBadge = false

    )

}