package com.moefactory.bettermiuiexpress.repository

import androidx.lifecycle.liveData
import com.moefactory.bettermiuiexpress.api.ApiCollection
import com.moefactory.bettermiuiexpress.base.app.customer
import com.moefactory.bettermiuiexpress.base.app.secretKey
import com.moefactory.bettermiuiexpress.model.BaseKuaiDi100Response
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ExpressRepository {

    fun queryCompany(mailNumber: String) =
        liveData<Result<List<KuaiDi100Company>>> {
            try {
                val response = ApiCollection.kuaiDi100Api.queryExpressCompany(secretKey, mailNumber)
                when {
                    // Normal
                    response.startsWith("[") -> {
                        val result = Json.decodeFromString<List<KuaiDi100Company>>(response)
                        emit(Result.success(result))
                    }
                    // Error
                    response.startsWith("{") -> {
                        val message =
                            Json.parseToJsonElement(response).jsonObject["message"]?.jsonPrimitive?.content
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
        liveData<Result<BaseKuaiDi100Response>>(Dispatchers.IO) {
            val data = Json.encodeToString(KuaiDi100RequestParam(companyCode, mailNumber))
            try {
                emit(Result.success(ApiCollection.kuaiDi100Api.queryPackage(customer, data)))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
}