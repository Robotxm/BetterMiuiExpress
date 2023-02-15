package com.moefactory.bettermiuiexpress.model

import android.os.Parcelable
import com.moefactory.bettermiuiexpress.ktx.BooleanPrimitiveType
import com.moefactory.bettermiuiexpress.ktx.JavaStringClass
import kotlinx.parcelize.Parcelize

fun Any.toExpressInfoWrapper() = ExpressInfoWrapper(this)

fun Any.toExpressInfoUriWrapper(): ExpressInfoUriWrapper {
    val expressInfoUriClass = this.javaClass
    val link = expressInfoUriClass.getMethod("getLink").invoke(this) as String
    val priority = expressInfoUriClass.getMethod("getPriority").invoke(this) as Int
    return ExpressInfoUriWrapper(link, priority)
}

fun Any.toExpressEntryWrapper() = ExpressEntryWrapper(this)

fun Any.toExpressInfoDetailWrapper() = ExpressInfoDetailWrapper(this)

class ExpressInfoWrapper(private val expressInfoObject: Any) {

    private val expressInfoClass = expressInfoObject.javaClass

    val provider: String?
        get() = expressInfoClass.getMethod("getProvider").invoke(expressInfoObject) as? String
    val companyCode: String
        get() = expressInfoClass.getField("companyCode").get(expressInfoObject) as String
    val orderNumber: String
        get() = expressInfoClass.getField("orderNumber").get(expressInfoObject) as String
    var clickDisappear: Boolean
        get() = expressInfoClass.getMethod("isClickDisappear").invoke(expressInfoObject) as Boolean
        set(value) {
            expressInfoClass.getMethod("setClickDisappear", BooleanPrimitiveType)
                .invoke(expressInfoObject, value)
        }
    val phone: String
        get() = expressInfoClass.getMethod("getPhone").invoke(expressInfoObject) as String
    var details: ArrayList<Any>?
        get() = expressInfoClass.getField("details")
            .get(expressInfoObject) as? ArrayList<Any>
        set(value) {
            expressInfoClass
                .getMethod("setDetails", java.util.ArrayList::class.java)
                .invoke(expressInfoObject, value)
        }
}

val ExpressInfoWrapper.isXiaomi: Boolean
    get() = provider == "Miguo" || provider == "MiMall"
val ExpressInfoWrapper.isJingDong: Boolean
    get() = companyCode == "JDKD"
val ExpressInfoWrapper.isXiaomiOrJingDong: Boolean
    get() = isXiaomi || isJingDong

@Parcelize
data class ExpressInfoUriWrapper(
    var link: String,
    var priority: Int
) : Comparable<ExpressInfoUriWrapper>, Parcelable {

    override fun compareTo(other: ExpressInfoUriWrapper): Int {
        return priority - other.priority
    }
}

class ExpressEntryWrapper(private val expressEntryObject: Any) {

    private val expressEntryClass = expressEntryObject.javaClass

    val companyCode: String
        get() = expressEntryClass.getField("companyCode").get(expressEntryObject) as String
    val companyName: String
        get() = expressEntryClass.getField("companyName").get(expressEntryObject) as String
    val orderNumber: String
        get() = expressEntryClass.getField("orderNumber").get(expressEntryObject) as String
    val phone: String?
        get() = expressEntryClass.getField("phone").get(expressEntryObject) as? String
    val uris: List<*>?
        get() = expressEntryClass.getMethod("getUris")
            .invoke(expressEntryObject) as List<*>?
    val provider: String?
        get() = expressEntryClass.getMethod("getProvider").invoke(expressEntryObject) as? String
}

val ExpressEntryWrapper.isXiaomi: Boolean
    get() = provider == "Miguo" || provider == "MiMall"
val ExpressEntryWrapper.isJingDong: Boolean
    get() = companyCode == "JDKD"
val ExpressEntryWrapper.isShunfeng: Boolean
    get() = provider == "ShunFeng"
val ExpressEntryWrapper.isJiTu: Boolean
    get() = provider == "JiTu"
val ExpressEntryWrapper.shouldUseNativeUI: Boolean
    get() = isXiaomi || isJingDong || isShunfeng || isJiTu

class ExpressInfoDetailWrapper(private val expressInfoDetailObject: Any) {

    private val expressInfoDetailClass = expressInfoDetailObject.javaClass

    var desc: String
        get() = expressInfoDetailClass.getMethod("getDesc")
            .invoke(expressInfoDetailObject) as String
        set(value) {
            expressInfoDetailClass.getMethod("setDesc", JavaStringClass)
                .invoke(expressInfoDetailObject, value)
        }
    var time: String
        get() = expressInfoDetailClass.getMethod("getTime")
            .invoke(expressInfoDetailObject) as String
        set(value) {
            expressInfoDetailClass.getMethod("setTime", JavaStringClass)
                .invoke(expressInfoDetailObject, value)
        }
}
