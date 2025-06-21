package com.moefactory.bettermiuiexpress.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimelineAttributes(
    val markerSize: Int,
    val markerColor: Int,
    val markerInCenter: Boolean,
    val markerLeftPadding: Int,
    val markerTopPadding: Int,
    val markerRightPadding: Int,
    val markerBottomPadding: Int,
    val linePadding: Int,
    val lineWidth: Int,
    val startLineColor: Int,
    val endLineColor: Int,
    val lineStyle: Int,
    val lineDashWidth: Int,
    val lineDashGap: Int
) : Parcelable