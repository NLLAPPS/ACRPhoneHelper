package com.nll.helper.update

import android.content.Context
import android.content.Intent
import com.nll.helper.recorder.CLog
import com.nll.helper.update.contract.IDownloadUrlOpener
import com.nll.helper.update.downloader.AppVersionData
import com.nll.helper.update.downloader.UpdateActivity
import com.nll.helper.update.version.RemoteAppVersion


object DownloadUrlOpenerImpl : IDownloadUrlOpener {
    private const val logTag = "DownloadUrlOpenerImpl"
    override fun getOpenDownloadUrlIntent(
        context: Context,
        remoteAppVersion: RemoteAppVersion
    ): Intent {

        CLog.log(logTag, "getOpenDownloadUrlIntent -> remoteAppVersion: $remoteAppVersion")

        val appVersionData = AppVersionData(
            remoteAppVersion.downloadUrl,
            remoteAppVersion.versionCode,
            remoteAppVersion.whatsNewMessage
        )
        return appVersionData.toIntent(Intent(context, UpdateActivity::class.java))
    }

    override fun openDownloadUrl(context: Context, remoteAppVersion: RemoteAppVersion) {
        if (CLog.isDebug()) {
            CLog.log(logTag, "openDownloadUrl -> remoteAppVersion: $remoteAppVersion")
        }
        val appVersionData = AppVersionData(
            remoteAppVersion.downloadUrl,
            remoteAppVersion.versionCode,
            remoteAppVersion.whatsNewMessage
        )
        UpdateActivity.start(context, appVersionData)
    }
}