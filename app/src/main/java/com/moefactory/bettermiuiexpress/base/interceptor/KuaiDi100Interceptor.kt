package com.moefactory.bettermiuiexpress.base.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class KuaiDi100Interceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.url.host.endsWith("kuaidi100.com")) {
            return chain.proceed(request)
        }

        val newRequest = request.newBuilder()
            .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 15; 2304FPN6DC Build/AQ3A.240912.001)")
            .build()

        return chain.proceed(newRequest)
    }
}