package com.moefactory.httputils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.moefactory.httputils.config.OkHttpConfig
import com.moefactory.httputils.cookie.CookieJarImpl
import com.moefactory.httputils.cookie.CookieStore
import com.moefactory.httputils.factory.ApiFactory
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl

class HttpUtils {

    /**
     * Initialize global [HttpUtils] instance.
     *
     * This method must be called in [Application.onCreate] method,
     * or cache will be unavailable
     */
    fun init(app: Application): HttpUtils {
        context = app
        return this
    }

    fun config(): ApiFactory {
        checkInitialize()
        return ApiFactory.INSTANCE
    }

    companion object {
        val INSTANCE: HttpUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { HttpUtils() }

        @SuppressLint("StaticFieldLeak")
        private var context: Application? = null

        fun getContext(): Context {
            checkInitialize()
            return context!!
        }

        /**
         * Check if instance initialized
         */
        private fun checkInitialize() {
            if (context == null) {
                throw ExceptionInInitializerError("init method must be called in Application")
            }
        }

        /**
         * Create a api instance for request with global base url
         */
        fun <K> createApi(cls: Class<K>): K {
            return ApiFactory.INSTANCE.createApi(cls)
        }

        /**
         * Create a api instance for request with specified base url
         */
        fun <K> createApi(
            baseUrlKey: String,
            baseUrlValue: String,
            cls: Class<K>
        ): K {
            return ApiFactory.INSTANCE.createApi(baseUrlKey, baseUrlValue, cls)
        }

        /**
         * Global [CookieJarImpl] instance
         */
        private val cookieJar: CookieJarImpl
            get() = OkHttpConfig.okHttpClient?.cookieJar as CookieJarImpl

        /**
         * Global [CookieStore] instance
         */
        private val cookieStore: CookieStore
            get() = cookieJar.cookieStore

        /**
         * Add a new cookie
         */
        fun addCookie(cookie: Cookie) = cookieStore.add(cookie)

        /**
         * Get all cookies that match specified url
         */
        fun getCookiesByUrl(url: String) = cookieStore.getByDomain(url.toHttpUrl())

        /**
         * Remove all cookies in cookie jar
         */
        fun removeAllCookies() = cookieStore.removeAll()

        /**
         * Remove all cookies of specified url
         */
        fun removeCookiesByUrl(url: String) = cookieStore.removeByDomain(url.toHttpUrl())
    }
}
