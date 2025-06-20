package com.moefactory.bettermiuiexpress.repository

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.moefactory.bettermiuiexpress.BuildConfig
import com.moefactory.bettermiuiexpress.api.KuaiDi100Api
import com.moefactory.bettermiuiexpress.base.converter.KuaiDi100RequestDataConverterFactory
import com.moefactory.bettermiuiexpress.base.cookiejar.MemoryCookieJar
import com.moefactory.bettermiuiexpress.base.interceptor.KuaiDi100Interceptor
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.NewKuaiDi100BaseResponse
import com.moefactory.bettermiuiexpress.model.NewKuaiDi100RequestParam
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
            .addConverterFactory(KuaiDi100RequestDataConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private val kuaiDi100Api by lazy { retrofit.create(KuaiDi100Api::class.java) }

    /**** KuaiDi100 Begin ****/

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

    /****  KuaiDi100 End  ****/

    /** New KuaiDi100 Begin **/
    suspend fun queryExpressDetailsFromKuaiDi100Actual(companyCode: String, mailNumber: String, phoneNumber: String?) = suspendCoroutine {
        val params = NewKuaiDi100RequestParam(phone = phoneNumber, mailNumber = mailNumber, companyCode = companyCode)
        val paramsString = jsonParser.encodeToString(params)
        val hash = "X5GqUj8Dc0mWl3eK6V2h78Z9sP1r4n$paramsString".upperMD5()

        kuaiDi100Api.queryPackageNew(paramString = paramsString, hash = hash).enqueue(object : Callback<NewKuaiDi100BaseResponse> {
            override fun onFailure(call: Call<NewKuaiDi100BaseResponse>, t: Throwable) {
                it.resumeWithException(t)
            }

            override fun onResponse(call: Call<NewKuaiDi100BaseResponse>, response: Response<NewKuaiDi100BaseResponse>) {
                it.resume(response.body())
            }
        })
    }
    /*** New KuaiDi100 End ***/
}