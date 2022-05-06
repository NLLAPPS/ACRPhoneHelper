package com.nll.helper

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import com.nll.helper.recorder.CacheFileProvider
import com.nll.helper.server.ServerContentProviderHelper
import com.nll.helper.server.ServerVersionData
import com.nll.helper.support.AccessibilityCallRecordingService
import java.io.File
import java.io.FileNotFoundException

class ServerContentProvider : ContentProvider() {
    private val logTag = "CR_ServerContentProvider"
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private val downloadMatchId = 1
    override fun onCreate(): Boolean {
        Log.i(logTag, "onCreate")
        uriMatcher.addURI(ServerContentProviderHelper.authority, "${ServerContentProviderHelper.downloadMatchPath}/*", downloadMatchId)
        return true
    }

    override fun call(method: String, methodArgument: String?, extras: Bundle?): Bundle? {
        Log.i(logTag, "call() -> method: $method")
        return when (method) {
            ServerContentProviderHelper.methodGetServerVersionData -> {

                Log.i(logTag, "call() -> Returning ServerVersionData")

                return ServerVersionData(BuildConfig.VERSION_CODE).toBundle()
            }
            ServerContentProviderHelper.methodIsHelperEnabled -> {
                context?.let {
                    if (AccessibilityCallRecordingService.isHelperServiceEnabled(it)) {
                        Log.i(logTag, "call() -> isHelperServiceEnabled() -> Returning True")
                        Bundle().apply {
                            putBoolean(ServerContentProviderHelper.methodIsHelperEnabled, true)
                        }
                    } else {
                        Log.i(logTag, "call() -> isHelperServiceEnabled() -> Returning Null")
                        null
                    }
                }
            }
            ServerContentProviderHelper.methodStartHelper -> {
                context?.let {
                    Log.i(logTag, "call() -> Starting AccessibilityCallRecordingService")
                    AccessibilityCallRecordingService.startHelperServiceIfIsNotRunning(it)
                }
                Bundle().apply {
                    putBoolean(ServerContentProviderHelper.methodStartHelper, true)
                }

            }
            ServerContentProviderHelper.methodDeleteCacheFile -> {
                var isDeleted = false
                Log.i(logTag, "call() -> methodArgument: $methodArgument")
                context?.let { ctx ->
                    methodArgument?.let { fileName ->
                        getFile(ctx, fileName).also { file ->
                            isDeleted = file.delete()
                            Log.i(logTag, "call() -> deleteResult: $isDeleted")
                        }
                    }
                }

                return Bundle().apply {
                    putBoolean(ServerContentProviderHelper.methodDeleteCacheFile, isDeleted)
                }
            }
            else -> {
                Log.i(logTag, "call() -> Unknown command")
                null
            }
        }

    }

    private fun getFile(context: Context, fileName: String) = File(CacheFileProvider.getExternalCacheDirectory(context), fileName)
    override fun query(incomingUri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {

        Log.i(logTag, "query -> uri: $incomingUri")

        //In case projection is null. Some apps like Telegram or TotalCommander does that
        val localProjection = projection ?: arrayOf(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE)

        context?.let { ctx ->
            when (uriMatcher.match(incomingUri)) {
                downloadMatchId -> {
                    Log.i(logTag, "downloadMatchId")
                    val file = getFile(ctx, incomingUri.lastPathSegment!!)
                    if (file.exists()) {
                        val matrixCursor = MatrixCursor(localProjection)
                        val rowBuilder = matrixCursor.newRow()
                        matrixCursor.columnNames.forEach { column ->
                            when {
                                column.equals(MediaStore.MediaColumns.DISPLAY_NAME, ignoreCase = true) -> {
                                    Log.i(logTag, "Projection is : DISPLAY_NAME. Return ${file.name}")
                                    rowBuilder.add(column, file.name)
                                }
                                column.equals(MediaStore.MediaColumns.SIZE, ignoreCase = true) -> {
                                    Log.i(logTag, "Projection is : SIZE. Return ${file.length()}")
                                    rowBuilder.add(column, file.length())
                                }
                                column.equals(MediaStore.MediaColumns.MIME_TYPE, ignoreCase = true) -> {
                                    Log.i(logTag, "Projection is : MIME_TYPE. Return audio/*")
                                    rowBuilder.add(column, "audio/*")
                                }
                                column.equals(MediaStore.MediaColumns.DATE_MODIFIED, ignoreCase = true) ||
                                        column.equals(MediaStore.MediaColumns.DATE_ADDED, ignoreCase = true) -> {
                                    Log.i(logTag, "Projection is : DATE_ADDED|DATE_MODIFIED. Return ${file.lastModified()}")
                                    rowBuilder.add(column, file.lastModified())
                                }
                            }
                        }
                        return matrixCursor
                    } else {
                        return null
                    }

                }
                else -> {
                    Log.i(logTag, "Cannot match")
                    return null
                }
            }
        } ?: return null

    }

    override fun openFile(incomingUri: Uri, mode: String): ParcelFileDescriptor? {
        Log.i(logTag, "openFile() -> Open file uri: $incomingUri")
        val match = uriMatcher.match(incomingUri)
        if (match != downloadMatchId) {
            Log.i(logTag, "openFile() -> Wrong Uri. $incomingUri does not match")
            throw FileNotFoundException(incomingUri.path)
        }
        context?.let { ctx ->
            val file = File(CacheFileProvider.getExternalCacheDirectory(ctx), incomingUri.lastPathSegment!!)
            Log.i(logTag, "openFile() -> File is ${file.absolutePath}. File size is ${file.length()}")
            return if (file.exists()) {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            } else {
                Log.i(logTag, "openFile() -> File $file not found")
                throw FileNotFoundException(file.toString())
            }
        } ?: throw Exception("openFile() -> Context was null!!!")


    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            downloadMatchId -> "audio/*"
            else -> ""
        }
    }

    override fun update(incomingUri: Uri, contentvalues: ContentValues?, s: String?, `as`: Array<String>?): Int {
        return 0
    }

    override fun delete(incomingUri: Uri, s: String?, `as`: Array<String>?): Int {
        return 0
    }

    override fun insert(uri: Uri, contentvalues: ContentValues?): Uri? {
        return null
    }

}