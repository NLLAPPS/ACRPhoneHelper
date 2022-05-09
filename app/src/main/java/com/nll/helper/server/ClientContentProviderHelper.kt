package com.nll.helper.server

import android.content.Context
import android.net.Uri
import com.nll.helper.recorder.CLog


/**
 *
 *These are commands/requests server (APH) sends to client (ACR Phone) and expects response
 * Client should implement a Content provider just like ServerContentProvider and respond to these commands/requests
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
        CLog.log(logTag, "askToClientToConnect()")

        /**
         * Make sure we handle crash as user may not have main app installed.
         * Client in response calls attempts to connect by creating instance of
         * IRemoteService_Proxy(context.applicationContext, IRemoteService.SERVICE_INTENT)
         *
         * Our client implementation uses coroutines, fo instantiating IRemoteService_Proxy also attempts to connect.
         * See https://github.com/josesamuel/remoter#kotlin-support-with-suspend-functions
         */
        try {
            context.contentResolver.call(Uri.parse("content://$clientAuthority"), clientMethodAskToConnect, null, null)
        } catch (ignored: Exception) {
        }

    }

    //Ignore can be private!
    fun getClientVersionData(context: Context): ClientVersionData? {

        CLog.log(logTag, "getClientVersionData()")
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
            CLog.log(logTag, "getClientVersionData() -> clientVersionData: $clientVersionData")
            clientVersionData
        } else {
            CLog.log(logTag, "getClientVersionData() -> clientVersionData: null")
            null
        }

    }
}