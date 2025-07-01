package com.nll.helper.recorder

import android.content.Context
import java.io.File

object CacheFileProvider {
    fun provideCacheFile(context: Context, fileName: String) = File(getExternalCacheDirectory(context), fileName)

    fun getExternalCacheDirectory(context: Context): File {
        /**
         * We used to use externalCacheDir but noticed that Android randomly deletes our cache files if recording time is long
         */
        return File(context.getExternalFilesDir(null), "recordings").also { folder ->
            if (folder.exists().not()) {
                folder.mkdirs()
            }
        }
    }


}