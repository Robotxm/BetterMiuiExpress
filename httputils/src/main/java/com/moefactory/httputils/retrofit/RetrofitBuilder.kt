package com.moefactory.httputils.retrofit

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.moefactory.httputils.interceptor.HttpLogger
import com.moefactory.httputils.utils.SSLUtils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

class RetrofitBuilder {

    var baseUrl: String? = null
    var callAdapterFactory: Array<out CallAdapter.Factory>? = null
    var converterFactory: Array<out Converter.Factory>? = null
    var okHttpClient: OkHttpClient? = null

    /**
     * Build a new [Retrofit] instance
     */
    @ExperimentalSerializationApi
    fun build(): Retrofit {
        val builder = Retrofit.Builder()
        builder.baseUrl(baseUrl!!)
        // Add all call adapters specified
        if (callAdapterFactory != null && callAdapterFactory!!.isNotEmpty()) {
            for (factory in callAdapterFactory!!) {
                builder.addCallAdapterFactory(factory)
            }
        }
        if (converterFactory == null || converterFactory!!.isEmpty()) {
            // If no converter is specified, add scalars and kotlinx serialization converter by default
            // for plain and json response support
            builder.addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }.asConverterFactory("application/json".toMediaType()))
        } else {
            // Add all converters specified
            for (factory in converterFactory!!) {
                builder.addConverterFactory(factory)
            }
        }
        return builder.client(okHttpClient ?: createOkHttpClient())
            .build()
    }

    /**
     * Build a new [OkHttpClient] instance
     */
    private fun createOkHttpClient() = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .sslSocketFactory(
            SSLUtils.sslSocketFactory,
            SSLUtils.trustAllCerts[0] as X509TrustManager
        )
        .addInterceptor(
            HttpLoggingInterceptor(HttpLogger())
                .setLevel(HttpLoggingInterceptor.Level.BODY)
        )
        .build()
}