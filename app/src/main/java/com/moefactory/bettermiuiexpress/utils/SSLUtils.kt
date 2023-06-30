package com.moefactory.bettermiuiexpress.utils

import android.annotation.SuppressLint
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object SSLUtils {

    /**
     * An array contains only one [X509TrustManager], which trusts all certificates.
     *
     * Only be used when debug.
     */
    val unsafeTrustManagers = arrayOf<TrustManager>(
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        }
    )

    /**
     * An SSL socket factory used to trust all certificates.
     *
     * Only be used when debug.
     */
    val sslSocketFactory = SSLContext.getInstance("TLSv1.2")
        .apply { init(null, unsafeTrustManagers, SecureRandom()) }
        .socketFactory!!
}