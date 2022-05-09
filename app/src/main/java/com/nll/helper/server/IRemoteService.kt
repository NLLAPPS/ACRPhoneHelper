package com.nll.helper.server

import android.os.IBinder
import remoter.annotations.Oneway
import remoter.annotations.ParamIn
import remoter.annotations.Remoter


/**
 *
 *  See https://github.com/josesamuel/remoter
 *
 */
@Remoter
interface IRemoteService {
    suspend fun startRecording( encoder: Int,
                                recordingFile: String,
                                audioChannels: Int,
                                encodingBitrate: Int,
                                audioSamplingRate: Int,
                                audioSource: Int,
                                mediaRecorderOutputFormat: Int,
                                mediaRecorderAudioEncoder: Int,
                                recordingGain: Int) : Int
    suspend fun stopRecording()
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    suspend fun registerClientProcessDeath(@ParamIn clientDeathListener: IBinder)
    @Oneway
    fun registerListener(listener: IRemoteServiceListener)
    @Oneway
    fun unRegisterListener(listener: IRemoteServiceListener)
    companion object {
        /**Intent to connect for this service. Will be used by the client.*/
        const val SERVICE_INTENT = "com.nll.helper.RemoteService"
    }


}