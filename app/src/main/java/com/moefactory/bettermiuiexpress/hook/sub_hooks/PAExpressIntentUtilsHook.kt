package com.moefactory.bettermiuiexpress.hook.sub_hooks

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_ENTRY
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_INTENT_UTILS
import com.moefactory.bettermiuiexpress.hook.bridge.jumpToDetailsActivity
import com.moefactory.bettermiuiexpress.ktx.ContextClass
import com.moefactory.bettermiuiexpress.ktx.IntentClass
import com.moefactory.bettermiuiexpress.ktx.ViewClass
import com.moefactory.bettermiuiexpress.model.toExpressEntryWrapper

// New version
// public static String gotoExpressDetailPage(Context context, View view, ExpressEntry expressEntry, boolean z, boolean z2, Intent intent, int i2)
object PAExpressIntentUtilsHook : YukiBaseHooker() {

    override fun onHook() {
        PA_EXPRESS_INTENT_UTILS.toClassOrNull()?.method {
            val expressEntryClass = PA_EXPRESS_ENTRY.toClassOrNull() ?: return

            name = "gotoExpressDetailPage"
            name = "gotoExpressDetailPage"
            param(
                ContextClass,
                ViewClass,
                expressEntryClass,
                BooleanType,
                BooleanType,
                IntentClass,
                IntType
            )
        }?.hook {
            replaceAny {
                val context = args().first().cast<Context>()!!
                val expressEntry = args(index = 2).any()
                val expressEntryWrapper = expressEntry?.toExpressEntryWrapper()
                if (expressEntryWrapper != null && jumpToDetailsActivity(context, expressEntryWrapper)) {
                    return@replaceAny null
                }

                // Other details will be processed normally
                return@replaceAny invokeOriginal(*args)
            }
        }
    }
}