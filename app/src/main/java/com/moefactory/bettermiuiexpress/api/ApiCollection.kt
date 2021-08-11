package com.moefactory.bettermiuiexpress.api

import com.moefactory.httputils.HttpUtils

object ApiCollection {
    val kuaiDi100Api by lazy {
        HttpUtils.createApi(ApiUrl.KUAIDI100_KEY, ApiUrl.KUAIDI100_URL, KuaiDi100Api::class.java)
    }
}

object ApiUrl {
    const val KUAIDI100_KEY = "kuaidi100"
    const val KUAIDI100_URL = "http://poll.kuaidi100.com/"
}