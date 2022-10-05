package com.moefactory.bettermiuiexpress.ktx

import android.content.Intent
import android.view.View

val ViewClass get() = View::class.java
val IntentClass get() = Intent::class.java
val JavaListClass get() = java.util.List::class.java
val JavaArrayListClass get() = java.util.ArrayList::class.java
val JavaStringClass get() = java.lang.String::class.java
val BooleanPrimitiveType get() = Boolean::class.javaPrimitiveType!!
val CharSequenceClass get() = java.lang.CharSequence::class.java