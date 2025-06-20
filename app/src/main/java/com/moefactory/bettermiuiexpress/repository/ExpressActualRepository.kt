package com.moefactory.bettermiuiexpress.repository

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.moefactory.bettermiuiexpress.BuildConfig
import com.moefactory.bettermiuiexpress.api.KuaiDi100Api
import com.moefactory.bettermiuiexpress.base.cookiejar.MemoryCookieJar
import com.moefactory.bettermiuiexpress.base.interceptor.KuaiDi100Interceptor
import com.moefactory.bettermiuiexpress.model.KuaiDi100BaseResponse
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.KuaiDi100ExpressDetailsRequestParam
import com.moefactory.bettermiuiexpress.model.KuaiDi100ExpressRegisterDeviceTrackIdRequestParam
import com.moefactory.bettermiuiexpress.model.Kuaidi100ExpressDetailsResult
import com.moefactory.bettermiuiexpress.utils.SSLUtils
import com.moefactory.bettermiuiexpress.utils.upperMD5
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object ExpressActualRepository {

    val jsonParser by lazy {
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .addInterceptor(KuaiDi100Interceptor())
            .apply {
                if (BuildConfig.DEBUG) {
                    sslSocketFactory(
                        SSLUtils.sslSocketFactory,
                        SSLUtils.unsafeTrustManagers[0] as X509TrustManager
                    )
                }
            }
            .cookieJar(MemoryCookieJar())
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://poll.kuaidi100.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()
    }

    private val kuaiDi100Api by lazy { retrofit.create(KuaiDi100Api::class.java) }

    suspend fun registerDeviceTrackIdActual(trackId: String) = suspendCoroutine {
        val params = KuaiDi100ExpressRegisterDeviceTrackIdRequestParam(trackId = trackId)
        val paramsString = jsonParser.encodeToString(params)

        kuaiDi100Api.registerDeviceTrackId(
            method = "pushbind2",
            paramString = paramsString,
            userId = 0,
            token = "",
            hash = "X5GqUj8Dc0mWl3eK6V2h78Z9sP1r4n$paramsString".upperMD5()
        ).enqueue(object : Callback<KuaiDi100BaseResponse<String>>{
            override fun onResponse(call: Call<KuaiDi100BaseResponse<String>>, response: Response<KuaiDi100BaseResponse<String>>) {
                val body = response.body()

                if (body?.status == "200") {
                    it.resume(true)
                } else {
                    it.resumeWithException(Exception("Failed to register"))
                }
            }

            override fun onFailure(call: Call<KuaiDi100BaseResponse<String>>, t: Throwable) {
                it.resumeWithException(t)
            }
        })
    }

    suspend fun queryCompanyActual(mailNumber: String) = suspendCoroutine<List<KuaiDi100Company>> {
        kuaiDi100Api.queryExpressCompany(mailNumber).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val body = response.body()
                when {
                    // Normal
                    body?.startsWith("[") == true -> {
                        it.resume(jsonParser.decodeFromString(body))
                    }
                    // Error
                    body?.startsWith("{") == true -> {
                        val message = jsonParser.parseToJsonElement(body)
                            .jsonObject["message"]?.jsonPrimitive?.content
                        it.resumeWithException(Exception(message))
                    }
                    // Exception
                    else -> it.resumeWithException(Exception("Unexpected response: $response"))
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                it.resumeWithException(t)
            }
        })
    }

    suspend fun queryExpressDetailsFromKuaiDi100Actual(companyCode: String, mailNumber: String, phoneNumber: String?, trackId: String) = suspendCoroutine {
        val params = KuaiDi100ExpressDetailsRequestParam(phone = phoneNumber, mailNumber = mailNumber, companyCode = companyCode, trackId = trackId)
        val paramsString = jsonParser.encodeToString(params)

        kuaiDi100Api.queryExpressDetails(
            method = "query",
            paramString = paramsString,
            userId = 0,
            token = "",
            hash = "X5GqUj8Dc0mWl3eK6V2h78Z9sP1r4n$paramsString".upperMD5()
        ).enqueue(object : Callback<KuaiDi100BaseResponse<Kuaidi100ExpressDetailsResult>> {
            override fun onFailure(call: Call<KuaiDi100BaseResponse<Kuaidi100ExpressDetailsResult>>, t: Throwable) {
                it.resumeWithException(t)
            }

            override fun onResponse(call: Call<KuaiDi100BaseResponse<Kuaidi100ExpressDetailsResult>>, response: Response<KuaiDi100BaseResponse<Kuaidi100ExpressDetailsResult>>) {
                it.resume(response.body())
            }
        })
    }
}