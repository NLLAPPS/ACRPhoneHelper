package com.nll.helper.support

import android.content.Context
import android.database.ContentObserver
import android.provider.Settings
import android.util.Log

object AccessibilityChangeObserver {
    private const val logTag = "CR_AccessibilityChangeObserver"
    fun registerAccessibilityServiceChangeContentObserver(context: Context) {
        Log.i(logTag, "registerAccessibilityServiceChangeContentObserver()")

        context.contentResolver.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES), false, object : ContentObserver(null) {
            override fun onChange(self: Boolean) {
                val isAccessibilityServiceEnabledNow = AccessibilityCallRecordingService.isHelperServiceEnabled(context)
                if (!isAccessibilityServiceEnabledNow) {
                    Log.i(logTag, "onChange -> Warn user that AccessibilityService is not enabled on CallRecordingSupportType that needs it")
                    AccessibilityCallRecordingService.postEnableHelperServiceNotificationAndToast(context, false)
                }
                if(isAccessibilityServiceEnabledNow){
                    AccessibilityCallRecordingService.startHelperServiceIfIsNotRunning(context)
                }
            }
        })
    }

}