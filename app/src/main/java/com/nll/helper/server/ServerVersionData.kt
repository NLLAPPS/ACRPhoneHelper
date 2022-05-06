package com.nll.helper.server

import android.os.Bundle

data class ServerVersionData(val serverVersion: Int) {

    fun toBundle() = Bundle().apply {

        putInt(ARG_SERVER_VERSION, serverVersion)



    }

    companion object {
        private const val ARG_SERVER_VERSION = "ARG_SERVER_VERSION"
        fun fromBundle(bundle: Bundle?): ServerVersionData? {
            return bundle?.let {
                val serverVersion = it.getInt(ARG_SERVER_VERSION, -1)
                ServerVersionData(serverVersion)
            }
        }
    }
}