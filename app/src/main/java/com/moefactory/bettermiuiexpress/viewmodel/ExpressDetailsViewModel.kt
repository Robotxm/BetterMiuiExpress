package com.moefactory.bettermiuiexpress.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moefactory.bettermiuiexpress.ktx.toLiveData
import com.moefactory.bettermiuiexpress.model.ExpressDetails
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.KuaiDi100ExpressState
import com.moefactory.bettermiuiexpress.model.toExpressTrace
import com.moefactory.bettermiuiexpress.repository.ExpressRepository
import com.moefactory.bettermiuiexpress.utils.ExpressCompanyUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ExpressDetailsViewModel : ViewModel() {

    private val _expressDetails = MutableLiveData<Result<ExpressDetails>>()
    val expressDetails = _expressDetails.toLiveData()

    private val _kuaiDi100CompanyInfo = MutableLiveData<KuaiDi100Company>()
    val kuaiDi100CompanyInfo = _kuaiDi100CompanyInfo.toLiveData()

    @OptIn(FlowPreview::class)
    fun queryExpressDetails(
        mailNumber: String,
        companyCode: String?,
        phoneNumber: String?,
        shouldFetchFromCaiNiao: Boolean,
        secretKey: String? = null,
        customer: String? = null
    ) {
        viewModelScope.launch {
            // Fetch from CaiNiao
            if (shouldFetchFromCaiNiao || secretKey.isNullOrBlank() || customer.isNullOrBlank()
                || companyCode.isNullOrBlank() || phoneNumber?.isBlank() == true
            ) {
                queryFromCaiNiao(mailNumber)
                    .catch { _expressDetails.value = Result.failure(it) }
                    .collect { _expressDetails.value = it }

                return@launch
            }

            val convertedCompanyCode = ExpressCompanyUtils.convertCode(companyCode)
            if (convertedCompanyCode != null) {
                queryFromKuaiDi100(
                    mailNumber,
                    convertedCompanyCode,
                    phoneNumber,
                    secretKey,
                    customer
                )
            } else {
                ExpressRepository.queryCompanyFromKuaiDi100(mailNumber, secretKey)
                    .flatMapConcat {
                        val response = it.getOrNull()
                        if (response.isNullOrEmpty()) {
                            queryFromCaiNiao(mailNumber)
                        } else {
                            queryFromKuaiDi100(
                                mailNumber,
                                response[0].companyCode,
                                phoneNumber,
                                secretKey,
                                customer
                            )
                        }
                    }
            }
                .catch { _expressDetails.value = Result.failure(it) }
                .collect { _expressDetails.value = it }
        }
    }

    private fun queryFromCaiNiao(mailNumber: String) =
        ExpressRepository.queryExpressDetailsFromCaiNiao(mailNumber)
            .map {
                val result = it.getOrNull()
                if (result == null) {
                    Result.failure(Exception())
                } else {
                    Result.success(
                        ExpressDetails(
                            dataSource = "菜鸟裹裹",
                            status = result.packageStatus?.status ?: "",
                            traces = result.fullTraceDetails?.map { d -> d.toExpressTrace() }
                                ?.sortedDescending() ?: listOf()
                        )
                    )
                }
            }

    private fun queryFromKuaiDi100(
        mailNumber: String,
        companyCode: String,
        phoneNumber: String?,
        secretKey: String,
        customer: String
    ) = ExpressRepository.queryExpressDetailsFromKuaiDi100(
        companyCode, mailNumber, phoneNumber, secretKey, customer
    ).map {
        val result = it.getOrNull()
        if (result == null) {
            Result.failure(Exception())
        } else {
            Result.success(
                ExpressDetails(
                    dataSource = "快递 100 ",
                    status = KuaiDi100ExpressState.values()
                        .first { s -> s.stateCode == result.state }.stateName,
                    traces = result.data?.map { d -> d.toExpressTrace() }
                        ?.sortedDescending() ?: listOf()
                )
            )
        }
    }
}