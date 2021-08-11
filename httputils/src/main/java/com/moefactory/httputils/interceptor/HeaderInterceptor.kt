package com.moefactory.httputils.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

abstract class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val headers = buildHeaders()
        return if (headers.isNullOrEmpty()) {
            chain.proceed(request)
        } else {
            chain.proceed(
                request.newBuilder()
                    .headers(buildHeaders(request, headers))
                    .build()
            )
        }
    }

    private fun buildHeaders(request: Request, headerMap: Map<String, String>) = run {
        val builder = request.headers.newBuilder()
        for (key in headerMap.keys) {
            builder.add(key, headerMap.getValue(key))
        }
        builder.build()
    }

    abstract fun buildHeaders(): Map<String, String>?
}