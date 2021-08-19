package com.moefactory.bettermiuiexpress.hook

import android.content.Context
import com.moefactory.bettermiuiexpress.activity.ExpressDetailsActivity
import com.moefactory.bettermiuiexpress.api.ApiCollection
import com.moefactory.bettermiuiexpress.base.app.customer
import com.moefactory.bettermiuiexpress.base.app.secretKey
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import com.moefactory.bettermiuiexpress.model.MiuiExpress
import com.moefactory.bettermiuiexpress.utils.ExpressCompanyUtils
import com.moefactory.bettermiuiexpress.utils.SignUtils
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "com.miui.personalassistant") {
            hookForExpressDetails(lpparam)
            hookForExpressCardView(lpparam)
        }
    }

    private fun hookForExpressDetails(lpparam: XC_LoadPackage.LoadPackageParam) {
        val expressIntentUtilsClass = XposedHelpers.findClass(
            "com.miui.personalassistant.express.ExpressIntentUtils",
            lpparam.classLoader
        )
        val expressEntryClass = XposedHelpers.findClass(
            "com.miui.personalassistant.express.bean.ExpressEntry",
            lpparam.classLoader
        )
        XposedHelpers.findAndHookMethod(
            expressIntentUtilsClass,
            "gotoExpressDetailPage",
            Context::class.java,
            expressEntryClass,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    val context = param.args[0] as Context
                    val expressEntry = param.args[1]
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
                    if (uris != null && uris.isNotEmpty()) {
                        // Store urls for future use to jump to third-party apps
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
                        return null
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
                            return null
                        }
                    }

                    // Other details will be processed normally
                    return XposedBridge.invokeOriginalMethod(
                        param.method,
                        param.thisObject,
                        param.args
                    )
                }
            })
    }

    private fun hookForExpressCardView(lpparam: XC_LoadPackage.LoadPackageParam) {
        val expressRepositoryClass = XposedHelpers.findClass(
            "com.miui.personalassistant.express.ExpressRepository",
            lpparam.classLoader
        )
        XposedHelpers.findAndHookMethod(
            expressRepositoryClass,
            "saveExpress",
            java.util.List::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runBlocking {
                        val expressInfoList = param.args[0] as java.util.List<*>
                        for (expressInfo in expressInfoList) {
                            expressInfo.javaClass.getMethod(
                                "setClickDisappear",
                                Boolean::class.javaPrimitiveType
                            ).invoke(expressInfo, false)

                            val mailNumber = expressInfo.javaClass.getField("orderNumber")
                                .get(expressInfo) as String
                            val companyCode = expressInfo.javaClass.getField("companyCode")
                                .get(expressInfo) as String
                            var convertedCompanyCode = ExpressCompanyUtils.convertCode(companyCode)
                            if (convertedCompanyCode == null) {
                                convertedCompanyCode = getCompanyCode(
                                    ApiCollection.kuaiDi100Api.queryExpressCompany(
                                        secretKey,
                                        mailNumber
                                    )
                                )
                            }

                            val data =
                                Json.encodeToString(
                                    KuaiDi100RequestParam(
                                        convertedCompanyCode,
                                        mailNumber
                                    )
                                )
                            val response = ApiCollection.kuaiDi100Api.queryPackage(
                                customer,
                                data,
                                SignUtils.sign(data, secretKey, customer)
                            )
                            val originalDetail = (expressInfo.javaClass.getField("details")
                                .get(expressInfo) as List<*>)[0]!!
                            originalDetail.javaClass.getMethod(
                                "setDesc",
                                java.lang.String::class.java
                            ).invoke(originalDetail, response.data!![0].context)
                        }
                    }
                }
            }
        )
    }

    private fun getCompanyCode(response: String): String {
        when {
            // Normal
            response.startsWith("[") -> {
                val result = Json.decodeFromString<List<KuaiDi100Company>>(response)
                return result[0].companyCode
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
    }

}