package com.moefactory.bettermiuiexpress.utils

import java.math.BigInteger
import java.security.MessageDigest

object SignUtils {
    fun sign(param: String, key: String, customer: String) = "$param$key$customer".md5()

    private fun String.md5() =
        BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)
            .padStart(32, '0')
}