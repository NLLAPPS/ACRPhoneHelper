package com.nll.helper.util


import android.content.Context
import com.nll.helper.recorder.CLog


object Util {
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun getVersionCode(context: Context): Long {
        return try {
            context.applicationContext.packageManager.getPackageInfo(context.applicationContext.packageName, 0).longVersionCode
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
            0L
        }
    }
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.applicationContext.packageManager.getPackageInfo(packageName, 0)!=null
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
            false
        }
    }

    fun getVersionName(context: Context): String {
        return try {
            context.applicationContext.packageManager.getPackageInfo(context.applicationContext.packageName, 0).versionName ?: "Cannot get version name!"
        } catch (e: Exception) {
            CLog.logPrintStackTrace(e)
            "Cannot get version name!"
        }
    }

}