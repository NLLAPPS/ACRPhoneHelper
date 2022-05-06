package com.nll.helper.update

import com.nll.helper.update.version.RemoteAppVersion


sealed class UpdateResult {
    class Required(val remoteAppVersion: RemoteAppVersion, val forceUpdate: Boolean) : UpdateResult()

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