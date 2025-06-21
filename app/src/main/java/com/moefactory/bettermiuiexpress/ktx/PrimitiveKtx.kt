package com.moefactory.bettermiuiexpress.ktx

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.TypedValue
import com.moefactory.bettermiuiexpress.base.app.BME_MAIN_ACTIVITY_ALIAS
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

fun Context.hideLauncherIcon() {
    packageManager.setComponentEnabledSetting(
        ComponentName(this, BME_MAIN_ACTIVITY_ALIAS),
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
}

fun Context.isLauncherIconEnabled(): Boolean {
    val value = packageManager.getComponentEnabledSetting(ComponentName(this, BME_MAIN_ACTIVITY_ALIAS))
    return (value == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            || value == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
}