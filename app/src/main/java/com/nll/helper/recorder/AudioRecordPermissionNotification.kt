package com.nll.helper.recorder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.nll.helper.Constants
import com.nll.helper.R
import com.nll.helper.ui.MainActivity
import io.karn.notify.Notify
import io.karn.notify.entities.Payload

object AudioRecordPermissionNotification {
    private const val logTag = "AudioRecordPermissionNotification"
    private fun getChannel(context: Context) = Payload.Alerts(
        channelKey = "helper_permission_notification",
        lockScreenVisibility = NotificationCompat.VISIBILITY_PUBLIC,
        channelName = context.getString(R.string.permissions_title),
        channelDescription = context.getString(R.string.permissions_title),
        channelImportance = Notify.IMPORTANCE_HIGH,
        showBadge = false

    )

    fun show(context: Context) {
        CLog.log(logTag, "showNeedsAudioRecordPermissionNotification()")
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingOpenIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alertPayload = getChannel(context)

        Notify.with(context)
            .alerting(alertPayload.channelKey) {
                lockScreenVisibility = alertPayload.lockScreenVisibility
                channelName = alertPayload.channelName
                channelDescription = alertPayload.channelDescription
                channelImportance = alertPayload.channelImportance
            }
            .header {
                icon = R.drawable.ic_warning_24
                color = Color.RED
                showTimestamp = true
            }
            .meta {
                group = "helper_permission_notification"
                sticky = true
                cancelOnClick = true
                clickIntent = pendingOpenIntent
            }
            .content {
                title = context.getString(R.string.audio_record_permission)
                text = context.getString(R.string.call_rec_permissions_message)
            }.show(Constants.permissionNotificationId)
    }
}