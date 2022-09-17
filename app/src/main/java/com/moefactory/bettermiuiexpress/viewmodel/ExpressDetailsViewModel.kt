package com.moefactory.bettermiuiexpress.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.moefactory.bettermiuiexpress.model.KuaiDi100CompanyQueryRequestParam
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import com.moefactory.bettermiuiexpress.repository.ExpressRepository

class ExpressDetailsViewModel : ViewModel() {

    private val queryExpressRequest =
        MutableLiveData<Triple<KuaiDi100RequestParam, String, String>>()
    val queryExpressResult = queryExpressRequest.switchMap {
        ExpressRepository.queryExpress(
            it.first.companyCode,
            it.first.mailNumber,
            it.second,
            it.third
        )
    }

    private val queryCompanyRequest = MutableLiveData<KuaiDi100CompanyQueryRequestParam>()
    val queryCompanyResult = queryCompanyRequest.switchMap {
        ExpressRepository.queryCompany(it.mailNumber, it.secretKey)
    }

    fun queryExpressDetails(mailNumber: String, companyCode: String, secretKey: String, customer: String) {
        queryExpressRequest.value = Triple(KuaiDi100RequestParam(mailNumber, companyCode), secretKey, customer)
    }

    fun queryCompany(mailNumber: String, secretKey: String) {
        queryCompanyRequest.value = KuaiDi100CompanyQueryRequestParam(secretKey, mailNumber)
    }
}