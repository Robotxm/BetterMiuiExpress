package com.moefactory.httputils.interceptor

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor

class HttpLogger : HttpLoggingInterceptor.Logger {

    private val mMessage = StringBuffer()

    override fun log(message: String) {
        if (message.startsWith("--> POST")) {
            mMessage.apply { setLength(0) }
                .append(" ")
                .append("\r\n")
        }
        if (message.startsWith("--> GET")) {
            mMessage.apply { setLength(0) }
                .append(" ")
                .append("\r\n")
        }
        mMessage.append(
            """
                $message
                
                """.trimIndent()
        )
        if (message.startsWith("<-- END HTTP")) {
            Log.e("HttpUtils", mMessage.toString())
        }
    }
}