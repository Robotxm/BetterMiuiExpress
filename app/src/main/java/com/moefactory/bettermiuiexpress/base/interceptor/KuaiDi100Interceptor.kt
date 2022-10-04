package com.moefactory.bettermiuiexpress.base.interceptor

import com.moefactory.bettermiuiexpress.utils.SignUtils
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response

class KuaiDi100Interceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.url.toString().startsWith("https://poll.kuaidi100.com/poll/query.do")) {
            return chain.proceed(request)
        }

        val builder = request.newBuilder()
        if (request.body is FormBody) {
            val formBodyBuilder = FormBody.Builder()
            val originalForm = request.body as FormBody
            var param = ""
            var customer = ""
            var secretKey = ""
            for (i in 0 until originalForm.size) {
                val originalNameEncoded = originalForm.encodedName(i)
                if (originalNameEncoded == "param") {
                    param = originalForm.value(i)
                }
                if (originalNameEncoded == "customer") {
                    customer = originalForm.value(i)
                }
                if (originalNameEncoded == "key") {
                    secretKey = originalForm.value(i)
                    continue
                }

                formBodyBuilder.addEncoded(originalNameEncoded, originalForm.encodedValue(i))
            }
            val sign = SignUtils.signForKuaiDi100(param, secretKey, customer)
            formBodyBuilder.add("sign", sign)
            if (request.method == "POST") {
                builder.method(request.method, formBodyBuilder.build())
            }
        }

        val newRequest = builder
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; M2102K1C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Mobile Safari/537.36 EdgA/105.0.1343.48")
            .build()

        return chain.proceed(newRequest)
    }
}