package com.moefactory.httputils.cookie

import okhttp3.Cookie
import okhttp3.HttpUrl

interface CookieStore {

    /**
     * Add a new cookie
     */
    fun add(cookie: Cookie)

    /**
     * Add new cookies
     */
    fun add(cookies: List<Cookie>)

    /**
     * Get all cookies that match specified [url]
     *
     * @return A [List] of [Cookie]
     */
    fun get(url: HttpUrl): List<Cookie>

    /**
     * Get all cookies whose domains match host of specified [url]
     *
     * @return A [List] of [Cookie]
     */
    fun getByDomain(url: HttpUrl): List<Cookie>

    /**
     * Remove all cookies whose domains match host of specified [url]
     *
     * @return true for success
     */
    fun removeByDomain(url: HttpUrl): Boolean

    /**
     * Remove all cookies
     *
     * @return true for success
     */
    fun removeAll(): Boolean
}