package com.moefactory.bettermiuiexpress.utils

import java.security.MessageDigest

fun String.upperMD5() = MessageDigest.getInstance("MD5")
    .digest(this.toByteArray()).joinToString("") { "%02X".format(it) }