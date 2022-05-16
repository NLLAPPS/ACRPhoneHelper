package com.nll.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object StoreConfigImpl : IStoreConfig {
    override fun openACRPhoneDownloadLink(context: Context, packageName: String) = try {
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        )
            .let(context::startActivity)
        true

    } catch (ignored: ActivityNotFoundException) {
        try {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
                .let(context::startActivity)
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    override fun canLinkToWebSite() = true
    override fun canLinkToGooglePlayStore() = true
    override fun getUpdateCheckUrl() = "https://acr.app/version-app-gallery.json"
    override fun requiresProminentPrivacyPolicyDisplay() = true
    override fun getPrivacyPolicyUrl()= "https://acr.app/policy.htm"
}