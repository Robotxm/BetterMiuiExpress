package com.moefactory.bettermiuiexpress.repository

import androidx.lifecycle.liveData
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.moefactory.bettermiuiexpress.api.KuaiDi100Api
import com.moefactory.bettermiuiexpress.model.BaseKuaiDi100Response
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import com.moefactory.bettermiuiexpress.utils.SignUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object ExpressRepository {

    val jsonParser by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://poll.kuaidi100.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()
    }

    private val kuaiDi100Api by lazy {
        retrofit.create(KuaiDi100Api::class.java)
    }

    fun queryCompany(mailNumber: String, secretKey: String) =
        liveData {
            try {
                val result = queryCompanyActual(mailNumber, secretKey)
                emit(Result.success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.failure(e))
            }
        }

    suspend fun queryCompanyActual(mailNumber: String, secretKey: String): List<KuaiDi100Company> {
        val response = kuaiDi100Api.queryExpressCompany(secretKey, mailNumber)
        when {
            // Normal
            response.startsWith("[") -> {
                return jsonParser.decodeFromString(response)
            }
            // Error
            response.startsWith("{") -> {
                val message = jsonParser.parseToJsonElement(response)
                    .jsonObject["message"]?.jsonPrimitive?.content
                throw Exception(message)
            }
            // Exception
            else -> throw Exception("Unexpected response: $response")
        }
    }

    fun queryExpress(
        companyCode: String,
        mailNumber: String,
        secretKey: String,
        customer: String
    ) =
        liveData(Dispatchers.IO) {
            try {
                val result = queryExpressActual(companyCode, mailNumber, secretKey, customer)
                emit(Result.success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.failure(e))
            }
        }

    suspend fun queryExpressActual(
        mailNumber: String,
        companyCode: String,
        secretKey: String,
        customer: String
    ): BaseKuaiDi100Response {
        val data = jsonParser.encodeToString(KuaiDi100RequestParam(companyCode, mailNumber))
        val sign = SignUtils.sign(data, secretKey, customer)
        return kuaiDi100Api.queryPackage(customer, data, sign)
    }
}