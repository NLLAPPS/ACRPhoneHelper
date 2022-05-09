package com.nll.helper.update

import com.nll.helper.AppSettings
import com.nll.helper.BuildConfig
import com.nll.helper.Constants
import com.nll.helper.recorder.CLog
import com.nll.helper.update.version.LocalAppVersion
import com.nll.helper.update.version.RemoteAppVersion
import java.util.concurrent.TimeUnit

internal class UpdateRequest {
    private val logTag = "UpdateRequest"

     fun getUpdateCheckUrl() = Constants.updateCheckUrl

     fun saveLastUpdateCheckTime() {
        if (CLog.isDebug()) {
            CLog.log(logTag, "saveLastUpdateCheckTime")
        }
        AppSettings.remoteVersionLastUpdateCheck = System.currentTimeMillis()
    }


     fun saveRemoteVersion(remoteVersionJson: String) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "saveRemoteVersion")
        }
        AppSettings.remoteVersionJson = remoteVersionJson

    }

     fun getUpdateRequestResult(): UpdateResult {
        if (CLog.isDebug()) {
            CLog.log(logTag, "getUpdateResult()")
        }
        return getRemoteVersionInfo()?.let { remoteVersionInfo ->
            if (CLog.isDebug()) {
                CLog.log(logTag, "getUpdateResult() -> remoteVersionInfo.versionCode: ${remoteVersionInfo.versionCode}, LocalAppVersion.versionCode: ${LocalAppVersion.versionCode}")
            }
            if (remoteVersionInfo.versionCode > LocalAppVersion.versionCode) {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "getUpdateResult -> UpdateResult.RequireUpdate(forceUpdate: ${remoteVersionInfo.forceUpdate})")
                }
                UpdateResult.Required(remoteVersionInfo, remoteVersionInfo.forceUpdate)
            } else {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "getUpdateResult -> UpdateResult.NotRequired")
                }
                UpdateResult.NotRequired
            }
        } ?: UpdateResult.NotRequired
    }

     private fun getRemoteVersionInfo(): RemoteAppVersion? {
        return try {
            RemoteAppVersion(AppSettings.remoteVersionJson)
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
            null
        }
    }

     fun shouldCheckUpdate(): Boolean {
        return if (BuildConfig.DEBUG) {
            if (CLog.isDebug()) {
                CLog.log(logTag, "shouldCheckUpdate() -> This is a Debug build. Return true")
            }
            true
        } else {
            val timeSinceLastCheckInHours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - AppSettings.remoteVersionLastUpdateCheck)
            timeSinceLastCheckInHours > 25
        }
    }

    //Some delay so that we do not bombard the server with requests on first install
     fun getUpdateCheckDelayInMillis(): Long {
        return 200
    }

}