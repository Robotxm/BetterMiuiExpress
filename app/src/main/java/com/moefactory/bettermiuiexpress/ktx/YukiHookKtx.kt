package com.moefactory.bettermiuiexpress.ktx

import android.content.Context
import android.content.Intent
import android.view.View

val ContextClass get() = Context::class.java
val ViewClass get() = View::class.java
val IntentClass get() = Intent::class.java
val JavaListClass get() = java.util.List::class.java
val JavaStringClass get() = java.lang.String::class.java
val BooleanPrimitiveType get() = Boolean::class.javaPrimitiveType
val ObjectClass get() = java.lang.Object::class.java