package com.moefactory.bettermiuiexpress.hook

import com.highcapable.yukihookapi.annotation.xposed.YukiHookLibXposedEntry
import com.highcapable.yukihookapi.hook.factory.configure
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.YukiHookXposedModule
import com.moefactory.bettermiuiexpress.BuildConfig
import com.moefactory.bettermiuiexpress.base.app.PA_PACKAGE_NAME
import com.moefactory.bettermiuiexpress.hook.subhooks.PAExpressRepositoryHook
import com.moefactory.bettermiuiexpress.hook.subhooks.PAExpressRouterHook
import com.moefactory.bettermiuiexpress.utils.OkHttpInitializer

@YukiHookLibXposedEntry(
    minApiVersion = 101,
    targetApiVersion = 102,
    scope = [PA_PACKAGE_NAME]
)
object HookEntry : YukiHookXposedModule {

    override fun onInit() = configure {
        debug = BuildConfig.DEBUG
        logging {
            tag = "BetterMiuiExpress"
        }
    }

    override fun onHook() = encase {
        loadApp(name = PA_PACKAGE_NAME) {
            registerAppLifecycle {
                attachBaseContext { context, hasCalledSuper ->
                    if (hasCalledSuper) {
                        OkHttpInitializer.initialize(context)
                    }
                }
            }
            loadHooker(PAExpressRouterHook)
            loadHooker(PAExpressRepositoryHook)
        }
    }
}
