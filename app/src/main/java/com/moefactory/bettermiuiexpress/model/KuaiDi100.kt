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

enum class KuaiDi100ExpressState(
    val stateCode: Int,
    val stateName: String
) {
    TRANSPORTING(0, "运输中"),
    ACCEPTED(1, "已揽件"),
    TROUBLE(2, "疑难件"),
    RECEIVED(3, "已签收"),
    REJECTED(4, "已拒签"),
    DELIVERING(5, "派送中"),
    WITHDRAWAL(6, "运输中"),
    TRANSFERRED(7, "已转寄"),
    EXPRESS_STATE(8, "清关中")
}