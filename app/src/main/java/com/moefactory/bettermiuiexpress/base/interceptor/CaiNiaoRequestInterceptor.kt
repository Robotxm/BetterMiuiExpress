package com.moefactory.bettermiuiexpress.base.interceptor

import com.moefactory.bettermiuiexpress.utils.SignUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.util.*

class CaiNiaoRequestInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.host != "acs.m.taobao.com") {
            return chain.proceed(request)
        }

        val urlQueries = request.url.query
            ?.split("&")
            ?.map { it.split("=") }
            ?.associate { it[0] to it[1] }
            ?.toMutableMap() ?: mutableMapOf()

        val requestBuilder = request.newBuilder()
            .header("Origin", "https://page.cainiao.com")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; M2102K1C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Mobile Safari/537.36 EdgA/105.0.1343.48")

        val appKey = urlQueries["appKey"] ?: return chain.proceed(requestBuilder.build())
        val token = urlQueries["token"] ?: return chain.proceed(requestBuilder.build())
        val data = urlQueries["data"] ?: return chain.proceed(requestBuilder.build())
        val timestamp = Calendar.getInstance().time.time

        val sign = SignUtils.signForCaiNiao(token, timestamp.toString(), appKey, data)

        val requestUrlBuilder = request.url.newBuilder()
        urlQueries.remove("token")
        urlQueries["t"] = timestamp.toString()
        urlQueries["sign"] = sign

        val newUrlQueries = urlQueries.map { "${it.key}=${it.value}" }.joinToString("&")
        val newRequestUrl = requestUrlBuilder.query(newUrlQueries).build()
        val newRequest = requestBuilder
            .url(newRequestUrl)
            .build()

        return chain.proceed(newRequest)
    }
}