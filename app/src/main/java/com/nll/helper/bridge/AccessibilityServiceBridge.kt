package com.nll.helper.bridge

import android.content.Context
import com.nll.helper.recorder.CLog
import com.nll.helper.server.IAccessibilityServiceBridge
import com.nll.helper.support.AccessibilityCallRecordingService


class AccessibilityServiceBridge : IAccessibilityServiceBridge {
    private val logTag = "CR_AccessibilityServiceBridge"
    override fun isHelperServiceEnabled(context: Context): Boolean {
        val isHelperServiceEnabled = AccessibilityCallRecordingService.isHelperServiceEnabled(context)
        CLog.log(logTag, "isHelperServiceEnabled -> isHelperServiceEnabled: $isHelperServiceEnabled")
        if (isHelperServiceEnabled) {
            AccessibilityCallRecordingService.startHelperServiceIfIsNotRunning(context)
        }
        return isHelperServiceEnabled
    }
}