package com.nll.helper.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.nll.helper.server.RecorderError
import com.nll.helper.server.ServerRecordingState
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class AndroidMediaAudioRecorder(private val recorderConfig: RecorderConfig) : Recorder, MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {
    private val logTag = "AndroidMediaAudioRecorder"
    private var timeAtPause: Long = 0
    private var elapsedTimeOnResumeInMicroSeconds: Long = 0
    private var recordingStartTime: Long = 0
    private var recorder: MediaRecorder? = null
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

    override fun needsPermissions(context: Context): Array<String> {
        val permissionsNeeded = arrayOf(Manifest.permission.RECORD_AUDIO).filter { permission ->
            ContextCompat.checkSelfPermission(context.applicationContext, permission) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()

        if (CLog.isDebug()) {
            CLog.log(logTag, "needsPermissions -> permissions: ${permissionsNeeded.joinToString(", ")}")
        }
        return permissionsNeeded
    }


    override fun getRoughRecordingTimeInMillis(): Long {
        val roughRecordingTimeInMillis = TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - recordingStartTime) - TimeUnit.MICROSECONDS.toNanos(elapsedTimeOnResumeInMicroSeconds))
        if (CLog.isDebug()) {
            CLog.log(logTag, "roughRecordingTimeInMillis: $roughRecordingTimeInMillis")

        }
        return roughRecordingTimeInMillis

    }

    private fun setElapsedTimeOnResume() {
        val changeInMicroSeconds = (System.nanoTime() / 1000L - timeAtPause)
        elapsedTimeOnResumeInMicroSeconds += changeInMicroSeconds

        if (CLog.isDebug()) {
            CLog.log(logTag, "setElapsedTimeOnResume() -> elapsedTimeOnResume: $elapsedTimeOnResumeInMicroSeconds")
        }
    }


    override fun startRecording() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "start() -> Start called. AudioRecorderConfig is: $recorderConfig")
        }

        recorder = MediaRecorder().apply {
            try {
                setAudioChannels(recorderConfig.audioChannels)
                setAudioEncodingBitRate(recorderConfig.encodingBitrate)
                setAudioSamplingRate(recorderConfig.audioSamplingRate)
                setAudioSource(recorderConfig.audioSource)
                setOutputFormat(recorderConfig.mediaRecorderOutputFormat)
                setOutputFile(FileOutputStream(recorderConfig.recordingFile).fd)
                setAudioEncoder(recorderConfig.mediaRecorderAudioEncoder)
                setOnErrorListener(this@AndroidMediaAudioRecorder)
                setOnInfoListener(this@AndroidMediaAudioRecorder)

                prepare()
                start()
                elapsedTimeOnResumeInMicroSeconds = 0
                timeAtPause = 0
                recordingStartTime = System.nanoTime()
                serverRecordingState = ServerRecordingState.Recording
                if (CLog.isDebug()) {
                    CLog.log(logTag, "startRecording() -> Recording started")
                }
            } catch (e: Exception) {
                serverRecordingState = ServerRecordingState.Error(RecorderError.MediaRecorderError, e)
                if (CLog.isDebug()) {
                    CLog.log(logTag, "startRecording() -> Recording cannot start! Error is:")
                }
                CLog.logPrintStackTrace(e)
            }
        }
    }

    override fun stopRecording() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "stopRecording() -> Stop called")
        }
        recorder?.apply {
            try {
                stop()
                release()
                recorder = null
                serverRecordingState = ServerRecordingState.Stopped

            } catch (e: Exception) {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "stopRecording() -> Exception while stopping")
                }
                CLog.logPrintStackTrace(e)
                recorder = null
                serverRecordingState = ServerRecordingState.Error(RecorderError.MediaRecorderError, e)
            }
        }

    }

    private fun setTimeAtPause() {
        timeAtPause = System.nanoTime() / 1000L
    }

    override fun pauseRecording() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "pauseRecording() -> Pause called")
        }
        try {
            if (serverRecordingState == ServerRecordingState.Recording) {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "pauseRecording() -> recordingState == RecordingState.Recording. Pausing...")
                }
                setTimeAtPause()
                recorder?.pause()
                serverRecordingState = ServerRecordingState.Paused
            } else {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "pauseRecording() -> Error! Pause should only be called after Start or before Stop is called. Current state is: $serverRecordingState")
                }
            }
        } catch (e: Exception) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "pauseRecording() -> Error! Pause called either before start or after stop. Current state is: $serverRecordingState")
            }
            CLog.logPrintStackTrace(e)
        }
    }

    override fun resumeRecording() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "resumeRecording() -> Resume called")
        }
        try {
            if (serverRecordingState == ServerRecordingState.Paused) {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "resumeRecording() -> recordingState == RecordingState.Paused. Resuming...")
                }
                setElapsedTimeOnResume()
                recorder?.resume()
                serverRecordingState = ServerRecordingState.Recording

            } else {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "resumeRecording() -> Error! Resume should only be called after Start or before Stop is called. Current state is: $serverRecordingState")
                }
            }
        } catch (e: Exception) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "resumeRecording() -> Error! Resume called either before start or after stop. Current state is: $serverRecordingState")
            }
            CLog.logPrintStackTrace(e)
        }
    }

    override fun getState(): ServerRecordingState {
        return serverRecordingState
    }

    override fun getConfig() = recorderConfig

    override fun onError(mr: MediaRecorder, what: Int, extra: Int) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "onError() -> what: $what, extra: $extra")
        }
        when (what) {
            MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN -> {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "onError() -> MEDIA_RECORDER_ERROR_UNKNOWN")
                }
            }
            MediaRecorder.MEDIA_ERROR_SERVER_DIED -> {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "onError() -> MEDIA_ERROR_SERVER_DIED")
                }
            }
            else -> {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "onError() -> Unknown error")
                }
            }
        }

        serverRecordingState = ServerRecordingState.Error(RecorderError.MediaRecorderError, Exception("Error code: $what, Error extra: $extra"))
    }

    override fun onInfo(mr: MediaRecorder, what: Int, extra: Int) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "onInfo() -> what: $what, extra: $extra")
        }

        when (what) {
            MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN -> {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "onInfo() -> MEDIA_RECORDER_INFO_UNKNOWN")
                }
            }
            MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "onInfo() -> MEDIA_RECORDER_INFO_MAX_DURATION_REACHED")
                }
            }
            MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "onInfo() -> MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED")
                }
            }
            else -> {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "onInfo() -> Unknown info")
                }
            }
        }
    }

    override fun toString(): String {
        return "AndroidMediaAudioRecorder(recorderConfig=$recorderConfig)"
    }

}