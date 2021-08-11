package com.moefactory.httputils.`interface`

fun interface BuildHeadersListener {
    fun buildHeaders(): Map<String, String>
}