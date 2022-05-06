package com.nll.helper.server

import android.content.Context
import android.net.Uri
import android.util.Log


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
object ClientContentProviderHelper {
    private const val logTag = "CR_ClientContentProviderHelper"

    //Ignore can be private!
    const val clientAuthority = "com.nll.helper.ClientContentProvider" //Same as client manifest

    //Ignore can be private!
    const val clientMethodAskToConnect = "client_ask_to_connect"

    //Ignore can be private!
    const val clientMethodGetClientVersionData = "client_version_data"

    //Ignore can be private!
    const val clientPackageName = "com.nll.cb"

    //Ignore can be private!
    fun askToClientToConnect(context: Context) {
        Log.i(logTag, "askToClientToConnect()")

        /**
         * Make sure we handle crash as user may not have main app installed
         */
        try {
            context.contentResolver.call(Uri.parse("content://$clientAuthority"), clientMethodAskToConnect, null, null)
        } catch (ignored: Exception) {
        }

    }

    //Ignore can be private!
    fun getClientVersionData(context: Context): ClientVersionData? {

        Log.i(logTag, "getClientVersionData()")
        /**
         * Make sure we handle crash as user may not have helper installed
         */
        val cmdResult = try {
            context.contentResolver.call(Uri.parse("content://${clientAuthority}"), clientMethodGetClientVersionData, null, null)
        } catch (e: Exception) {
            null
        }
        return if (cmdResult != null) {
            val clientVersionData = ClientVersionData.fromBundle(cmdResult)
            Log.i(logTag, "getClientVersionData() -> clientVersionData: $clientVersionData")
            clientVersionData
        } else {
            Log.i(logTag, "getClientVersionData() -> clientVersionData: null")
            null
        }

    }
}