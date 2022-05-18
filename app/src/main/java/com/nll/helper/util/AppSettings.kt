package com.nll.helper.util


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

    /**
     *
     * As per
     * https://developer.android.com/guide/components/activities/background-starts
     * https://developer.android.com/about/versions/oreo/background
     *
     * Since client connects to server with AIDL bound service, helper app is excluded from background restrictions
     * as long as client app is in the background. In our case, since our app is bound by system telecom service
     * we are safe
     *
     * We however still have give option to users to start foreground service just in case some devices behave badly
     *
     */
    var actAsForegroundService: Boolean by booleanPref(
        key = "actAsForegroundService",
        default = false
    )
    var privacyPolicyAccepted: Boolean by booleanPref(
        key = "privacyPolicyAccepted",
        default = false
    )

}