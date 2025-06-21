package com.moefactory.bettermiuiexpress.hook.subhooks

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_ENTRY
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_ROUTER
import com.moefactory.bettermiuiexpress.hook.bridge.jumpToDetailsActivity
import com.moefactory.bettermiuiexpress.ktx.ContextClass
import com.moefactory.bettermiuiexpress.ktx.ObjectClass
import com.moefactory.bettermiuiexpress.model.toExpressEntryWrapper

// From PA 5.5.55, use ExpressRouter
// public static void route(Context context, Object obj, ExpressEntry expressEntry)
object PAExpressRouterHook : YukiBaseHooker() {

    override fun onHook() {
        PA_EXPRESS_ROUTER.toClassOrNull()?.method {
            val expressEntryClass = PA_EXPRESS_ENTRY.toClassOrNull() ?: return

            name = "route"
            param(ContextClass, ObjectClass, expressEntryClass)
        }?.hook {
            replaceAny {
                val context = args().first().cast<Context>()!!
                val expressEntry = args(2).any()!!
                val expressEntryWrapper = expressEntry.toExpressEntryWrapper()
                if (jumpToDetailsActivity(context, expressEntryWrapper)) {
                    return@replaceAny null
                }

                // Other details will be processed normally
                return@replaceAny invokeOriginal(*args)
            }
        }
    }
}