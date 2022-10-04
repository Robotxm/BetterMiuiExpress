package com.moefactory.bettermiuiexpress.utils

import java.security.MessageDigest

object SignUtils {
    fun signForKuaiDi100(param: String, key: String, customer: String): String {
        return "$param$key$customer".upperMD5()
    }

    fun signForCaiNiao(token: String, timestamp: String, appKey: String, data: String): String {
        return "$token&$timestamp&$appKey&$data".lowerMD5()
    }
}

fun String.upperMD5() = MessageDigest.getInstance("MD5")
    .digest(this.toByteArray()).run { joinToString("") { "%02X".format(it) } }

fun String.lowerMD5() = upperMD5().lowercase()