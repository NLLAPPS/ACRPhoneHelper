package com.nll.helper.update

import com.nll.helper.update.version.RemoteAppVersion


interface UpdateRequest {
    fun getUpdateCheckUrl(): String
    fun saveRemoteVersion(remoteVersionJson: String)
    fun saveLastUpdateCheckTime()
    fun getUpdateRequestResult(): UpdateResult
    fun getRemoteVersionInfo(): RemoteAppVersion?
    fun shouldCheckUpdate(): Boolean
    //Some delay different then MessageRequest so that we do not bombard the server with 2 request on first install
    fun getUpdateCheckDelayInMillis() = 200L
}