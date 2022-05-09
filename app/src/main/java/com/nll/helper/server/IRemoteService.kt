package com.nll.helper.server

import android.os.IBinder
import remoter.annotations.Oneway
import remoter.annotations.ParamIn
import remoter.annotations.Remoter


/**
 *
 * Server files should be within app-recorder and copied to acr module app-helper-client.
 *
 * Spent a day trying to move client files to its own module like server
 *
 * Had to include server files and consequently included manifest declarations for the server
 *
 * Spend 3 hour figuring out ACR PHone was actually connection to itself because server files were packaged!!
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