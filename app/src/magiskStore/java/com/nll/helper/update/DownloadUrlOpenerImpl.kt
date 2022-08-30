package com.nll.helper.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.nll.helper.R
import com.nll.helper.recorder.CLog
import com.nll.helper.update.contract.IDownloadUrlOpener
import com.nll.helper.update.version.RemoteAppVersion


object DownloadUrlOpenerImpl : IDownloadUrlOpener {
    private const val logTag = "DownloadUrlOpenerImpl"
    override fun getOpenDownloadUrlIntent(
        context: Context,
        remoteAppVersion: RemoteAppVersion
    ): Intent {

        CLog.log(logTag, "getOpenDownloadUrlIntent -> remoteAppVersion: $remoteAppVersion")

        return Intent(Intent.ACTION_VIEW, Uri.parse(remoteAppVersion.downloadUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }
    }

    override fun openDownloadUrl(context: Context, remoteAppVersion: RemoteAppVersion) {

        CLog.log(logTag, "openDownloadUrl -> remoteAppVersion: $remoteAppVersion")


        try {
            val urlToOpen = Uri.parse(remoteAppVersion.downloadUrl)
            try {
                /**
                 * TODO Do we need FLAG_ACTIVITY_NEW_DOCUMENT
                 * An activity that handles documents can use this attribute so that with every document you open you launch a separate instance of the same activity.
                 * If you check your recent apps, then you will see various screens of the same activity of your app, each using a different document.
                 */
                val openIntent = Intent(Intent.ACTION_VIEW, urlToOpen).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                }
                context.startActivity(openIntent)
            } catch (e: Exception) {
                CLog.logPrintStackTrace(e)
                Toast.makeText(context, R.string.no_url_handle, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
            Toast.makeText(context, R.string.url_error, Toast.LENGTH_LONG).show()
        }
    }
}