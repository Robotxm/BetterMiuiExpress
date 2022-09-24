package com.moefactory.bettermiuiexpress.repository

import androidx.lifecycle.liveData
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.moefactory.bettermiuiexpress.api.KuaiDi100Api
import com.moefactory.bettermiuiexpress.model.BaseKuaiDi100Response
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import com.moefactory.bettermiuiexpress.model.MiuiExpress
import com.moefactory.bettermiuiexpress.model.ExpressDetails
import com.moefactory.bettermiuiexpress.utils.SignUtils
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.request.UrlBuilder
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
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
        phoneNumber: String?,
        secretKey: String,
        customer: String
    ) =
        liveData(Dispatchers.IO) {
            try {
                val result = queryExpressActual(companyCode, mailNumber, phoneNumber, secretKey, customer)
                emit(Result.success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.failure(e))
            }
        }

    suspend fun queryExpressActual(
        mailNumber: String,
        companyCode: String,
        phoneNumber: String?,
        secretKey: String,
        customer: String
    ): BaseKuaiDi100Response {
        // Shunfeng and Fengwang need phone number
        val data = if (companyCode == "shunfeng" || companyCode == "fengwang") {
            jsonParser.encodeToString(KuaiDi100RequestParam(companyCode, mailNumber, phoneNumber))
        } else {
            jsonParser.encodeToString(KuaiDi100RequestParam(companyCode, mailNumber))
        }

        val sign = SignUtils.sign(data, secretKey, customer)
        return kuaiDi100Api.queryPackage(customer, data, sign)
    }

    fun queryExpressDetailsFromCaiNiao(miuiExpress: MiuiExpress) = liveData(Dispatchers.IO) {
        try {
            emit(Result.success(queryExpressDetailsFromCaiNiaoActual(miuiExpress)))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    suspend fun queryExpressDetailsFromCaiNiaoActual(miuiExpress: MiuiExpress): Pair<String?, List<ExpressDetails>> {
        return skrape(BrowserFetcher) {
            request {
                url {
                    protocol = UrlBuilder.Protocol.HTTPS
                    host = "page.cainiao.com"
                    port = -1
                    path = "/guoguo/app-myexpress-taobao/ld.html"
                    queryParam {
                        "mailNo" to miuiExpress.mailNumber
                        "cpCode" to miuiExpress.companyCode
                        "secretKey" to miuiExpress.secretKey
                        "from" to "XIAOMI"
                    }
                }
                userAgent = "Mozilla/5.0 (Linux; Android 12; M2102K1C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Mobile Safari/537.36 EdgA/105.0.1343.48"
                sslRelaxed = true
            }

            response {
                val jDoc = Jsoup.parse(responseBody)

                // Find express status
                val expressStatus = jDoc.selectFirst("body > div > div.cp-info.physical-border > div.cp-info_detail > div.package-status")
                    ?.text()

                val expressDetailsList = mutableListOf<ExpressDetails>()

                // Find express details
                jDoc.select("body > div > div.feed-container > ul > li")
                    .forEach { element ->
                        val detailContent = element.selectFirst("div.feed-item_content")
                            ?.text()
                        val detailDate =
                            element.selectFirst("div.feed-item_datetime > div.feed-item_date")
                                ?.text()
                        val detailTime =
                            element.selectFirst("div.feed-item_datetime > div.feed-item_time")
                                ?.text()

                        if (detailContent != null && detailDate != null && detailTime != null) {
                            expressDetailsList.add(
                                ExpressDetails(
                                    context = detailContent,
                                    formattedTime = "$detailDate $detailTime:00",
                                    time = "$detailDate $detailTime"
                                )
                            )
                        }
                    }

                Pair(expressStatus, expressDetailsList)
            }
        }
    }
}