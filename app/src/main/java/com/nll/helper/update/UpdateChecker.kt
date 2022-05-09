package com.nll.helper.update

import android.content.Context
import com.nll.helper.AppSettings
import com.nll.helper.recorder.CLog
import com.nll.helper.update.version.RemoteAppVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object UpdateChecker {
    private const val logTag = "UpdateChecker"


    suspend fun checkUpdate(context: Context): UpdateResult = withContext(Dispatchers.IO) {
        //Init settings
        AppSettings.initIfNeeded(context)
        val updateRequest = UpdateRequest()

        if (updateRequest.shouldCheckUpdate()) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "checkUpdate() -> Check updates from server after delay and return up to date result")
            }
            delay(updateRequest.getUpdateCheckDelayInMillis())
            realDownloadUpdate(updateRequest)
        } else {
            if (CLog.isDebug()) {
                CLog.log(logTag, "checkUpdate() -> No update check needed. Return previous update result")
            }
        }

        updateRequest.getUpdateRequestResult()
    }


    private fun realDownloadUpdate(updateRequest: UpdateRequest) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "realDownloadUpdate")
        }
        try {
            HttpProvider.provideOkHttpClient().newCall(HttpProvider.provideRequestForOwnServer(updateRequest.getUpdateCheckUrl()).build()).execute().use { response ->
                if (response.isSuccessful) {
                    if (CLog.isDebug()) {
                        CLog.log(logTag, "realDownloadUpdate -> response.isSuccessful. Save last update check time")
                    }
                    updateRequest.saveLastUpdateCheckTime()

                    response.body?.let { body ->
                        val bodyString = body.string()
                        if (CLog.isDebug()) {
                            CLog.log(logTag, "realDownloadUpdate -> response.body: $bodyString")
                        }

                        try {
                            val remoteVersion = RemoteAppVersion(bodyString)
                            if (CLog.isDebug()) {
                                CLog.log(logTag, "realDownloadUpdate success. Save remoteVersion: $remoteVersion")
                            }
                            updateRequest.saveRemoteVersion(bodyString)
                        } catch (e: Exception) {
                            if (CLog.isDebug()) {
                                CLog.log(logTag, "realDownloadUpdate failed while parsing response. Will try again later")
                            }
                            CLog.logPrintStackTrace(e)
                        }

                    }
                }
            }
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
        }
    }

}