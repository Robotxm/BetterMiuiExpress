package com.moefactory.bettermiuiexpress.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.moefactory.bettermiuiexpress.model.Credential
import com.moefactory.bettermiuiexpress.model.KuaiDi100CompanyQueryRequestParam
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import com.moefactory.bettermiuiexpress.repository.ExpressRepository

class ExpressDetailsViewModel : ViewModel() {

    private val queryExpressRequest = MutableLiveData<KuaiDi100RequestParam>()
    val queryExpressResult = queryExpressRequest.switchMap {
        ExpressRepository.queryExpress(it.companyCode, it.mailNumber)
    }

    private val queryCompanyRequest = MutableLiveData<KuaiDi100CompanyQueryRequestParam>()
    val queryCompanyResult = queryCompanyRequest.switchMap {
        ExpressRepository.queryCompany(it.mailNumber)
    }

    fun queryExpressDetails(secretKey: String, mailNumber: String) {
        queryExpressRequest.value = KuaiDi100RequestParam(secretKey, mailNumber)
    }

    fun queryCompany(secretKey: String, mailNumber: String) {
        queryCompanyRequest.value = KuaiDi100CompanyQueryRequestParam(secretKey, mailNumber)
    }
}