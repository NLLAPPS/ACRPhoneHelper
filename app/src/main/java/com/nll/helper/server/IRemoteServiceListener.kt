package com.nll.helper.server

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
interface IRemoteServiceListener {
     fun onRecordingStateChange(recordingState: Int)

}