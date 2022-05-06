package com.nll.helper.bridge

import android.content.Context
import android.util.Log
import com.nll.helper.server.IAccessibilityServiceBridge
import com.nll.helper.support.AccessibilityCallRecordingService

class AccessibilityServiceBridge : IAccessibilityServiceBridge {
    private val logTag = "CR_AccessibilityServiceBridge"
    override fun isHelperServiceEnabled(context: Context): Boolean {
        val isHelperServiceEnabled = AccessibilityCallRecordingService.isHelperServiceEnabled(context)
        Log.i(logTag, "isHelperServiceEnabled -> isHelperServiceEnabled: $isHelperServiceEnabled")
        if (isHelperServiceEnabled) {
            AccessibilityCallRecordingService.startHelperServiceIfIsNotRunning(context)
        }
        return isHelperServiceEnabled
    }
}