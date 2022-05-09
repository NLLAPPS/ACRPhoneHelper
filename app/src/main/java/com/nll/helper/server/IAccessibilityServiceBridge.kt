package com.nll.helper.server

import android.content.Context

/**
 * Implementation for server is provided.
 * Client should implement no-op version of this interface in the same package
 *
 * While it won't be used, due to nature of AIDL, server package has to be in client code.
 * We use bridge package to provide no-op version of this interface to client app.
 */
interface IAccessibilityServiceBridge {
    fun isHelperServiceEnabled(context: Context): Boolean

}