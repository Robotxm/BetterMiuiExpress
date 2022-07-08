package com.moefactory.bettermiuiexpress.hook

import android.content.Context
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.moefactory.bettermiuiexpress.BuildConfig
import com.moefactory.bettermiuiexpress.activity.ExpressDetailsActivity
import com.moefactory.bettermiuiexpress.api.KuaiDi100Api
import com.moefactory.bettermiuiexpress.base.app.*
import com.moefactory.bettermiuiexpress.base.intercepter.KuaiDi100Interceptor
import com.moefactory.bettermiuiexpress.ktx.ContextType
import com.moefactory.bettermiuiexpress.ktx.IntentType
import com.moefactory.bettermiuiexpress.ktx.JavaListClass
import com.moefactory.bettermiuiexpress.ktx.ViewType
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import com.moefactory.bettermiuiexpress.model.MiuiExpress
import com.moefactory.bettermiuiexpress.utils.ExpressCompanyUtils
import de.robv.android.xposed.XposedHelpers
import kotlinx.coroutines.runBlocking
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

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

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

    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://poll.kuaidi100.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()
    }

    private val kuaiDi100 by lazy {
        retrofit.create(KuaiDi100Api::class.java)
    }

    private fun getCompanyCode(response: String): String {
        when {
            // Normal
            response.startsWith("[") -> {
                val result = jsonParser.decodeFromString<List<KuaiDi100Company>>(response)
                return result[0].companyCode
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
    }

    override fun onInit() = configs {
        debugTag = "BetterMiuiExpress"
        isDebug = BuildConfig.DEBUG
    }

    override fun onHook() = encase {
        loadApp(name = PA_PACKAGE_NAME) {
            val expressEntryClass = try {
                PA_EXPRESS_ENTRY.clazz
            } catch (_: Exception) {
                PA_EXPRESS_ENTRY_OLD.clazz
            }

            // Old version
            // public static String gotoExpressDetailPage(Context context, ExpressEntry expressEntry, boolean z, boolean z2) {
            findClass(PA_EXPRESS_INTENT_UTILS, PA_EXPRESS_INTENT_UTILS_OLD)
                .hook {
                    injectMember {
                        method {
                            name = "gotoExpressDetailPage"
                            param(ContextType, expressEntryClass, BooleanType, BooleanType)
                        }.ignoredError()
                        replaceAny {
                            val context = args().first().cast<Context>()!!
                            val expressEntry = args(index = 1).any()!!
                            val companyCode = expressEntry.javaClass.getField("companyCode")
                                .get(expressEntry) as String
                            val companyName = expressEntry.javaClass.getField("companyName")
                                .get(expressEntry) as String
                            val mailNumber =
                                expressEntry.javaClass.getField("orderNumber").get(expressEntry) as String
                            val phoneNumber =
                                expressEntry.javaClass.getField("phone").get(expressEntry) as? String
                            // Check if the details will be showed in third-party apps(taobao, cainiao, etc.)
                            val uris =
                                expressEntry.javaClass.getMethod("getUris").invoke(expressEntry) as List<*>?
                            if (!uris.isNullOrEmpty()) {
                                // Store urls for future use such as jumping to third-party apps
                                val uriList = arrayListOf<String>()
                                for (uriEntity in uris) {
                                    val uriString = uriEntity!!.javaClass.getMethod("getLink")
                                        .invoke(uriEntity) as String
                                    uriList.add(uriString)
                                }
                                ExpressDetailsActivity.gotoDetailsActivity(
                                    context,
                                    MiuiExpress(companyCode, companyName, mailNumber, phoneNumber),
                                    uriList
                                )
                                return@replaceAny null
                            } else {
                                val provider = expressEntry.javaClass.getMethod("getProvider")
                                    .invoke(expressEntry) as? String
                                val isXiaomi = provider == "Miguo" || provider == "MiMall"
                                val isJingDong = companyCode == "JDKD"
                                // Details of packages from Xiaomi or JingDong will be showed in built-in app
                                if (!isXiaomi && !isJingDong) {
                                    ExpressDetailsActivity.gotoDetailsActivity(
                                        context,
                                        MiuiExpress(companyCode, companyName, mailNumber, phoneNumber),
                                        null
                                    )
                                    return@replaceAny null
                                }
                            }

                            // Other details will be processed normally
                            return@replaceAny method.invokeOriginal(*args)
                        }
                    }
                }

            // New version
            // public static String gotoExpressDetailPage(Context context, View view, ExpressEntry expressEntry, boolean z, boolean z2, Intent intent, int i2)
            findClass(PA_EXPRESS_INTENT_UTILS, PA_EXPRESS_INTENT_UTILS_OLD)
                .hook {
                    injectMember {
                        method {
                            name = "gotoExpressDetailPage"
                            param(
                                ContextType,
                                ViewType,
                                expressEntryClass,
                                BooleanType,
                                BooleanType,
                                IntentType,
                                IntType
                            )
                        }.ignoredError()
                        replaceAny {
                            val context = args().first().cast<Context>()!!
                            val expressEntry = args(index = 2).any()!!
                            val companyCode = expressEntry.javaClass.getField("companyCode")
                                .get(expressEntry) as String
                            val companyName = expressEntry.javaClass.getField("companyName")
                                .get(expressEntry) as String
                            val mailNumber =
                                expressEntry.javaClass.getField("orderNumber").get(expressEntry) as String
                            val phoneNumber =
                                expressEntry.javaClass.getField("phone").get(expressEntry) as? String
                            // Check if the details will be showed in third-party apps(taobao, cainiao, etc.)
                            val uris =
                                expressEntry.javaClass.getMethod("getUris").invoke(expressEntry) as List<*>?
                            if (!uris.isNullOrEmpty()) {
                                // Store urls for future use such as jumping to third-party apps
                                val uriList = arrayListOf<String>()
                                for (uriEntity in uris) {
                                    val uriString = uriEntity!!.javaClass.getMethod("getLink")
                                        .invoke(uriEntity) as String
                                    uriList.add(uriString)
                                }
                                ExpressDetailsActivity.gotoDetailsActivity(
                                    context,
                                    MiuiExpress(companyCode, companyName, mailNumber, phoneNumber),
                                    uriList
                                )
                                return@replaceAny null
                            } else {
                                val provider = expressEntry.javaClass.getMethod("getProvider")
                                    .invoke(expressEntry) as? String
                                val isXiaomi = provider == "Miguo" || provider == "MiMall"
                                val isJingDong = companyCode == "JDKD"
                                // Details of packages from Xiaomi or JingDong will be showed in built-in app
                                if (!isXiaomi && !isJingDong) {
                                    ExpressDetailsActivity.gotoDetailsActivity(
                                        context,
                                        MiuiExpress(companyCode, companyName, mailNumber, phoneNumber),
                                        null
                                    )
                                    return@replaceAny null
                                }
                            }

                            // Other details will be processed normally
                            return@replaceAny method.invokeOriginal(*args)
                        }
                    }
                }

            // Hook ExpressRepository$saveExpress
            // Save the latest detail
            findClass(PA_EXPRESS_REPOSITOIRY, PA_EXPRESS_REPOSITORY_OLD)
                .hook {
                    injectMember {
                        method {
                            name = "saveExpress"
                            param(JavaListClass)
                        }
                        beforeHook {
                            runBlocking {
                                args().first().cast<java.util.List<*>>()?.let { expressInfoList ->
                                    for (expressInfo in expressInfoList) {
                                        // Skip packages from Xiaomi and JingDong
                                        val provider =
                                            expressInfo.javaClass.getMethod("getProvider")
                                                .invoke(expressInfo) as? String
                                        val isXiaomi = provider == "Miguo" || provider == "MiMall"
                                        val companyCode =
                                            expressInfo.javaClass.getField("companyCode")
                                                .get(expressInfo) as String
                                        val isJingDong = companyCode == "JDKD"
                                        if (isXiaomi || isJingDong) {
                                            continue
                                        }

                                        // Get the company code
                                        val mailNumber =
                                            expressInfo.javaClass.getField("orderNumber")
                                                .get(expressInfo) as String
                                        val convertedCompanyCode =
                                            ExpressCompanyUtils.convertCode(companyCode)
                                                ?: getCompanyCode(
                                                    kuaiDi100.queryExpressCompany(
                                                        secretKey, mailNumber
                                                    )
                                                )

                                        // Get the details
                                        val data =
                                            jsonParser.encodeToString(
                                                KuaiDi100RequestParam(
                                                    convertedCompanyCode,
                                                    mailNumber
                                                )
                                            )
                                        val response = kuaiDi100.queryPackage(customer, data)
                                        // Ignore invalid result
                                        if (response.data.isNullOrEmpty()) {
                                            continue
                                        }

                                        // Prevent detail from disappearing
                                        expressInfo.javaClass.getMethod(
                                            "setClickDisappear",
                                            Boolean::class.javaPrimitiveType
                                        ).invoke(expressInfo, false)
                                        val originalDetails =
                                            expressInfo.javaClass.getField("details")
                                                .get(expressInfo) as? ArrayList<Any>
                                        val detailClass = try {
                                            PA_EXPRESS_INFO_DETAIL.clazz
                                        } catch (e: XposedHelpers.ClassNotFoundError) {
                                            PA_EXPRESS_INFO_DETAIL_OLD.clazz
                                        }
                                        when {
                                            originalDetails == null -> {
                                                // Null list, create a new instance and put the latest detail
                                                val newDetail = detailClass.newInstance()
                                                newDetail.javaClass.getMethod(
                                                    "setDesc",
                                                    java.lang.String::class.java
                                                ).invoke(newDetail, response.data[0].context)
                                                newDetail.javaClass.getMethod(
                                                    "setTime",
                                                    java.lang.String::class.java
                                                ).invoke(newDetail, response.data[0].formattedTime)
                                                val newDetails = ArrayList<Any>(1)
                                                newDetails.add(newDetail)
                                                expressInfo.javaClass
                                                    .getMethod(
                                                        "setDetails",
                                                        java.util.ArrayList::class.java
                                                    )
                                                    .invoke(expressInfo, newDetails)
                                            }
                                            originalDetails.isEmpty() -> {
                                                // Empty list, put the latest detail
                                                val newDetail = detailClass.newInstance()
                                                newDetail.javaClass.getMethod(
                                                    "setDesc",
                                                    java.lang.String::class.java
                                                ).invoke(newDetail, response.data[0].context)
                                                newDetail.javaClass.getMethod(
                                                    "setTime",
                                                    java.lang.String::class.java
                                                ).invoke(newDetail, response.data[0].formattedTime)
                                                originalDetails.add(newDetail)
                                            }
                                            else -> {
                                                // Normally, the original details contains one item
                                                val originalDetail =
                                                    (expressInfo.javaClass.getField("details")
                                                        .get(expressInfo) as List<*>)[0]
                                                originalDetail?.javaClass?.getMethod(
                                                    "setDesc",
                                                    java.lang.String::class.java
                                                )?.invoke(originalDetail, response.data[0].context)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
}