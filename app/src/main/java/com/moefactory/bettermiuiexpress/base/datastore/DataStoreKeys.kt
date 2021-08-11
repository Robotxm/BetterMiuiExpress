package com.moefactory.bettermiuiexpress.base.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    val SECRET_KEY = stringPreferencesKey("secret_key")
    val CUSTOMER = stringPreferencesKey("customer")
}