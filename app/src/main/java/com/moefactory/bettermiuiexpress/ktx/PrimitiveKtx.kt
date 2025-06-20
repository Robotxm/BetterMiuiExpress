package com.moefactory.bettermiuiexpress.ktx

import android.content.res.Resources
import android.util.TypedValue
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.math.roundToInt


val <T: Number> T.dpFloat
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

val <T: Number> T.dp
    get() = dpFloat.roundToInt()

val <T: Number> T.spFloat
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

val <T: Number> T.sp
    get() = spFloat.roundToInt()

fun UUID.toByteArray(): ByteArray = ByteBuffer.wrap(ByteArray(16))
    .putLong(mostSignificantBits)
    .putLong(leastSignificantBits)
    .array()