package com.moefactory.bettermiuiexpress.base.app

import android.app.Application
import android.webkit.WebSettings
import com.moefactory.bettermiuiexpress.api.ApiUrl
import com.moefactory.bettermiuiexpress.base.intercepter.KuaiDi100Interceptor
import com.moefactory.httputils.HttpUtils
import com.moefactory.httputils.`interface`.BuildHeadersListener
import com.moefactory.httputils.config.OkHttpConfig
import com.moefactory.httputils.cookie.PersistentCookieStore

class BetterMiuiExpress : Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        val customOkHttpClient = OkHttpConfig.Builder(this).apply {
            cookieStore = PersistentCookieStore(this@BetterMiuiExpress)
            connectTimeout = 20
            readTimeout = 20
            writeTimeout = 20
            buildHeadersListener = BuildHeadersListener {
                mapOf("User-Agent" to WebSettings.getDefaultUserAgent(baseContext))
            }
            interceptors = arrayOf(KuaiDi100Interceptor())
        }.build()
        HttpUtils.INSTANCE
            .init(this)
            .config()
            .setBaseUrl(ApiUrl.KUAIDI100_URL)
            .apply { okHttpClient = customOkHttpClient }
    }

    companion object {
        lateinit var INSTANCE: BetterMiuiExpress
            private set
    }
}

const val secretKey = "NECqxUrS2626"
const val customer = "157E92B1594C114CA2377A627EC0892F"
