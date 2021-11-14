package com.moefactory.bettermiuiexpress.repository

import androidx.lifecycle.liveData
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.moefactory.bettermiuiexpress.api.KuaiDi100Api
import com.moefactory.bettermiuiexpress.base.app.customer
import com.moefactory.bettermiuiexpress.base.app.secretKey
import com.moefactory.bettermiuiexpress.base.intercepter.KuaiDi100Interceptor
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object ExpressRepository {

    private val jsonParser by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .addInterceptor(KuaiDi100Interceptor())
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()
    }

    private val kuaiDi100 by lazy {
        retrofit.create(KuaiDi100Api::class.java)
    }

    fun queryCompany(mailNumber: String) =
        liveData {
            try {
                val response = kuaiDi100.queryExpressCompany(secretKey, mailNumber)
                when {
                    // Normal
                    response.startsWith("[") -> {
                        val result = jsonParser.decodeFromString<List<KuaiDi100Company>>(response)
                        emit(Result.success(result))
                    }
                    // Error
                    response.startsWith("{") -> {
                        val message =
                            jsonParser.parseToJsonElement(response).jsonObject["message"]?.jsonPrimitive?.content
                        throw Exception(message)
                    }
                    // Exception
                    else -> throw Exception("Unexpected response: $response")
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }

    fun queryExpress(companyCode: String, mailNumber: String) =
        liveData(Dispatchers.IO) {
            val data = jsonParser.encodeToString(KuaiDi100RequestParam(companyCode, mailNumber))
            try {
                emit(Result.success(kuaiDi100.queryPackage(customer, data)))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
}