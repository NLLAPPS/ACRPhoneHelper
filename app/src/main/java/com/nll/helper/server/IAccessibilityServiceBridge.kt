package com.nll.helper.server

import android.content.Context

interface IAccessibilityServiceBridge {
    fun isHelperServiceEnabled(context: Context): Boolean

}