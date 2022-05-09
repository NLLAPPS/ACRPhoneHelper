package com.nll.helper


import android.content.Context


object Util {
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

}