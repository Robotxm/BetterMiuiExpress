package com.moefactory.httputils.interceptor

import com.moefactory.httputils.utils.NetworkUtils
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

class NoNetCacheInterceptor(private val noNetCacheTime: Int) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        // Force to use cache if network is unavailable
        if (!NetworkUtils.isNetworkAvailable()) {
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
            val response = chain.proceed(request)

            // Force to fetch online if both network and cache are unavailable
            if (response.code == 504) {
                request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build()
                return chain.proceed(request)
            }
            return response.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=$noNetCacheTime")
                .removeHeader("Pragma")
                .build()
        }
        // Do nothing if network is available
        return chain.proceed(request)
    }

}