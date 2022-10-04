package com.moefactory.bettermiuiexpress.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaiNiaoBaseResponse<T>(
    @SerialName("c") val token: String? = null,
    val data: T,
    @SerialName("ret") val results: List<String>
)

@Serializable
data class CaiNiaoExpressDetailsResponse(
    @SerialName("result") val results: List<CaiNiaoExpressDetailsResult>? = null
)

@Serializable
data class CaiNiaoExpressDetailsResult(
    @SerialName("fullTraceDetail") val fullTraceDetails: List<CaiNiaoExpressDetails>? = null,
    val packageStatus: CaiNiaoPackageStatus? = null
)

@Serializable
data class CaiNiaoExpressDetails(
    val time: String,
    @SerialName("desc") val description: String,
    @SerialName("standerdDesc") val standardDescription: String
)

@Serializable
data class CaiNiaoPackageStatus(
    val newStatusCode: String,
    @SerialName("newStatusDesc") val newStatusDescription: String,
    val status: String,
    val statusCode: String
)

@Serializable
class PlaceholderObject

@Serializable
data class CaiNiaoRequestData(
    @SerialName("mailNo") val mailNumber: String,
    val appName: String? = "GUOGUO",
    val actor: String? = "RECEIVER",
    @SerialName("isAccoutOut") val isAccountOut: Boolean? = true,
    val isShowConsignDetail: Boolean? = true,
    val ignoreInvalidNode: Boolean? = true,
    val isUnique: Boolean? = true,
    val isStandard: Boolean? = true,
    val isShowItem: Boolean? = true,
    val isShowTemporalityService: Boolean? = true,
    val isShowCommonService: Boolean? = true,
    val isStandardActionCode: Boolean? = true,
    val isOrderByAction: Boolean? = true,
    val isShowExpressMan: Boolean? = true,
    val isShowProgressbar: Boolean? = true,
    val isShowLastOneService: Boolean? = true,
    val isShowServiceProvider: Boolean? = true,
    val isShowDeliveryProgress: Boolean? = true
)