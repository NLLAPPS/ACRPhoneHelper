package com.nll.helper.recorder

import android.os.Build

enum class Encoder(val id: Int) {
    MediaCodec(1), AndroidMediaRecorder(2);

    override fun toString(): String {
        return when (this) {
            AndroidMediaRecorder -> "AndroidMediaRecorder"
            MediaCodec -> "MediaCodecRecorder"
        }
    }

    companion object {
        private const val logTag = "Encoder"
        private val map = values().associateBy(Encoder::id)
        fun fromIdOrDefault(id: Int): Encoder {
            return when (id) {
                0 -> {
                    if (forceAndroidMediaRecorder()) {
                        CLog.log(logTag, "fromIdOrDefault -> forceAndroidMediaRecorder() is true. Returning AndroidMediaRecorder")

                        AndroidMediaRecorder
                    } else {
                        getDefaultEncoder()
                    }
                }
                else -> {
                    val encoder = map[id] ?: getDefaultEncoder()
                    CLog.log(logTag, "fromIdOrDefault -> User changed -> Returning $encoder")

                    encoder
                }
            }
        }


        /**
         * LGE Has issues with MediaCodec if Earpieces used
         * OnePlus cannot seem to be able to record with mediacodec on 11+
         */
        private fun forceAndroidMediaRecorder() = isLGE() || (isOnePlus() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)

        private fun isLGE() = Build.MANUFACTURER.equals("LGE", ignoreCase = true)

        private fun isOnePlus() = Build.MANUFACTURER.equals("ONEPLUS", ignoreCase = true)

        /**
         * Return AndroidMediaRecorder since we cannot use native fix.
         * This also helps issues with our MediaCodec implementation.
         * We seem to get a lot ANRs with MediaCodec
         */
        private fun getDefaultEncoder() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CLog.log(logTag, "getDefaultEncoder -> Android 10 and above. Returning AndroidMediaRecorder")
            AndroidMediaRecorder
        } else {
            CLog.log(logTag, "getDefaultEncoder -> Below Android10. Returning MediaCodec")
            MediaCodec
        }

    }
}