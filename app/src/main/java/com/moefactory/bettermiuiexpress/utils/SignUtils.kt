package com.moefactory.bettermiuiexpress.utils

import java.security.MessageDigest

object SignUtils {
    fun sign(param: String, key: String, customer: String): String {
        return md5Hash("$param$key$customer".toByteArray())
    }

    private fun md5Hash(message: ByteArray): String {
        try {
            val hash = MessageDigest.getInstance("MD5").digest(message)
            val hex = StringBuilder(hash.size * 2)
            for (b in hash) {
                val i = b.toInt() and 0xFF
                if (i < 0x10) {
                    hex.append('0')
                }
                hex.append(Integer.toHexString(i).uppercase())
            }

            return hex.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }
}