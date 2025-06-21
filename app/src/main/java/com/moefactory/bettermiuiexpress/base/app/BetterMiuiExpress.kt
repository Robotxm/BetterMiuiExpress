package com.moefactory.bettermiuiexpress.base.app

import com.highcapable.yukihookapi.hook.factory.prefs
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import com.moefactory.bettermiuiexpress.ktx.hideLauncherIcon
import com.moefactory.bettermiuiexpress.ktx.isLauncherIconEnabled

class BetterMiuiExpress : ModuleApplication() {

    override fun onCreate() {
        super.onCreate()

        if (isLauncherIconEnabled() && prefs().getString(PREF_KEY_DEVICE_TRACK_ID).isNotEmpty()) {
            hideLauncherIcon()
        }
    }
}