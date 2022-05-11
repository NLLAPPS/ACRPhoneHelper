package com.nll.helper.update.downloader


import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.nll.helper.recorder.CLog
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class AppVersionData(val downloadUrl: String, val downloadVersion: Int, val versionNotes: String?) : Parcelable {


    fun getDestinationFile(context: Context): File {
        return File(FileDownloader.getBaseFolder(context), "$downloadVersion.apk")
    }


    fun toIntent(intent: Intent) = intent.apply {
        putExtra(argKey, this@AppVersionData as Parcelable)
    }

    companion object {
        private const val argKey = "app-version-data"
        fun fromIntent(intent: Intent?) = try {
            intent?.getParcelableExtra<AppVersionData>(argKey)
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
            null
        }
    }
}