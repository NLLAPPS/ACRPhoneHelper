package com.nll.helper.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.nll.helper.server.RecorderError
import com.nll.helper.server.ServerRecordingState
import kotlin.properties.Delegates

class MediaCodecAudioRecorder2(private val recorderConfig: RecorderConfig) : Recorder, MediaCodecAudioEncoder2.Listener {
    private val logTag = "MediaCodecAudioRecorder2"
    private lateinit var encoder: MediaCodecAudioEncoder2
    override fun isSIPRecorder() = false
    override fun isHelperRecorder() = false

    /*

        It must be
        Delegates.observable<AudioRecorderState>(AudioRecorderState.Stopped)

        Not
        Delegates.observable(AudioRecorderState.Stopped)

        Or else you get
        Property delegate must have a 'setValue(MediaRecorderAudioRecorder, KProperty<*>, AudioRecorderState)' method. None of the following functions is suitable:
        public abstract operator fun setValue(thisRef: Any?, property: KProperty<*>, value: AudioRecorderState.Stopped): Unit defined in kotlin.properties.ReadWriteProperty


     */
    private var serverRecordingState: ServerRecordingState by Delegates.observable<ServerRecordingState>(ServerRecordingState.Stopped) { _, oldValue, newValue ->
        if (CLog.isDebug()) {
            CLog.log(logTag, "State value updated, oldValue: $oldValue, newValue: $newValue")
        }
        if (oldValue != newValue) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "Since oldValue != newValue calling recorderListener.onRecordingState ")
            }
            recorderConfig.serverRecorderListener.onRecordingStateChange(newValue)
        }
    }


    override fun startRecording() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "start() -> Start called. AudioRecorderConfig is: $recorderConfig")
        }
        encoder = MediaCodecAudioEncoder2(recorderConfig.recordingFile, recorderConfig.audioChannels, recorderConfig.audioSource, recorderConfig.audioSamplingRate, recorderConfig.encodingBitrate, recorderConfig.recordingGain, this)
        encoder.apply {
            try {
                start()
                serverRecordingState = ServerRecordingState.Recording
                if (CLog.isDebug()) {
                    CLog.log(logTag, "start() -> Recording started")
                }
            } catch (e: Exception) {
                serverRecordingState = ServerRecordingState.Error(RecorderError.MediaCodecException, e)
                if (CLog.isDebug()) {
                    CLog.log(logTag, "start() -> Recording cannot start! Error is:")
                }
                CLog.logPrintStackTrace(e)
            }
        }
    }


    override fun stopRecording() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "stop() -> Stop called")
        }
        encoder.apply {

            serverRecordingState = try {
                stop()
                ServerRecordingState.Stopped

            } catch (e: Exception) {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "stop() -> Exception while stopping")
                }
                CLog.logPrintStackTrace(e)
                ServerRecordingState.Error(RecorderError.MediaCodecException, e)
            }
        }


    }

    override fun pauseRecording() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "pause() -> Pause called")
        }
        try {
            if (serverRecordingState == ServerRecordingState.Recording) {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "pause() -> recordingState == RecordingState.Recording. Pausing...")
                }
                encoder.pause()
                serverRecordingState = ServerRecordingState.Paused
            } else {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "pause() -> Error! Pause should only be called after Start or before Stop is called. Current state is: $serverRecordingState")
                }
            }
        } catch (e: Exception) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "pause() -> Error! Pause called either before start or after stop. Current state is: $serverRecordingState")
            }
            CLog.logPrintStackTrace(e)
        }
    }

    override fun resumeRecording() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "resume() -> Resume called")
        }
        try {
            if (serverRecordingState == ServerRecordingState.Paused) {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "resume() -> recordingState == RecordingState.Paused. Resuming...")
                }
                encoder.resume()
                serverRecordingState = ServerRecordingState.Recording
            } else {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "resume() -> Error! Resume should only be called after Start or before Stop is called. Current state is: $serverRecordingState")
                }
            }
        } catch (e: Exception) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "resume() -> Error! Resume called either before start or after stop. Current state is: $serverRecordingState")
            }
            CLog.logPrintStackTrace(e)
        }
    }

    override fun getState(): ServerRecordingState {
        return serverRecordingState
    }

    override fun getConfig() = recorderConfig

    override fun needsPermissions(context: Context): Array<String> {
        val permissionsNeeded = arrayOf(Manifest.permission.RECORD_AUDIO).filter { permission ->
            ContextCompat.checkSelfPermission(context.applicationContext, permission) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()

        if (CLog.isDebug()) {
            CLog.log(logTag, "needsPermissions -> permissions: ${permissionsNeeded.joinToString(", ")}")
        }
        return permissionsNeeded
    }


    //Prevent  lateinit property encoder has not been initialized by checking state
    override fun getRoughRecordingTimeInMillis() = when (serverRecordingState) {
        ServerRecordingState.Recording,
        ServerRecordingState.Paused -> encoder.getRoughRecordingTimeInMillis()
        ServerRecordingState.Stopped -> 0
        is ServerRecordingState.Error -> 0
        ServerRecordingState.HelperIsNotRunning -> 0
        ServerRecordingState.RemoteAIDLError -> 0
    }

    override fun onEncoderError(mediaCodecAudioEncoder: MediaCodecAudioEncoder2, recorderError: RecorderError, exception: Exception) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "mediaCodecAudioEncoder.onError() -> recorderError: $recorderError")
        }
        serverRecordingState = ServerRecordingState.Error(recorderError, exception)
        CLog.logPrintStackTrace(exception)

    }


    override fun onEncoderStart(mediaCodecAudioEncoder: MediaCodecAudioEncoder2) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "mediaCodecAudioEncoder.onStart()")
        }
        serverRecordingState = ServerRecordingState.Recording
    }

    override fun onEncoderPause(mediaCodecAudioEncoder: MediaCodecAudioEncoder2) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "mediaCodecAudioEncoder.onPause()")
        }
        serverRecordingState = ServerRecordingState.Paused
    }

    override fun onEncoderStop(mediaCodecAudioEncoder: MediaCodecAudioEncoder2) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "mediaCodecAudioEncoder.onStop()")
        }
        serverRecordingState = ServerRecordingState.Stopped
    }


    override fun toString(): String {
        return "MediaCodecAudioRecorder(recorderConfig=$recorderConfig)"
    }
}