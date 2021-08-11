package com.moefactory.httputils.manager

import com.moefactory.httputils.HttpUtils
import com.moefactory.httputils.factory.ApiFactory
import java.util.*

class UrlManager private constructor() {

    private var urlMap: MutableMap<String, String>

    /**
     * Set url map
     */
    fun setUrls(urlMap: MutableMap<String, String>): UrlManager {
        this.urlMap = urlMap
        return this
    }

    /**
     * Add new url
     *
     * @param urlKey   Key of new url
     * @param urlValue New url
     */
    fun add(urlKey: String, urlValue: String): UrlManager {
        urlMap[urlKey] = urlValue
        return this
    }

    /**
     * Remove url associated with specified key
     */
    fun remove(urlKey: String): UrlManager {
        urlMap.remove(urlKey)
        return this
    }

    /**
     * Set new base url for single base url. All data of old base url will be cleared
     */
    fun setUrl(urlValue: String): UrlManager {
        urlMap[DEFAULT_URL_KEY] = urlValue
        return this
    }

    /**
     * Global unique base url
     */
    val url: String?
        get() = getByKey(DEFAULT_URL_KEY)

    /**
     * Get url by key
     */
    fun getByKey(urlKey: String?): String? {
        return urlMap[urlKey]
    }

    /**
     * Clear all urls
     */
    fun clear(): UrlManager {
        urlMap.clear()
        ApiFactory.INSTANCE.clearAllApi()
        HttpUtils.removeAllCookies()
        return this
    }

    companion object {
        val INSTANCE: UrlManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { UrlManager() }
        const val DEFAULT_URL_KEY = "http_utils_default_url_key"
    }

    init {
        urlMap = HashMap()
    }
}