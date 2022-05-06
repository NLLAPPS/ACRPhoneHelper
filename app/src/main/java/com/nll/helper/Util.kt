package com.nll.helper


import android.content.Context


object Util {
    const val notificationId = 1
    const val updateNotificationId = 2
    const val permissionNotificationId = 3
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

}