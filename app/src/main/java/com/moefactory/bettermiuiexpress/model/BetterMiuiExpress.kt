package com.moefactory.bettermiuiexpress.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class MiuiExpress(
    val companyCode: String,
    val companyName: String,
    val mailNumber: String,
    val phoneNumber: String?
) : Parcelable

@Parcelize
data class ExpressDetails(
    val dataSource: String,
    val status: String,
    val traces: List<ExpressTrace>
) : Parcelable

@Parcelize
data class ExpressTrace(
    val fullDateTime: String,
    val date: String,
    val time: String,
    val description: String
) : Parcelable, Comparable<ExpressTrace> {

    override operator fun compareTo(other: ExpressTrace): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

        return sdf.parse(fullDateTime)!!.compareTo(sdf.parse(other.fullDateTime))
    }
}

fun KuaiDi100ExpressDetails.toExpressTrace(): ExpressTrace {
    val originalSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val newSdf = SimpleDateFormat("MM-dd\nHH:mm", Locale.CHINA)
    val dateTime = originalSdf.parse(formattedTime)
    val newDateTime = newSdf.format(dateTime!!).split("\n")

    return ExpressTrace(formattedTime, newDateTime[0], newDateTime[1], context)
}

fun CaiNiaoExpressDetails.toExpressTrace(): ExpressTrace {
    val originalSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val newSdf = SimpleDateFormat("MM-dd\nHH:mm", Locale.CHINA)
    val dateTime = originalSdf.parse(time)
    val newDateTime = newSdf.format(dateTime!!).split("\n")

    return ExpressTrace(time, newDateTime[0], newDateTime[1], description)
}
