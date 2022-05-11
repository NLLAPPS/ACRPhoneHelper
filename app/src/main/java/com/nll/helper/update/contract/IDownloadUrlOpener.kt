package com.nll.helper.update.contract

import android.content.Context
import android.content.Intent
import com.nll.helper.update.version.RemoteAppVersion

interface IDownloadUrlOpener {
    fun getOpenDownloadUrlIntent(context: Context, remoteAppVersion: RemoteAppVersion): Intent
    fun openDownloadUrl(context: Context, remoteAppVersion: RemoteAppVersion)
}