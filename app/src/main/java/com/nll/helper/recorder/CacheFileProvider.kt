package com.nll.helper.recorder

import android.content.Context
import java.io.File

object CacheFileProvider {
    fun provideCacheFile(context: Context, fileName: String) = File(getExternalCacheDirectory(context), fileName)

    fun getExternalCacheDirectory(context: Context): File {
        /**
         * Use getExternalFilesDir instead of externalCacheDir.
         * There have been instances where Android cleared cache dir while we are downloading the apk
         */
        return File(context.getExternalFilesDir(null), "recordings").also { folder ->
            if (folder.exists().not()) {
                folder.mkdirs()
            }
        }
    }


}