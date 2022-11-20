package com.nll.helper.update.downloader

import android.content.Context
import android.content.pm.PackageInfo
import com.nll.helper.recorder.CLog
import com.nll.helper.update.HttpProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.net.HttpURLConnection

class FileDownloader() {
    private val logTag = "FileDownloader"
    private val bufferLengthBytes = 1024 * 8

    companion object {
        fun getBaseFolder(context: Context): File {
            val childFolder = "apks"
            val baseFolder = File(context.externalCacheDir, childFolder)
            if (!baseFolder.exists()) {
                baseFolder.mkdirs()
            }
            return baseFolder
        }

        suspend fun getPackageInfoFromApk(context: Context, downloadedApk: File): PackageInfo? = withContext(Dispatchers.IO) {
            try {
                context.packageManager.getPackageArchiveInfo(downloadedApk.absolutePath, 0)
            } catch (e: Exception) {
                CLog.logPrintStackTrace(e)
                null
            }
        }
    }

    sealed class DownloadStatus {
        class Progress(val percent: Int, val bytesCopied: Int, val totalBytes: Long) : DownloadStatus()
        class Completed(val downloadedFile: File, val packageInfo: PackageInfo) : DownloadStatus()
        class Error(val message: Message) : DownloadStatus() {

            sealed class Message {
                class GenericError(val message: String) : Message()
                class ServerError(val responseCode: Int) : Message()
                object LowerVersion : Message()
                object MalformedFile : Message()
                object UnableToRenameDownload : Message()

                override fun toString() = when (this) {
                    LowerVersion -> "LowerVersion"
                    MalformedFile -> "MalformedFile"
                    is ServerError -> "ServerError($responseCode)"
                    UnableToRenameDownload -> "UnableToRenameDownload"
                    is GenericError -> "GenericError(message: $message)"
                }
            }

        }

        override fun toString() = when (this) {
            is Completed -> "Completed(downloadedFile: $downloadedFile)"
            is Error -> "Error($message)"
            is Progress -> "Progress(percent: $percent, bytesCopied: $bytesCopied, totalBytes: $totalBytes)"
        }
    }


    private fun getTempFile(context: Context): File {
        val tempFile = File(getBaseFolder(context), "tmp.apk")
        if (tempFile.exists()) {
            tempFile.delete()
        }
        return tempFile
    }

    private fun clearOldApks(context: Context, currentVersionName: String) {
        try {
            if (CLog.isDebug()) {
                CLog.log(logTag, "clearOldApks() -> currentVersionName $currentVersionName")
            }
            getBaseFolder(context).listFiles()?.filter { it.path.endsWith(".apk") && it.name.lowercase() != currentVersionName }?.forEach {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "clearOldApks() -> Deleting $it")
                }
                it.delete()

            }
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
        }

    }

    /**
     * Double needed as calculation can go ver Int.Max
     */
    private fun calculatePercentage(obtained: Double, total: Double) = (obtained * 100 / total).toInt()
    suspend fun download(context: Context, downloadUrl: String, targetFile: File): Flow<DownloadStatus> = flow {

        clearOldApks(context, targetFile.name.lowercase())

        val tempFile = getTempFile(context)
        if (CLog.isDebug()) {
            CLog.log(logTag, "download() -> downloadUrl: $downloadUrl, targetFile: $targetFile, tempFile: $tempFile")
        }
        try {
            val request = Request.Builder().url(downloadUrl).build()
            val response = HttpProvider.provideOkHttpClient().newCall(request).execute()
            val body = response.body
            val responseCode = response.code
            if (responseCode >= HttpURLConnection.HTTP_OK &&
                responseCode < HttpURLConnection.HTTP_MULT_CHOICE &&
                body != null
            ) {
                val length = body.contentLength()
                body.byteStream().apply {
                    tempFile.outputStream().use { fileOut ->
                        var bytesCopied = 0
                        val buffer = ByteArray(bufferLengthBytes)
                        var bytes = read(buffer)
                        while (bytes >= 0) {
                            fileOut.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            bytes = read(buffer)

                            val percent = calculatePercentage(bytesCopied.toDouble(), length.toDouble())
                            /*if (CLog.isDebug()) {
                                CLog.log(logTag, "download() -> percent: $percent, bytesCopied: $bytesCopied, length: $length")
                            }*/
                            emit(DownloadStatus.Progress(percent, bytesCopied, length))
                        }
                    }
                    if (CLog.isDebug()) {
                        CLog.log(logTag, "download() -> Completed. Renaming from $tempFile to $targetFile")
                    }
                    val renamed = tempFile.renameTo(targetFile)
                    if (renamed) {
                        val packageInfo = getPackageInfoFromApk(context, targetFile)
                        if (packageInfo != null) {
                            if (CLog.isDebug()) {
                                CLog.log(logTag, "download() -> Renaming completed. Emitting DownloadStatus.Completed")
                            }
                            emit(DownloadStatus.Completed(targetFile, packageInfo))
                        } else {
                            if (CLog.isDebug()) {
                                CLog.log(logTag, "download() -> Target file was malformed! Delete it")
                            }
                            targetFile.delete()
                            emit(DownloadStatus.Error(DownloadStatus.Error.Message.MalformedFile))
                        }
                    } else {
                        val packageInfo = getPackageInfoFromApk(context, targetFile)
                        if (packageInfo != null) {
                            if (CLog.isDebug()) {
                                CLog.log(logTag, "download() -> Cannot rename downloaded file!. Emitting temp file")
                            }
                            emit(DownloadStatus.Completed(tempFile, packageInfo))
                        } else {
                            if (CLog.isDebug()) {
                                CLog.log(logTag, "download() -> Temp file was malformed! Delete it")
                            }
                            tempFile.delete()
                            emit(DownloadStatus.Error(DownloadStatus.Error.Message.MalformedFile))
                        }
                    }
                }
            } else {
                if (CLog.isDebug()) {
                    CLog.log(logTag, "download() -> Download error. responseCode: $responseCode")
                }
                emit(DownloadStatus.Error(DownloadStatus.Error.Message.ServerError(responseCode)))
            }
        } catch (e: Exception) {
            if (CLog.isDebug()) {
                CLog.logPrintStackTrace(e)
            }
            emit(DownloadStatus.Error(DownloadStatus.Error.Message.GenericError(e.message ?: "NULL")))
        }
    }

}