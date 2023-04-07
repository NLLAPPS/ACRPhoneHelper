package com.nll.helper.support

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import com.nll.helper.*
import com.nll.helper.recorder.CLog
import com.nll.helper.ui.MainActivity
import com.nll.helper.update.DownloadUrlOpenerImpl
import com.nll.helper.update.UpdateChecker
import com.nll.helper.update.UpdateResult
import com.nll.helper.util.AppSettings
import com.nll.helper.util.LiveEvent
import com.nll.helper.util.extNotificationManager
import io.karn.notify.Notify
import io.karn.notify.entities.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AccessibilityCallRecordingService : AccessibilityService(), CoroutineScope {
    private val job = Job()
    override val coroutineContext = Dispatchers.IO + job


    private fun getChannel(context: Context) = Payload.Alerts(
        channelKey = "helper_notification",
        lockScreenVisibility = NotificationCompat.VISIBILITY_PUBLIC,
        channelName = context.getString(R.string.accessibility_service_name),
        channelDescription = context.getString(R.string.accessibility_service_name),
        channelImportance = Notify.IMPORTANCE_MIN,
        showBadge = false

    )

    private fun startAsForegroundServiceWithNotification(context: Context) {

        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingOpenIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alertPayload = getChannel(context)

        val notification = Notify.with(context)
            .alerting(alertPayload.channelKey) {
                lockScreenVisibility = alertPayload.lockScreenVisibility
                channelName = alertPayload.channelName
                channelDescription = alertPayload.channelDescription
                channelImportance = alertPayload.channelImportance
            }
            .header {
                icon = R.drawable.ic_helper_notification2
                showTimestamp = false
            }
            .meta {
                group = "helper_notification"
                sticky = true
                cancelOnClick = false
                clickIntent = pendingOpenIntent
            }
            .content {
                text = context.getString(R.string.helper_service_notification)
            }.asBuilder()

        startForeground(Constants.foregroundNotificationId, notification.build())
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return super.onKeyEvent(event)
    }


    override fun onCreate() {
        super.onCreate()
        CLog.log(logTag, "onCreate()")
        checkUpdates()
    }

    private fun checkUpdates() {
        launch {
            CLog.log(logTag, "onCreate() -> Check for updates")
            UpdateChecker.checkUpdate(applicationContext).let { updateResult ->
                if (CLog.isDebug()) {
                    CLog.log(logTag, "onVersionUpdateResult -> updateResult: $updateResult")
                }
                when (updateResult) {
                    is UpdateResult.Required -> {

                        if (updateResult.forceUpdate) {
                            showUpdateNotification(applicationContext, updateResult)
                        }
                    }
                    is UpdateResult.NotRequired -> {
                        if (CLog.isDebug()) {
                            CLog.log(logTag, "onVersionUpdateResult -> NotRequired")
                        }
                    }
                }
            }
        }
    }

    private fun showUpdateNotification(context: Context, updateResult: UpdateResult.Required) {
        val launchIntent =
            DownloadUrlOpenerImpl.getOpenDownloadUrlIntent(context, updateResult.remoteAppVersion)

        val pendingOpenIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alertPayload = getChannel(context)

        Notify.with(context)
            .alerting(alertPayload.channelKey) {
                lockScreenVisibility = alertPayload.lockScreenVisibility
                channelName = alertPayload.channelName
                channelDescription = alertPayload.channelDescription
                channelImportance = alertPayload.channelImportance
            }
            .header {
                icon = R.drawable.ic_info_24dp
                showTimestamp = true
            }
            .meta {
                group = "helper_update_notification"
                sticky = true
                cancelOnClick = true
                clickIntent = pendingOpenIntent
            }
            .content {
                title = context.getString(R.string.new_version_found)
                text =
                    updateResult.remoteAppVersion.whatsNewMessage.ifEmpty { context.getString(R.string.forced_update_message_generic) }
            }.show(Constants.updateNotificationId)
    }


    override fun onServiceConnected() {
        CLog.log(logTag, "onServiceConnected()")

        isRunning = true

        toggleNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            /**
             * Send AccessibilityServicesChangedEvent As soon as system binds to this service
             * On Android 11 onChange sent by Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES content URI are delayed around 4-5 seconds. We want to make sure observers of ContentObservers#accessibilityServicesChangedEvent pickup changes as quick as possible
             *
             * NOTE: This will cause  observers to load data twice! Once this posted, and then again when we receive actual change for Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
             * However, it should not be an issue as this process does not happen often.
             *
             */
            CLog.log(
                logTag,
                "onServiceConnected() -> Call sendAccessibilityServicesChangedEvent(true)"
            )
            sendAccessibilityServicesChangedEvent(true)
        }
    }

    private fun toggleNotification() {
        /**
         *
         * We have just discovered that Android requires apps to have an ongoing foreground notification in order to record audio!
         * Even if app has android.permission.CAPTURE_AUDIO_OUTPUT
         * So we are forcing app to show permanent ongoing notification and hiding control away from user in root mode.
         * While ideally we should show notification only when recording, it requires us to do a large factoring which we do not have time for at the moment.
         *
         * There is no such problem when AccessibilityService is used due to nature of AccessibilityService Api.
         *
         */
        if (AppSettings.actAsForegroundService || App.hasCaptureAudioOutputPermission()) {
            startAsForegroundServiceWithNotification(applicationContext)
        } else {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CLog.log(logTag, "onStartCommand()")

        isRunning = true
        /**
         *
         * Important to make sure isHelperServiceEnabled because we might be starting this service before system binds to it.
         * For example when actAsForegroundService is true
         *
         */
        sendAccessibilityServicesChangedEvent(isHelperServiceEnabled(applicationContext))
        toggleNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        CLog.log(logTag, "onDestroy()")

        isRunning = false
        sendAccessibilityServicesChangedEvent(false)
        job.cancel()

    }


    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        CLog.log(logTag, "event: $event")

    }

    override fun onInterrupt() {
        CLog.log(logTag, "onInterrupt()")

    }


    companion object {
        private const val logTag: String = "CR_AccessibilityCallRecordingService"

        fun start(context: Context) {
            context.startService(Intent(context.applicationContext, AccessibilityCallRecordingService::class.java))
        }


        // Do not integrate to isHelperServiceEnabled() as running service might not be stopped instantly when user switches it off in Accessibility settings
        private var isRunning = false

        //https://stackoverflow.com/a/63214655
        private fun addHighlightInTheList(context: Context, intent: Intent): Intent {
            intent.apply {
                //Important as we call this from non activity classes
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)

                val showArgs = context.packageName.toString() + "/" + AccessibilityCallRecordingService::class.java.name
                val extraFragmentKeyArg = ":settings:fragment_args_key"
                putExtra(extraFragmentKeyArg, showArgs)
                val bundle = Bundle().apply {
                    putString(extraFragmentKeyArg, showArgs)
                }
                putExtra(":settings:show_fragment_args", bundle)
            }
            return intent
        }

        private fun getDefaultOpenAccessibilitySettingsIntent(context: Context): Intent {
            return addHighlightInTheList(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            })
        }

        private fun openSamsungIntentBelowAndroidR(context: Context) {
            //This Does not work on Android 11+ there is no such action. We have tried with         setClassName("com.samsung.accessibility", "com.samsung.accessibility.core.winset.activity.SubSettings") but cannot start it.
            //We get java.lang.SecurityException: Permission Denial: starting Intent { flg=0x10000000 cmp=com.samsung.accessibility/.core.winset.activity.SubSettings } from ProcessRecord{a0cc8de 32732:com.nll.cb/u0a358} (pid=32732, uid=10358) not exported from uid 1000
            val samsungDeepLink = addHighlightInTheList(
                context,
                Intent("com.samsung.accessibility.installed_service")
            )
            return try {
                context.startActivity(samsungDeepLink)
            } catch (e: Exception) {
                context.startActivity(getDefaultOpenAccessibilitySettingsIntent(context))
            }


        }

        fun openHelperServiceSettingsIfNeeded(
            context: Context,
            openWithoutCheckingIfEnabled: Boolean
        ): Boolean {
            CLog.log(logTag, "openHelperServiceSettingsIfNeeded()")

            if (!isHelperServiceEnabled(context) || openWithoutCheckingIfEnabled) {
                val isSamsungAndBelowAndroidR =
                    Build.MANUFACTURER.uppercase() == "SAMSUNG" && Build.VERSION.SDK_INT < Build.VERSION_CODES.R

                if (isSamsungAndBelowAndroidR) {
                    openSamsungIntentBelowAndroidR(context)
                } else {
                    context.startActivity(getDefaultOpenAccessibilitySettingsIntent(context))
                }

                return true
            }

            return false
        }

        /**
         * https://stackoverflow.com/a/40568194
         *
         * Based on [com.android.settingslib.accessibility.AccessibilityUtils.getEnabledServicesFromSettings]
         * @see [AccessibilityUtils](https://github.com/android/platform_frameworks_base/blob/d48e0d44f6676de6fd54fd8a017332edd6a9f096/packages/SettingsLib/src/com/android/settingslib/accessibility/AccessibilityUtils.java.L55)
         */
        fun isHelperServiceEnabled(context: Context): Boolean {
            /*
                Rather than altering everything, we simply check if we have CaptureAudioOutput permission (meaning app is installed with Magisk module) and pretend that accessibility service is running
                This saves us a lot of time we may spend changing whole structure of APH.
                Perhaps we can re-visit this and change the structure to introduces different call recording modes such as, root, accessibility etc
             */
            return if (App.hasCaptureAudioOutputPermission()) {
                CLog.log(logTag, "isHelperServiceEnabled() -> hasCaptureAudioOutputPermission is true. There is no need for AccessibilityCallRecordingService. Returning True")
                true
            } else {
                val expectedComponentName = ComponentName(context, AccessibilityCallRecordingService::class.java)
                val enabledServicesSetting: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                    ?: return false
                val colonSplitter = TextUtils.SimpleStringSplitter(':')
                colonSplitter.setString(enabledServicesSetting)
                while (colonSplitter.hasNext()) {
                    val componentNameString: String = colonSplitter.next()
                    val enabledService = ComponentName.unflattenFromString(componentNameString)
                    if (enabledService != null && enabledService == expectedComponentName) return true
                }
                false
            }

        }

        fun startHelperServiceIfIsNotRunning(context: Context) {
            CLog.log(logTag, "startHelperServiceIfIsNotRunning -> isRunning: $isRunning")

            if (!isRunning) {
                context.startService(Intent(context, AccessibilityCallRecordingService::class.java))
            }
        }

        fun postEnableHelperServiceNotificationAndToast(context: Context, showToast: Boolean) {
            if (showToast) {
                Toast.makeText(context, R.string.accessibility_service_toast, Toast.LENGTH_SHORT)
                    .show()
            }
            showHelperServiceNotEnabledNotification(context)

        }

        private val accessibilityServicesChangedEventLiveData = LiveEvent<Boolean>()
        fun sendAccessibilityServicesChangedEvent(value: Boolean) {
            //To UI. We can't seem to get update from flow in viewmodel!
            accessibilityServicesChangedEventLiveData.postValue(value)
        }

        fun observeAccessibilityServicesChangesLiveData(): LiveData<Boolean> =
            accessibilityServicesChangedEventLiveData

        private fun showHelperServiceNotEnabledNotification(context: Context) {
            val notificationChannel = Payload.Alerts(
                channelKey = "cb_enable_call_recording_helper",
                lockScreenVisibility = NotificationCompat.VISIBILITY_PUBLIC,
                channelName = context.getString(R.string.accessibility_service_name),
                channelImportance = Notify.IMPORTANCE_HIGH,
                showBadge = false

            )

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingOpenIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            val builder = Notify.with(context)
                .alerting(notificationChannel.channelKey) {
                    lockScreenVisibility = notificationChannel.lockScreenVisibility
                    channelName = notificationChannel.channelName
                    channelImportance = notificationChannel.channelImportance
                }

                .meta {
                    category = NotificationCompat.CATEGORY_ERROR
                    group = "cb_enable_call_recording_notifications"
                    clickIntent = pendingOpenIntent
                }

                .header {
                    icon = R.drawable.ic_warning_24
                    headerText = context.getString(R.string.accessibility_service_name)
                    color = Color.RED
                    showTimestamp = true

                }

                .content {
                    title = context.getString(R.string.accessibility_service_name)
                    text = context.getString(R.string.accessibility_service_notification)

                }.asBuilder()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.foregroundServiceBehavior = Notification.FOREGROUND_SERVICE_IMMEDIATE
            }
            context.extNotificationManager()?.notify(notificationChannel.channelKey.hashCode(), builder.build())


        }


    }
}