package com.nll.helper.support

import android.content.Context
import android.util.Log
import androidx.startup.Initializer


/**
 * Used to observer Accessibility Service changes
 */
class AccessibilityChangeObserverInitiator : Initializer<Unit> {
    private val logTag = "CR_AccessibilityChangeObserverInitiator"

    override fun create(context: Context) {
        Log.i(logTag, "create()")
        AccessibilityChangeObserver.registerAccessibilityServiceChangeContentObserver(context)

    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}