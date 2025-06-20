package com.moefactory.bettermiuiexpress.model

import android.util.Base64
import com.moefactory.bettermiuiexpress.ktx.toByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
open class KuaiDi100BaseRequestParam(
    @SerialName("appid") val appId: String = "com.Kingdee.Express",
    val versionCode: Int = 852,
    @SerialName("os_version") val osVersion: String = "android15",
    @SerialName("os_name") val osName: String = "2304FPN6DC",
    @SerialName("t") val time: String = System.currentTimeMillis().toString(),
    @SerialName("tra") open val trackId: String,
    @SerialName("uchannel") val userChannel: String = "null",
    @SerialName("nt") val networkType: String = "wifi",
    @SerialName("apiversion") val apiVersion: Int = 31,
    val deviceId: String = UUID.randomUUID().toString().let { it.substring(0..(it.length / 2)) },
)

@Serializable
data class KuaiDi100ExpressDetailsRequestParam(
    @SerialName("num") val mailNumber: String,
    @SerialName("com") val companyCode: String,
    override val trackId: String,
    val type: String = "detail",
    val phone: String? = null,
) : KuaiDi100BaseRequestParam(trackId = trackId)

@Serializable
data class KuaiDi100ExpressRegisterDeviceTrackIdRequestParam(
    @SerialName("device_token") val deviceToken: String = Base64.encodeToString(UUID.randomUUID().toByteArray(), Base64.NO_WRAP),
    @SerialName("third_type") val thirdType: String = "XIAOMI",
    val isOpenNotice: Int = 2,
    override val trackId: String,
) : KuaiDi100BaseRequestParam(trackId = trackId)

@Serializable
data class KuaiDi100BaseResponse<T>(
    val status: String? = null,
    val lastResult: T? = null
)

@Serializable
data class Kuaidi100ExpressDetailsResult(
    val state: String? = null,
    val data: List<KuaiDi100ExpressDetails>? = null
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
data class KuaiDi100Company(
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