package com.moefactory.httputils.factory

import com.moefactory.httputils.manager.UrlManager
import com.moefactory.httputils.retrofit.RetrofitBuilder
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.*

class ApiFactory private constructor() {
    var callAdapterFactory: Array<out CallAdapter.Factory>? = null
    var converterFactory: Array<out Converter.Factory>? = null
    var okHttpClient: OkHttpClient? = null

    /**
     * Clear all cached apis
     */
    fun clearAllApi() {
        apiServiceCache.clear()
    }

    /**
     * Set global base url
     */
    fun setBaseUrl(baseUrl: String): ApiFactory {
        UrlManager.INSTANCE.setUrl(baseUrl)
        return this
    }

    /**
     * Create api using global parameters (base url, etc.)
     */
    fun <A> createApi(apiClass: Class<A>): A {
        val urlKey = UrlManager.DEFAULT_URL_KEY
        val urlValue = UrlManager.INSTANCE.url!!
        return createApi(urlKey, urlValue, apiClass)
    }

    /**
     * Create api using specified parameters
     */
    fun <A> createApi(
        baseUrlKey: String,
        baseUrlValue: String,
        apiClass: Class<A>
    ): A {
        val key = getApiKey(baseUrlKey, apiClass)
        var api = apiServiceCache[key] as A
        if (api == null) {
            api = RetrofitBuilder().also {
                it.baseUrl = baseUrlValue
                it.callAdapterFactory = callAdapterFactory
                it.converterFactory = converterFactory
                it.okHttpClient = okHttpClient
            }.build().create(apiClass)
            apiServiceCache[key] = api
        }
        return api
    }

    companion object {
        val INSTANCE: ApiFactory by lazy { ApiFactory() }

        /**
         * Use for api cache.
         * For same base urls and same api interface, only one [Retrofit] instance will be created.
         */
        private lateinit var apiServiceCache: HashMap<String, Any?>

        private fun <A> getApiKey(baseUrlKey: String, apiClass: Class<A>) =
            String.format("%s_%s", baseUrlKey, apiClass.name)
    }

    init {
        apiServiceCache = HashMap()
    }
}