package com.nll.helper.util

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.getSystemService
import java.util.*
import kotlin.math.ln
import kotlin.math.pow
fun Context.extPowerManager(): PowerManager? = getSystemService()
/**
 * setCompoundDrawablesWithIntrinsicBounds depending on the Locale
 * We need to set even if compoundDrawable=null as we may be calling this method from ViewHolders and might mean to update/reset
 */
fun TextView.extSetCompoundDrawablesWithIntrinsicBoundsToRightOrLeft(@DrawableRes compoundDrawableRes: Int, paddingDp: Float = 0f) {
    //Do not use this.layoutDirection because it gets direction from system and user may have different system locale to the app
    val isRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == android.view.View.LAYOUT_DIRECTION_RTL
    if (isRtl) {
        setCompoundDrawablesWithIntrinsicBounds(compoundDrawableRes, 0, 0, 0)
    } else {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, compoundDrawableRes, 0)
    }
    compoundDrawablePadding = if (paddingDp != 0f) {
        Util.dpToPx(context, paddingDp).toInt()
    } else {
        0
    }

}
fun Context.extNotificationManager(): NotificationManager? = getSystemService()
@ColorInt
fun Context.extGetThemeAttrColor(@AttrRes colorAttr: Int): Int {
    val array = obtainStyledAttributes(null, intArrayOf(colorAttr))
    return try {
        array.getColor(0, 0)
    } finally {
        array.recycle()
    }
}
fun Long.extHumanReadableByteCount(si: Boolean): String {
    val unit = if (si) 1000 else 1024
    if (this < unit)
        return "$this B"
    val exp = (ln(this.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
    return String.format(Locale.getDefault(), "%.1f %sB", this / unit.toDouble().pow(exp.toDouble()), pre)
}

fun Int.extHumanReadableByteCount(si: Boolean): String {
    val unit = if (si) 1000 else 1024
    if (this < unit)
        return "$this B"
    val exp = (ln(this.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
    return String.format(Locale.getDefault(), "%.1f %sB", this / unit.toDouble().pow(exp.toDouble()), pre)
}

fun Activity.extOpenAppDetailsSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }.let(::startActivity)
}
fun TextView.extSetHTML(cancelPendingInputEvents: Boolean, html: String, urlToOpen: (String) -> Unit) {
    val sequence: CharSequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    val strBuilder = SpannableStringBuilder(sequence)
    strBuilder.getSpans(0, sequence.length, URLSpan::class.java).forEach { span ->

        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                //Prevent passing click events to the widget so (for example) clicking on the link would not mark checkbox as checked
                if(cancelPendingInputEvents) {
                    widget.cancelPendingInputEvents()
                }
                urlToOpen(span.url)
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)

    }
    movementMethod = LinkMovementMethod.getInstance()
    text = strBuilder
}
