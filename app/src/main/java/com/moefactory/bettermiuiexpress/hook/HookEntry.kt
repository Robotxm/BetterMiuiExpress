package com.moefactory.bettermiuiexpress.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.moefactory.bettermiuiexpress.BuildConfig
import com.moefactory.bettermiuiexpress.base.app.PA_PACKAGE_NAME
import com.moefactory.bettermiuiexpress.hook.subhooks.PAExpressRepositoryHook
import com.moefactory.bettermiuiexpress.hook.subhooks.PAExpressRouterHook
import com.moefactory.bettermiuiexpress.utils.OkHttpInitializer

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        isDebug = BuildConfig.DEBUG
        debugLog {
            tag = "BetterMiuiExpress"
        }
    }

    override fun onHook() = encase {
        loadApp(name = PA_PACKAGE_NAME) {
            onAppLifecycle {
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
