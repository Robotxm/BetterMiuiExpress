package com.moefactory.bettermiuiexpress.base.app

import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import com.moefactory.bettermiuiexpress.ktx.hideLauncherIcon
import com.moefactory.bettermiuiexpress.ktx.deviceTrackId
import com.moefactory.bettermiuiexpress.ktx.isLauncherIconEnabled

class BetterMiuiExpress : ModuleApplication() {

    override fun onCreate() {
        super.onCreate()

        YukiHook.Status.observeFrameworkService { available ->
            if (available) {
                val trackId = deviceTrackId
                if (trackId.isNotEmpty()) {
                    deviceTrackId = trackId
                    if (isLauncherIconEnabled()) {
                        hideLauncherIcon()
                    }
                }
            }
        }
    }
}
