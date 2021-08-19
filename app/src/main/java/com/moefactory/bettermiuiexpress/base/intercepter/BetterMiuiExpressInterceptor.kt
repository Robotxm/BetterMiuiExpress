package com.moefactory.bettermiuiexpress.base.intercepter

import com.moefactory.bettermiuiexpress.base.app.secretKey
import com.moefactory.bettermiuiexpress.utils.SignUtils
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response

class KuaiDi100Interceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.url.toString().startsWith("https://poll.kuaidi100.com/poll/query.do")) {
            val builder = request.newBuilder()
            if (request.body is FormBody) {
                val formBodyBuilder = FormBody.Builder()
                val originalForm = request.body as FormBody
                var param = ""
                var customer = ""
                for (i in 0 until originalForm.size) {
                    val originalNameEncoded = originalForm.encodedName(i)
                    formBodyBuilder.addEncoded(originalNameEncoded, originalForm.encodedValue(i))
                    if (originalNameEncoded == "param") {
                        param = originalForm.value(i)
                    }
                    if (originalNameEncoded == "customer"){
                        customer = originalForm.value(i)
                    }
                }
                val sign = SignUtils.sign(param, secretKey, customer)
                formBodyBuilder.add("sign", sign)
                if (request.method == "POST") {
                    builder.method(request.method, formBodyBuilder.build())
                }
            }
            request = builder.build()
        }
        return chain.proceed(request)
    }
}