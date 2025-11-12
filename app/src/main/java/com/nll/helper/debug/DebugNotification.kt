package com.nll.helper.debug

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.nll.helper.R
import com.nll.helper.util.extGetThemeAttrColor
import io.karn.notify.Notify
import io.karn.notify.entities.Payload
import androidx.appcompat.R as AppCompatResources


object DebugNotification {
    private fun alertPayload(context: Context) = Payload.Alerts(
        channelKey = "cb-common-notifications",
        lockScreenVisibility = NotificationCompat.VISIBILITY_PUBLIC,
        channelName = context.getString(R.string.debug_log),
        channelDescription = context.getString(R.string.debug_log),
        channelImportance = Notify.IMPORTANCE_LOW,
        showBadge = false

    )

    fun getDebugEnabledNotification(context: Context, startIntent: PendingIntent): NotificationCompat.Builder {
        val alertPayload =alertPayload(context)
        return Notify.with(context)
            .meta {
                cancelOnClick = false
                sticky = true
                clickIntent = startIntent
                group = "debug-enabled"
            }
            .alerting(alertPayload.channelKey) {
                lockScreenVisibility = alertPayload.lockScreenVisibility
                channelName = alertPayload.channelName
                channelDescription = alertPayload.channelDescription
                channelImportance = alertPayload.channelImportance

            }
            .header {
                icon = R.drawable.notification_debug
                color = context.extGetThemeAttrColor(AppCompatResources.attr.colorPrimary)
                showTimestamp = true
            }
            .content {
                title = context.getString(R.string.debug_log)

            }
            .asBuilder()
    }
}