package com.moefactory.bettermiuiexpress.hook

import com.highcapable.yukihookapi.annotation.xposed.YukiHookLibXposedEntry
import com.highcapable.yukihookapi.hook.factory.configure
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.YukiHookXposedModule
import com.highcapable.yukihookapi.hook.xposed.reload.lifecycle.registerModuleLifecycle
import com.moefactory.bettermiuiexpress.BuildConfig
import com.moefactory.bettermiuiexpress.base.app.PA_PACKAGE_NAME
import com.moefactory.bettermiuiexpress.hook.subhooks.PAExpressRepositoryHook
import com.moefactory.bettermiuiexpress.hook.subhooks.PAExpressRouterHook
import com.moefactory.bettermiuiexpress.utils.OkHttpInitializer

@YukiHookLibXposedEntry(
    minApiVersion = 101,
    targetApiVersion = 102,
    hotReload = YukiHookLibXposedEntry.HotReload.AUTO,
    scope = [PA_PACKAGE_NAME]
)
object HookEntry : YukiHookXposedModule {

    override fun onInit() {
        configure {
            debug = BuildConfig.DEBUG
            logging {
                tag = "BetterMiuiExpress"
            }
        }

        YLog.info("Module initialized: version=${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}), hotReload=AUTO")
        registerModuleLifecycle {
            canReload {
                val allowed = PAExpressRepositoryHook.canReload()
                if (allowed) {
                    YLog.info("Hot reload requested: module work is idle; waiting for framework handoff")
                }
                allowed
            }
            onSaveState {
                // Recheck after YukiHook freezes managed callbacks: one may have queued work since canReload.
                check(PAExpressRepositoryHook.canReload()) { "Express queries are still running; retry the reload later" }
                YLog.info("Hot reload: managed callbacks frozen, saving module state")
                null
            }
            onDispose {
                YLog.info("Hot reload: disposing old module resources")
                try {
                    PAExpressRepositoryHook.dispose()
                    YLog.info("Hot reload: old module resources released")
                } catch (exception: Exception) {
                    YLog.error("Hot reload: resource disposal failed", e = exception)
                    throw exception
                }
            }
            onReloaded {
                YLog.info("Hot reload completed: version=${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            }
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
            // Completed Application lifecycle callbacks are not replayed after a hot reload.
            withApplicationReady { application ->
                OkHttpInitializer.initialize(application)
            }
            loadHooker(PAExpressRouterHook)
            loadHooker(PAExpressRepositoryHook)
        }
    }
}
