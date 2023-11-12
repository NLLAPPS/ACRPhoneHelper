package com.nll.helper.server

import android.content.Context
import android.os.IBinder
import com.nll.helper.bridge.AccessibilityServiceBridge
import com.nll.helper.bridge.RecorderBridge
import com.nll.helper.recorder.CLog


/**
 *
 *  See https://github.com/josesamuel/remoter
 *
 */
class RemoteServiceImpl(private val context: Context) : IRemoteService {
    private val logTag = "CR_RemoteServiceImpl (${Integer.toHexString(System.identityHashCode(this))})"

    private val accessibilityServiceBridge: IAccessibilityServiceBridge = AccessibilityServiceBridge()
    private val recorderBridge: IRecorderBridge = RecorderBridge()

    private var serverRecordingState: ServerRecordingState = ServerRecordingState.Stopped
    private val serverRecorderListener = object : ServerRecorderListener {
        override fun onRecordingStateChange(newState: ServerRecordingState) {
            CLog.log(logTag, "onRecordingStateChange() -> newState: $newState")

            serverRecordingState = newState
            listeners.forEach { listener ->
                try {
                    CLog.log(logTag, "onRecordingStateChange() -> listener: $listener")
                    listener.onRecordingStateChange(newState.asResponseCode())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    override suspend fun startRecording(
        encoder: Int,
        recordingFile: String,
        audioChannels: Int,
        encodingBitrate: Int,
        audioSamplingRate: Int,
        audioSource: Int,
        mediaRecorderOutputFormat: Int,
        mediaRecorderAudioEncoder: Int,
        recordingGain: Int
    ): Int {

        val result = if (accessibilityServiceBridge.isHelperServiceEnabled(context)) {
            try {
                recorderBridge.startRecording(
                    context = context.applicationContext,
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
            } catch (e: Exception) {
                e.printStackTrace()
                RemoteResponseCodes.RECORDING_STOPPED
            }

        } else {
            CLog.log(logTag, "startRecording() -> isHelperServiceEnabled false. Return HELPER_IS_NOT_RUNNING (${RemoteResponseCodes.HELPER_IS_NOT_RUNNING})")
            RemoteResponseCodes.HELPER_IS_NOT_RUNNING
        }
        CLog.log(logTag, "startRecording() -> result: $result")
        return result
    }

    private fun internalStopRecordingAndCleanup(){
        CLog.log(logTag, "internalStopRecordingAndCleanup()")
        try {
            recorderBridge.stopRecording()
        } catch (e: Exception) {
            e.printStackTrace()
            /*
                Set state to stopped if there was a crash
                We do not use try catch at recorderBridge because we do not have access to serverRecordingState there
             */
            serverRecordingState = ServerRecordingState.Stopped
        }
    }
    override suspend fun stopRecording() {
        CLog.log(logTag, "stopRecording()")
        internalStopRecordingAndCleanup()
    }

    override suspend fun pauseRecording() {
        CLog.log(logTag, "pauseRecording()")
        try {
            recorderBridge.pauseRecording()
        } catch (e: Exception) {
            e.printStackTrace()

            CLog.log(logTag, "Crash! Call internalStopRecordingAndCleanup()")
            internalStopRecordingAndCleanup()
        }


    }

    override suspend fun resumeRecording() {
        CLog.log(logTag, "resumeRecording()")
        try {
            recorderBridge.resumeRecording()

        } catch (e: Exception) {
            e.printStackTrace()

            CLog.log(logTag, "Crash! Call internalStopRecordingAndCleanup()")
            internalStopRecordingAndCleanup()
        }


    }

    /**
     * Listen to client process death to re init and ask to connect back
     * This may not work on android 14+
     * See https://twitter.com/MishaalRahman/status/1722758953300292095
     * See https://android.googlesource.com/platform/frameworks/base/+/79825c6f2f8b46808e4b431fe52b3be78f1e8ac8
     */
    private lateinit var clientBinder: IBinder
    override suspend fun registerClientProcessDeath(clientDeathListener: IBinder) {
        CLog.log(logTag, "registerClientProcessDeath()")
        clientBinder = clientDeathListener
        //Create new anonymous class of IBinder.DeathRecipient()
        clientDeathListener.linkToDeath(object : IBinder.DeathRecipient {
            override fun binderDied() {
                CLog.log(logTag, "registerClientProcessDeath() -> Client died")
                try {
                    CLog.log(logTag, "onUnbind() -> Calling internalStopRecordingAndCleanup() just to make sure stop recording when client dies")
                    internalStopRecordingAndCleanup()
                    CLog.log(logTag, "onUnbind() -> Calling stopRecording() asking client to conenct again")
                    ClientContentProviderHelper.askToClientToConnect(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //Unregister from client death recipient
                clientBinder.unlinkToDeath(this, 0)
            }
        }, 0)
    }

    private val listeners = mutableListOf<IRemoteServiceListener>()
    override fun registerListener(listener: IRemoteServiceListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    override fun unRegisterListener(listener: IRemoteServiceListener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener)
        }
    }


}