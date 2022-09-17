package com.moefactory.bettermiuiexpress.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MiuiExpress(
    val companyCode: String,
    val companyName: String,
    val mailNumber: String,
    val phoneNumber: String?
) : Parcelable
