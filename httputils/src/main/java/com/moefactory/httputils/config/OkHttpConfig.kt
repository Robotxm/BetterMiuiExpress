package com.moefactory.httputils.config

import android.content.Context
import com.moefactory.httputils.`interface`.BuildHeadersListener
import com.moefactory.httputils.cookie.CookieJarImpl
import com.moefactory.httputils.cookie.CookieStore
import com.moefactory.httputils.cookie.PersistentCookieStore
import com.moefactory.httputils.interceptor.HeaderInterceptor
import com.moefactory.httputils.interceptor.NetCacheInterceptor
import com.moefactory.httputils.interceptor.NoNetCacheInterceptor
import com.moefactory.httputils.utils.SSLUtils
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

class OkHttpConfig {
    class Builder(var context: Context) {

        /**
         * Should cache be enabled. Default value is false
         */
        var isCacheEnabled = false

        /**
         * Expire time of cache in second when network is available. Default value is 60
         */
        var cacheExpireTime = 60

        /**
         * Expire time of cache in second when network is unavailable. Default value is 3600
         */
        var noNetworkCacheExpireTime = 3600

        /**
         * Path for cache
         */
        var cachePath: String? = null

        /**
         * Size limitation for cache, 0 means unlimited. Default value is 0
         */
        var cacheMaxSize: Long = 0

        /**
         * Specify cookie store for OkHttp. A [PersistentCookieStore] is used by default
         */
        var cookieStore: CookieStore? = null

        /**
         * Timeout for reading data in second. Default value is 10
         */
        var readTimeout: Long = 0

        /**
         * Timeout for writing data in second. Default value is 10
         */
        var writeTimeout: Long = 0

        /**
         * Timeout for connection in second. Default value is 10
         */
        var connectTimeout: Long = 0

        /**
         * Specify custom interceptors for OkHttp
         */
        var interceptors: Array<Interceptor>? = null

        /**
         * Global headers
         */
        var buildHeadersListener: BuildHeadersListener? = null

        /**
         * Build a new [OkHttpClient] instance using specified config
         */
        fun build(): OkHttpClient {
            INSTANCE
            setCookieConfig()
            setCacheConfig()
            setHeadersConfig()
            setSSLConfig()
            addInterceptors()
            setTimeout()
            okHttpClient = okHttpClientBuilder.build()
            return okHttpClient!!
        }

        /**
         * Add interceptors
         */
        private fun addInterceptors() {
            interceptors?.let {
                for (interceptor in it) {
                    okHttpClientBuilder.addInterceptor(interceptor)
                }
            }
        }

        /**
         * Add headers by adding a new interceptor to OkHttpClient
         */
        private fun setHeadersConfig() {
            buildHeadersListener?.let {
                okHttpClientBuilder.addInterceptor(object :
                    HeaderInterceptor() {
                    override fun buildHeaders() = it.buildHeaders()
                })
            }
        }

        /**
         * Add cookie store
         */
        private fun setCookieConfig() {
            cookieStore?.let {
                okHttpClientBuilder.cookieJar(CookieJarImpl(it))
            }
        }

        /**
         * Config cache
         */
        private fun setCacheConfig() {
            val externalCacheDir = context.externalCacheDir ?: return
            defaultCachePath = "${externalCacheDir.path}/HttpCacheData"
            if (isCacheEnabled) {
                val cache = if (!cachePath.isNullOrEmpty() && cacheMaxSize > 0) {
                    Cache(File(cachePath!!), cacheMaxSize)
                } else {
                    Cache(File(defaultCachePath!!), defaultCacheSize)
                }
                okHttpClientBuilder
                    .cache(cache)
                    .addInterceptor(NoNetCacheInterceptor(noNetworkCacheExpireTime))
                    .addNetworkInterceptor(NetCacheInterceptor(cacheExpireTime))
            }
        }

        /**
         * Set timeout
         */
        private fun setTimeout() {
            okHttpClientBuilder.readTimeout(
                if (readTimeout == 0L) defaultTimeout else readTimeout, TimeUnit.SECONDS
            )
                .writeTimeout(
                    if (writeTimeout == 0L) defaultTimeout else writeTimeout,
                    TimeUnit.SECONDS
                )
                .connectTimeout(
                    if (connectTimeout == 0L) defaultTimeout else connectTimeout,
                    TimeUnit.SECONDS
                )
                .retryOnConnectionFailure(true)
        }

        /**
         * Config SSL. All certificates will be trusted
         */
        private fun setSSLConfig() {
            okHttpClientBuilder.sslSocketFactory(
                SSLUtils.sslSocketFactory,
                SSLUtils.trustAllCerts[0] as X509TrustManager
            )
        }

    }

    companion object {
        val INSTANCE: OkHttpConfig by lazy { OkHttpConfig() }
        private var defaultCachePath: String? = null
        private const val defaultCacheSize: Long = 1024 * 1024 * 100
        private const val defaultTimeout: Long = 10
        private val okHttpClientBuilder by lazy { OkHttpClient.Builder() }
        var okHttpClient: OkHttpClient? = null
    }
}