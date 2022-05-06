package com.nll.helper


import android.content.Context
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.KotprefModel

object AppSettings : KotprefModel() {
    fun initIfNeeded(context: Context) {
        if (!Kotpref.isInitialized) {
            Kotpref.init(context.applicationContext)
        }
    }
    //For mapping KotPref to sharedPref used in SettingsFragment
    //Mapping to preference fragments done in BasePreferenceCompatFragment
    override val kotprefName: String = "app-recorder"


    var remoteVersionJson: String by stringPref(
        key = "remoteVersionJson",
        default = ""
    )
    var remoteVersionLastUpdateCheck: Long by longPref(
        key = "remoteVersionLastUpdateCheck",
        default = 0
    )

}