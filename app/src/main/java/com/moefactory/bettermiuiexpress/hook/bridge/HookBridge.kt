package com.moefactory.bettermiuiexpress.hook.bridge

import android.content.Context
import com.moefactory.bettermiuiexpress.activity.ExpressDetailsActivity
import com.moefactory.bettermiuiexpress.model.ExpressEntryWrapper
import com.moefactory.bettermiuiexpress.model.MiuiExpress
import com.moefactory.bettermiuiexpress.model.shouldUseNativeUI
import com.moefactory.bettermiuiexpress.model.toExpressInfoUriWrapper

fun jumpToDetailsActivity(
    context: Context, expressEntryWrapper: ExpressEntryWrapper
): Boolean {
    val companyCode = expressEntryWrapper.companyCode
    val companyName = expressEntryWrapper.companyName
    val mailNumber = expressEntryWrapper.orderNumber
    val phoneNumber = expressEntryWrapper.phone
    // Check if the details will be showed in third-party apps(taobao, cainiao, etc.)
    val uris = expressEntryWrapper.uris
    if (!uris.isNullOrEmpty()) {
        // Store urls for future use such as jumping to third-party apps
        ExpressDetailsActivity.gotoDetailsActivity(
            context,
            MiuiExpress(companyCode, companyName, mailNumber, phoneNumber),
            ArrayList(uris.map { it!!.toExpressInfoUriWrapper() })
        )
        return true
    } else {
        // Details of packages from Xiaomi or JingDong will be showed in built-in app
        if (!expressEntryWrapper.shouldUseNativeUI()) {
            ExpressDetailsActivity.gotoDetailsActivity(
                context,
                MiuiExpress(companyCode, companyName, mailNumber, phoneNumber),
                null
            )
            return true
        }
    }

    return false
}