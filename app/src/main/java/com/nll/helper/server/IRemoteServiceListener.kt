package com.nll.helper.server

import remoter.annotations.Remoter



/**
 *
 *  See https://github.com/josesamuel/remoter
 *
 */
@Remoter
interface IRemoteServiceListener {
     fun onRecordingStateChange(recordingState: Int)

}