package com.moefactory.bettermiuiexpress.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.moefactory.bettermiuiexpress.base.app.BetterMiuiExpress
import com.moefactory.bettermiuiexpress.base.app.dataStore
import com.moefactory.bettermiuiexpress.base.datastore.DataStoreKeys
import com.moefactory.bettermiuiexpress.model.Credential
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore: DataStore<Preferences> =
        getApplication<BetterMiuiExpress>().dataStore

    fun getSavedCredential() = dataStore.data.map {
        Credential(
            it[DataStoreKeys.CUSTOMER] ?: "",
            it[DataStoreKeys.SECRET_KEY] ?: ""
        )
    }.asLiveData()

    fun saveCredential(credential: Credential) {
        viewModelScope.launch {
            dataStore.edit {
                it[DataStoreKeys.CUSTOMER] = credential.customer
                it[DataStoreKeys.SECRET_KEY] = credential.secretKey
            }
        }
    }
}