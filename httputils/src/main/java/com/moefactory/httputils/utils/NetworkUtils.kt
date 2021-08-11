package com.moefactory.httputils.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.moefactory.httputils.HttpUtils

object NetworkUtils {

    /**
     * Return true if network available
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            HttpUtils.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            // For other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            // For check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }
}