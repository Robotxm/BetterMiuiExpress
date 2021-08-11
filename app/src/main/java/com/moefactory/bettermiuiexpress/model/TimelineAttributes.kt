package com.moefactory.bettermiuiexpress.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimelineAttributes(
        var markerSize: Int,
        var markerColor: Int,
        var markerInCenter: Boolean,
        var markerLeftPadding: Int,
        var markerTopPadding: Int,
        var markerRightPadding: Int,
        var markerBottomPadding: Int,
        var linePadding: Int,
        var lineWidth: Int,
        var startLineColor: Int,
        var endLineColor: Int,
        var lineStyle: Int,
        var lineDashWidth: Int,
        var lineDashGap: Int
): Parcelable