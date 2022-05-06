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
object ServerContentProviderHelper {
    private const val logTag = "CR_ServerContentProviderHelper"
    private var helperServiceEnabledLastUpdated = 0L
    private const val helperServiceEnabledUpdateCheckInMillis = 6000
    private var isHelperServiceEnabled = false
    const val downloadMatchPath = "download"
    private const val serverPackageName = "com.nll.helper"

    //Ignore can be private!
    const val methodGetServerVersionData = "server_version_data"

    //Ignore can be private!
    const val methodIsHelperEnabled = "is_helper_enabled"

    //Ignore can be private!
    const val methodStartHelper = "start_helper"

    //Ignore can be private!
    const val methodDeleteCacheFile = "delete_cache_file"

    //Ignore can be private!
    val authority = "com.nll.helper.ServerContentProvider" //Same as manifest

    private fun shouldUpdateIsHelperServiceEnabled() = (System.currentTimeMillis() - helperServiceEnabledLastUpdated) > helperServiceEnabledUpdateCheckInMillis

    fun getDownloadUri(): Uri = Uri.parse("content://${authority}").buildUpon().appendPath(downloadMatchPath).build()

    //Ignore can be private!
    fun getServerVersionData(context: Context): ServerVersionData? {

        Log.i(logTag, "getServerVersionData()")
        /**
         * Make sure we handle crash as user may not have helper installed
         */
        val cmdResult = try {
            context.contentResolver.call(Uri.parse("content://$authority"), methodGetServerVersionData, null, null)
        } catch (e: Exception) {
            null
        }
        return if (cmdResult != null) {
            val serverVersionData = ServerVersionData.fromBundle(cmdResult)
            Log.i(logTag, "getServerVersionData() -> serverVersionData: $serverVersionData")
            serverVersionData
        } else {
            Log.i(logTag, "getServerVersionData() -> serverVersionData: null")
            null
        }


    }

    //Ignore can be unused!
    fun startUIIfInstalled(context: Context): Boolean {
        val serverVersionData = getServerVersionData(context)
        val isInstalled = serverVersionData != null
        return if (isInstalled) {
            Log.i(logTag, "startUIIfInstalled()")
            try {
                context.startActivity(context.packageManager.getLaunchIntentForPackage(serverPackageName))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            true
        } else {
            false
        }
    }

    fun isHelperServiceEnabled(context: Context): Boolean {
        return if (shouldUpdateIsHelperServiceEnabled()) {
            Log.i(logTag, "isHelperServiceEnabled() -> Cache expired. Real Update")
            /**
             * Make sure we handle crash as user may not have helper installed
             */
            val cmdResult = try {
                context.contentResolver.call(Uri.parse("content://$authority"), methodIsHelperEnabled, null, null)
            } catch (e: Exception) {
                null
            }
            helperServiceEnabledLastUpdated = System.currentTimeMillis()
            isHelperServiceEnabled = cmdResult != null
            isHelperServiceEnabled
        } else {
            Log.i(logTag, "isHelperServiceEnabled() -> Cache valid. Return casched value")
            isHelperServiceEnabled
        }
    }

    fun startHelperServiceIfIsNotRunning(context: Context): Boolean {
        Log.i(logTag, "startHelperServiceIfIsNotRunning()")
        /**
         * Make sure we handle crash as user may not have helper installed
         */
        val cmdResult = try {
            context.contentResolver.call(Uri.parse("content://$authority"), methodStartHelper, null, null)
        } catch (e: Exception) {
            null
        }
        return cmdResult != null
    }

    fun deleteCacheFile(context: Context, recordingName: String): Boolean {
        Log.i(logTag, "deleteCacheFile()")
        /**
         * Make sure we handle crash as user may not have helper installed
         */
        val cmdResult = try {
            context.contentResolver.call(Uri.parse("content://$authority"), methodDeleteCacheFile, recordingName, null)
        } catch (e: Exception) {
            null
        }
        return cmdResult != null
    }
}