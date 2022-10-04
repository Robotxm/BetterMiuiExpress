package com.moefactory.bettermiuiexpress.model

import com.moefactory.bettermiuiexpress.serializer.IntAsBooleanSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseKuaiDi100Response(
    /* Common fields */
    val message: String,

    /* Normal response fields */
    val state: Int? = null,
    val status: Int? = null,
    val condition: String? = null,
    @SerialName("ischeck")
    @Serializable(with = IntAsBooleanSerializer::class) val isReceived: Boolean? = null,
    @SerialName("com") val companyCode: String? = null,
    @SerialName("nu") val mailNumber: String? = null,
    val data: List<KuaiDi100ExpressDetails>? = null,

    /* Unexpected response fields */
    val result: Boolean? = null,
    val returnCode: String? = null
)

@Serializable
data class KuaiDi100ExpressDetails(
    val context: String,
    val time: String,
    @SerialName("ftime") val formattedTime: String,
    val status: String? = null,
    val areaCode: String? = null,
    val areaName: String? = null
)

@Serializable
data class KuaiDi100RequestParam(
    @SerialName("com") val companyCode: String,
    @SerialName("num") val mailNumber: String,
    val phone: String? = null
)

@Serializable
data class KuaiDi100Company(
    val lengthPre: Int,
    @SerialName("comCode") val companyCode: String,
    @SerialName("name") val companyName: String
)

object KuaiDi100ExpressState {
    data class ExpressState(
        val categoryCode: Int,
        val categoryName: String
    )

    const val Transporting = 0
    const val Accepted = 1
    const val Trouble = 2
    const val Received = 3
    const val Rejected = 4
    const val Delivering = 5
    const val Withdrawal = 6
    const val Transferred = 7
    const val InCustoms = 8

    val statesMap = listOf(
        ExpressState(Transporting, "运输中"),
        ExpressState(Accepted, "已揽件"),
        ExpressState(Trouble, "疑难件"),
        ExpressState(Received, "已签收"),
        ExpressState(Rejected, "已拒签"),
        ExpressState(Delivering, "派送中"),
        ExpressState(Withdrawal, "运输中"),
        ExpressState(Transferred, "已转寄"),
        ExpressState(InCustoms, "清关中"),
    )
}