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
    @Serializable(with = IntAsBooleanSerializer::class)
    val isReceived: Boolean? = null,
    @SerialName("com")
    val companyCode: String? = null,
    @SerialName("nu")
    val mailNumber: String? = null,
    val data: List<ExpressDetails>? = null,

    /* Unexpected response fields */
    val result: Boolean? = null,
    val returnCode: String? = null
)

@Serializable
data class ExpressDetails(
    val context: String,
    val time: String,
    @SerialName("ftime")
    val formatedTime: String,
    val status: String? = null,
    val areaCode: String? = null,
    val areaName: String? = null
)

@Serializable
data class KuaiDi100RequestParam(
    @SerialName("com")
    val companyCode: String,
    @SerialName("num")
    val mailNumber: String
)

@Serializable
data class KuaiDi100CompanyQueryRequestParam(
    @SerialName("key")
    val secretKey: String,
    @SerialName("num")
    val mailNumber: String,
)

@Serializable
data class KuaiDi100Company(
    val lengthPre: Int,
    @SerialName("comCode")
    val companyCode: String,
    @SerialName("name")
    val companyName: String
)

object KuaiDi100ExpressState {
    data class ExpressState(
        val categoryCode: Int,
        val categoryName: String,
        val code: Int,
        val name: String,
        val description: String
    )

    val statesMap = listOf(
        OnTheWay.ArrivedInReceiverCity,
        OnTheWay.OnTheWay,

        Packed.OrderPlaced,
        Packed.WaitForPack,
        Packed.Packed,

        Trouble.TimeoutToBeReceived,
        Trouble.TimeoutForUpdate,
        Trouble.RejectedWithoutConfirmation,
        Trouble.ExceptionWhileDelivering,
        Trouble.TimeoutToBeFetched,
        Trouble.OutOfCommunication,
        Trouble.OutOfArea,
        Trouble.Stop,
        Trouble.Broken,

        Received.ReceivedInPerson,
        Received.ReceivedAfterException,
        Received.ReceivedByOthers,
        Received.ReceivedByStation,

        Rejected.Revoked,
        Rejected.Rejected,

        Delivering.DeliveredAtStation,

        Withdrawal.Withdrawal,

        Transferred.Transferred,

        Customs.WaitForClearance,
        Customs.Clearing,
        Customs.Cleared,
        Customs.ExceptionOccurredWhileCleared,

        )

    /* 0: On the way */
    object OnTheWay {
        val categoryCode = 0
        val categoryName = "在途" // 在途
        val ArrivedInReceiverCity = ExpressState(categoryCode, categoryName, 1001, "到达派件城市", "快件到达收件人城市")
        val OnTheWay = ExpressState(categoryCode, categoryName, 1002, "干线", "快件处于运输过程中")
    }

    /* 1: Packed */
    object Packed {
        val categoryCode = 1
        val categoryName = "揽收" // 揽收
        val OrderPlaced = ExpressState(categoryCode, categoryName, 101, "已下单", "已经下快件单")
        val WaitForPack = ExpressState(categoryCode, categoryName, 102, "待揽收", "待快递公司揽收")
        val Packed = ExpressState(categoryCode, categoryName, 103, "已揽收", "快递公司已经揽收")
    }

    /* 2: Trouble*/
    object Trouble {
        val categoryCode = 2
        val categoryName = "疑难" // 疑难
        val TimeoutToBeReceived = ExpressState(categoryCode, categoryName, 201, "超时未签收", "快件长时间派件后未签收")
        val TimeoutForUpdate = ExpressState(categoryCode, categoryName, 202, "超时未更新", "快件长时间没有派件或签收")
        val RejectedWithoutConfirmation = ExpressState(categoryCode, categoryName, 203, "拒收", "收件人发起拒收快递,待发货方确认")
        val ExceptionWhileDelivering = ExpressState(categoryCode, categoryName, 204, "派件异常", "快件派件时遇到异常情况")
        val TimeoutToBeFetched = ExpressState(categoryCode, categoryName, 205, "柜或驿站超时未取", "快件在快递柜或者驿站长时间未取")
        val OutOfCommunication = ExpressState(categoryCode, categoryName, 206, "无法联系", "无法联系到收件人")
        val OutOfArea = ExpressState(categoryCode, categoryName, 207, "超区", "超出快递公司的服务区范围")
        val Stop = ExpressState(categoryCode, categoryName, 208, "滞留", "快件滞留在网点，没有派送")
        val Broken = ExpressState(categoryCode, categoryName, 209, "破损", "快件破损")
    }

    /* 3: Received */
    object Received {
        val categoryCode = 3
        val categoryName = "签收" // 签收
        val ReceivedInPerson = ExpressState(categoryCode, categoryName, 301, "本人签收", "收件人正常签收")
        val ReceivedAfterException = ExpressState(categoryCode, categoryName, 302, "派件异常后签收", "快件显示派件异常，但后续正常签收")
        val ReceivedByOthers = ExpressState(categoryCode, categoryName, 303, "代签", "快件已被代签")
        val ReceivedByStation = ExpressState(categoryCode, categoryName, 304, "投柜或站签收", "快件已由快递柜或者驿站签收")
    }

    /* 4: Rejected */
    object Rejected {
        val categoryCode = 4
        val categoryName = "退签" // 退签
        val Revoked = ExpressState(categoryCode, categoryName, 401, "已销单", "此快件单已撤销")
        val Rejected = ExpressState(categoryCode, categoryName, 14, "拒签", "收件人拒签快件")
    }

    /* 5: Delivering */
    object Delivering {
        val categoryCode = 5
        val categoryName = "派件" // 派件
        val DeliveredAtStation = ExpressState(categoryCode, categoryName, 501, "投柜或驿站", "快件已经投递到快递柜或者快递驿站")
    }

    /* 6: Withdrawal */
    object Withdrawal {
        val categoryCode = 6
        val categoryName = "退回" // 退回
        val Withdrawal = ExpressState(categoryCode, categoryName, 6, "退回", "快件正处于返回发货人的途中")
    }

    /* 7: Transferred */
    object Transferred {
        val categoryCode = 7
        val categoryName = "转投" // 转投
        val Transferred = ExpressState(categoryCode, categoryName, 7, "转投", "快件转给其他快递公司邮寄")
    }

    /* 8: Customs */
    object Customs {
        val categoryCode = 8
        val categoryName = "清关" // 清关
        val WaitForClearance = ExpressState(categoryCode, categoryName, 10, "待清关", "快件等待清关")
        val Clearing = ExpressState(categoryCode, categoryName, 11, "清关中", "快件正在清关流程中")
        val Cleared = ExpressState(categoryCode, categoryName, 12, "已清关", "快件已完成清关流程")
        val ExceptionOccurredWhileCleared = ExpressState(categoryCode, categoryName, 13, "清关异常", "货物在清关过程中出现异常")
    }
}