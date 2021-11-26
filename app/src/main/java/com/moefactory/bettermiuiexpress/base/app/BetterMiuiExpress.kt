package com.moefactory.bettermiuiexpress.base.app

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager

class BetterMiuiExpress : Application() {

    override fun onCreate() {
        super.onCreate()

        if (isLauncherIconEnabled()) {
            hideLauncherIcon()
        }
    }

    private fun hideLauncherIcon() {
        packageManager.setComponentEnabledSetting(
            ComponentName(this, BME_MAIN_ACTIVITY_ALIAS),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun isLauncherIconEnabled(): Boolean {
        val value =
            packageManager.getComponentEnabledSetting(ComponentName(this, BME_MAIN_ACTIVITY_ALIAS))
        return (value == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || value == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    }
}