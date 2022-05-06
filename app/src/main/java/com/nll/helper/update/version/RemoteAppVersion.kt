package com.nll.helper.update.version

import org.json.JSONObject
/*
Expected Json response format is
{
  "versionCode": 1,
  "downloadUrl": "https://play.google.com/store/apps/details?id=com.nll.cb",
  "whatsNewMessage": "",
  "forceUpdate": false
}

 */
class RemoteAppVersion(json: String) : JSONObject(json) {
    val versionCode = this.optInt("versionCode")
    val downloadUrl: String = this.optString("downloadUrl")
    val whatsNewMessage: String = this.optString("whatsNewMessage")
    val forceUpdate: Boolean = this.optBoolean("forceUpdate")

    override fun toString(): String {
        return "RemoteVersionInfo(versionCode=$versionCode, downloadUrl='$downloadUrl', whatsNewMessage='$whatsNewMessage', forceUpdate='$forceUpdate')"
    }

}