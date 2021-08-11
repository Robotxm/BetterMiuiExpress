package com.moefactory.bettermiuiexpress.hook

import android.content.Context
import com.moefactory.bettermiuiexpress.activity.ExpressDetailsActivity
import com.moefactory.bettermiuiexpress.model.MiuiExpress
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.miui.personalassistant") {
            return
        }

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
                            val uriString = uriEntity!!.javaClass.getMethod("getLink").invoke(uriEntity) as String
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

}