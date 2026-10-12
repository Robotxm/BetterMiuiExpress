package com.moefactory.bettermiuiexpress.utils

import android.content.Context
import android.content.ContextWrapper
import com.moefactory.bettermiuiexpress.BuildConfig
import okhttp3.OkHttp

object OkHttpInitializer {

    fun initialize(hostContext: Context) {
        val moduleContext = hostContext.createPackageContext(BuildConfig.APPLICATION_ID, 0)

        // OkHttp reads its public suffix list from the module APK's assets. Returning
        // the host Application here would make OkHttp look in the host APK instead.
        val assetContext = object : ContextWrapper(moduleContext) {
            override fun getApplicationContext(): Context = this
        }
        OkHttp.initialize(assetContext)
    }
}
