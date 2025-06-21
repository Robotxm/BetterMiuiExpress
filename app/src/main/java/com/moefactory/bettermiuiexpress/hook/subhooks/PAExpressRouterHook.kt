package com.moefactory.bettermiuiexpress.hook.subhooks

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_ENTRY
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_ROUTER
import com.moefactory.bettermiuiexpress.hook.bridge.jumpToDetailsActivity
import com.moefactory.bettermiuiexpress.ktx.ContextClass
import com.moefactory.bettermiuiexpress.ktx.ObjectClass
import com.moefactory.bettermiuiexpress.model.isJingDong
import com.moefactory.bettermiuiexpress.model.shouldUseNativeUI
import com.moefactory.bettermiuiexpress.model.toExpressEntryWrapper

// From PA 5.5.55, use ExpressRouter
// public static void route(Context context, Object obj, ExpressEntry expressEntry)
object PAExpressRouterHook : YukiBaseHooker() {

    override fun onHook() {
        val paExpressRouterClass = PA_EXPRESS_ROUTER.toClassOrNull()

        paExpressRouterClass?.method {
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
                } else if (expressEntryWrapper.shouldUseNativeUI() && expressEntryWrapper.isJingDong) {
                    // From new versions of PA, details of packages from JingDong will be display in JD app by default, which is unexpected
                    // Here we just intercept it
                    args(1).any()?.let { arg1 ->
                        paExpressRouterClass.method {
                            name = "gotoNative"
                            param(ContextClass, ObjectClass, expressEntry::class.java)
                        }.get().call(context, arg1, expressEntry)

                        return@replaceAny null
                    }
                }

                // Other details will be processed normally
                return@replaceAny invokeOriginal(*args)
            }
        }
    }
}