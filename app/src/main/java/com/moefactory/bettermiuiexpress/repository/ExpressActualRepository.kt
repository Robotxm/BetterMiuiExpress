package com.moefactory.bettermiuiexpress.repository

import com.highcapable.yukihookapi.hook.log.loggerD
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.moefactory.bettermiuiexpress.BuildConfig
import com.moefactory.bettermiuiexpress.api.CaiNiaoApi
import com.moefactory.bettermiuiexpress.api.KuaiDi100Api
import com.moefactory.bettermiuiexpress.base.converter.CaiNiaoRequestDataConverterFactory
import com.moefactory.bettermiuiexpress.base.converter.KuaiDi100RequestDataConverterFactory
import com.moefactory.bettermiuiexpress.base.cookiejar.MemoryCookieJar
import com.moefactory.bettermiuiexpress.base.interceptor.CaiNiaoRequestInterceptor
import com.moefactory.bettermiuiexpress.base.interceptor.KuaiDi100Interceptor
import com.moefactory.bettermiuiexpress.model.*
import com.moefactory.bettermiuiexpress.utils.SSLUtils
import com.moefactory.bettermiuiexpress.utils.upperMD5
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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
            .addInterceptor(CaiNiaoRequestInterceptor())
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

    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://poll.kuaidi100.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
            .addConverterFactory(CaiNiaoRequestDataConverterFactory.create())
            .addConverterFactory(KuaiDi100RequestDataConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private val kuaiDi100Api by lazy { retrofit.create(KuaiDi100Api::class.java) }

    private val caiNiaoApi by lazy { retrofit.create(CaiNiaoApi::class.java) }

    /**** KuaiDi100 Begin ****/

    suspend fun queryCompanyActual(mailNumber: String, secretKey: String?) = suspendCoroutine<List<KuaiDi100Company>> {
        val call = if (secretKey.isNullOrEmpty()) kuaiDi100Api.queryExpressCompanyNew(mailNumber) else kuaiDi100Api.queryExpressCompany(secretKey, mailNumber)
        call.enqueue(object : Callback<String> {
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

    suspend fun queryExpressDetailsFromKuaiDi100Actual(
        companyCode: String, mailNumber: String, phoneNumber: String?, secretKey: String, customer: String
    ) = suspendCoroutine {
        // Shunfeng and Fengwang need phone number
        val data = if (companyCode == "shunfeng" || companyCode == "fengwang") {
            KuaiDi100RequestParam(companyCode, mailNumber, phoneNumber)
        } else {
            KuaiDi100RequestParam(companyCode, mailNumber)
        }

        kuaiDi100Api.queryPackage(data, customer, secretKey).enqueue(object : Callback<BaseKuaiDi100Response> {
            override fun onFailure(call: Call<BaseKuaiDi100Response>, t: Throwable) {
                it.resumeWithException(t)
            }

            override fun onResponse(call: Call<BaseKuaiDi100Response>, response: Response<BaseKuaiDi100Response>) {
                it.resume(response.body())
            }
        })
    }

    /****  KuaiDi100 End  ****/

    /** New KuaiDi100 Begin **/
    suspend fun queryExpressDetailsFromNewKuaiDi100Actual(companyCode: String, mailNumber: String, phoneNumber: String?) = suspendCoroutine {
        val params = NewKuaiDi100RequestParam(phone = phoneNumber, mailNumber = mailNumber, companyCode = companyCode)
        val paramsString = jsonParser.encodeToString(params)
        val hash = "L0Z1yKqPXseWi4ERAUFnxQmgHwhafITG$paramsString".upperMD5()

        loggerD(msg = paramsString)

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

    /***** CaiNiao Begin *****/

    suspend fun queryExpressDetailsFromCaiNiaoActual(mailNumber: String) = suspendCoroutine {
        caiNiaoApi.getToken().enqueue(object : Callback<CaiNiaoBaseResponse<PlaceholderObject>> {
            override fun onResponse(
                call: Call<CaiNiaoBaseResponse<PlaceholderObject>>,
                response: Response<CaiNiaoBaseResponse<PlaceholderObject>>
            ) {
                val tokenResponse = response.body()
                if (tokenResponse == null) {
                    it.resumeWithException(IllegalArgumentException("Missing token response"))
                    return
                }
                val tokenField = tokenResponse.token
                if (tokenField == null) {
                    it.resumeWithException(IllegalArgumentException("Missing token in response"))
                    return
                }
                val token = tokenField.split("_")[0]

                caiNiaoApi.queryExpressDetails(CaiNiaoRequestData(mailNumber), token).enqueue(object : Callback<CaiNiaoBaseResponse<CaiNiaoExpressDetailsResponse>> {
                    override fun onResponse(
                        call: Call<CaiNiaoBaseResponse<CaiNiaoExpressDetailsResponse>>,
                        response: Response<CaiNiaoBaseResponse<CaiNiaoExpressDetailsResponse>>
                    ) {
                        val detailsResponse = response.body()
                        val details = detailsResponse?.data?.results?.get(0)
                        it.resume(details)
                    }

                    override fun onFailure(call: Call<CaiNiaoBaseResponse<CaiNiaoExpressDetailsResponse>>, t: Throwable) {
                        it.resumeWithException(t)
                    }
                })
            }

            override fun onFailure(call: Call<CaiNiaoBaseResponse<PlaceholderObject>>, t: Throwable) {
                it.resumeWithException(t)
            }
        })
    }

    /****** CaiNiao End ******/
}