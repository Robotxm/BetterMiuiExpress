package com.moefactory.httputils.cookie

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CookieJarImpl(val cookieStore: CookieStore) : CookieJar {

    override fun loadForRequest(url: HttpUrl) = cookieStore.get(url)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.add(cookies)
    }

}