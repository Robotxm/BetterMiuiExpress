package com.moefactory.bettermiuiexpress.ktx

import android.content.Context
import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.hook.factory.preferences
import com.moefactory.bettermiuiexpress.base.app.PREF_KEY_DEVICE_TRACK_ID

// Keep a private copy for startup before the framework service connects and for upgrades from Xposed 82.
var Context.deviceTrackId: String
    get() {
        if (YukiHook.Status.isModuleActive) {
            val sharedTrackId = preferences().getString(PREF_KEY_DEVICE_TRACK_ID)
            if (sharedTrackId.isNotEmpty()) {
                return sharedTrackId
            }
        }

        return preferences().native().getString(PREF_KEY_DEVICE_TRACK_ID)
    }
    set(value) {
        preferences().native().edit {
            putString(PREF_KEY_DEVICE_TRACK_ID, value)
        }
        if (YukiHook.Status.isModuleActive) {
            preferences().edit {
                putString(PREF_KEY_DEVICE_TRACK_ID, value)
            }
        }
    }
