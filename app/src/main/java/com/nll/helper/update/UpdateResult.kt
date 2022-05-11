package com.nll.helper.update

import android.content.Context
import com.nll.helper.update.version.RemoteAppVersion


sealed class UpdateResult {
    class Required(val remoteAppVersion: RemoteAppVersion, val forceUpdate: Boolean) : UpdateResult(){
        fun openDownloadUrl(context: Context) {
            DownloadUrlOpenerImpl.openDownloadUrl(context, remoteAppVersion)
        }
    }

    /**
     * No update required
     */
    object NotRequired : UpdateResult()

    override fun toString(): String {
        return when (this) {
            is NotRequired -> "NotRequired"
            is Required -> "Required(forceUpdate: $forceUpdate)"

        }
    }

}