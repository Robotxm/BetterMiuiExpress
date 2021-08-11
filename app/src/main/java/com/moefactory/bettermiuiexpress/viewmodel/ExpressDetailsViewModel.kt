package com.moefactory.bettermiuiexpress.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.moefactory.bettermiuiexpress.model.Credential
import com.moefactory.bettermiuiexpress.model.KuaiDi100CompanyQueryRequestParam
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import com.moefactory.bettermiuiexpress.repository.ExpressRepository

class ExpressDetailsViewModel : ViewModel() {

    private val queryExpressRequest = MutableLiveData<Pair<String, KuaiDi100RequestParam>>()
    val queryExpressResult = queryExpressRequest.switchMap {
        ExpressRepository.queryExpress(it.first, it.second.companyCode, it.second.mailNumber)
    }

    private val queryCompanyRequest = MutableLiveData<KuaiDi100CompanyQueryRequestParam>()
    val queryCompanyResult = queryCompanyRequest.switchMap {
        ExpressRepository.queryCompany(it.secretKey, it.mailNumber)
    }

    fun queryExpressDetails(customer: String, secretKey: String, mailNumber: String) {
        queryExpressRequest.value = Pair(customer, KuaiDi100RequestParam(secretKey, mailNumber))
    }

    fun queryCompany(secretKey: String, mailNumber: String) {
        queryCompanyRequest.value = KuaiDi100CompanyQueryRequestParam(secretKey, mailNumber)
    }
}