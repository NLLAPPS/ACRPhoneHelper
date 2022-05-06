package com.nll.helper.server

import android.os.Bundle

data class ClientVersionData(val clientVersion: Int){
    fun toBundle() = Bundle().apply {

        putInt(ARG_CLIENT_VERSION, clientVersion)


    }
    companion object {
        private const val ARG_CLIENT_VERSION = "ARG_CLIENT_VERSION"
        fun fromBundle(bundle: Bundle?): ClientVersionData? {
            return bundle?.let {
                val clientVersion = it.getInt(ARG_CLIENT_VERSION, -1)
                ClientVersionData(clientVersion)
            }
        }
    }
}