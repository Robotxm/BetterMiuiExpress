package com.moefactory.httputils.interceptor

import com.moefactory.httputils.utils.NetworkUtils
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class NetCacheInterceptor(private val cacheTime: Int) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (NetworkUtils.isNetworkAvailable()) {
            return chain.proceed(request)
                .newBuilder()
                .header(
                    "Cache-Control",
                    CacheControl.Builder().maxAge(cacheTime, TimeUnit.SECONDS).build().toString()
                )
                .removeHeader("Pragma")
                .build()
        }
        return chain.proceed(request)
    }

}