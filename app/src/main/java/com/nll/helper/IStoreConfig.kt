package com.nll.helper

import android.content.Context

interface IStoreConfig {
    fun openACRPhoneDownloadLink(context: Context, packageName: String): Boolean
    fun canLinkToWebSite(): Boolean
    fun getUpdateCheckUrl(): String
}