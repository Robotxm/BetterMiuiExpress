package com.moefactory.bettermiuiexpress.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moefactory.bettermiuiexpress.base.app.DATA_PROVIDER_LEGACY_KUAIDI100
import com.moefactory.bettermiuiexpress.base.app.DATA_PROVIDER_NEW_KUAIDI100
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
        dataProvider: Int,
        secretKey: String? = null,
        customer: String? = null
    ) {
        viewModelScope.launch {
            if (dataProvider == DATA_PROVIDER_NEW_KUAIDI100 && !companyCode.isNullOrEmpty()) {
                val convertedCompanyCode = ExpressCompanyUtils.convertCode(companyCode)
                val flow = if (convertedCompanyCode != null) {
                    queryFromNewKuaiDi100(mailNumber, convertedCompanyCode, phoneNumber)
                } else {
                    ExpressRepository.queryCompanyFromKuaiDi100(mailNumber, null)
                        .flatMapConcat {
                            val response = it.getOrNull()
                            if (response.isNullOrEmpty()) {
                                queryFromCaiNiao(mailNumber)
                            } else {
                                queryFromNewKuaiDi100(mailNumber, response[0].companyCode, phoneNumber)
                            }
                        }
                }
                flow.catch { _expressDetails.value = Result.failure(it) }
                    .collect { _expressDetails.value = it }
            } else if (dataProvider == DATA_PROVIDER_LEGACY_KUAIDI100 && !companyCode.isNullOrEmpty()
                && (!secretKey.isNullOrBlank() &&! customer.isNullOrBlank())) {
                val convertedCompanyCode = ExpressCompanyUtils.convertCode(companyCode)
                val flow = if (convertedCompanyCode != null) {
                    queryFromKuaiDi100(mailNumber, convertedCompanyCode, phoneNumber, secretKey, customer)
                } else {
                    ExpressRepository.queryCompanyFromKuaiDi100(mailNumber, secretKey)
                        .flatMapConcat {
                            val response = it.getOrNull()
                            if (response.isNullOrEmpty()) {
                                queryFromCaiNiao(mailNumber)
                            } else {
                                queryFromKuaiDi100(mailNumber, response[0].companyCode, phoneNumber, secretKey, customer)
                            }
                        }
                }
                flow.catch { _expressDetails.value = Result.failure(it) }
                    .collect { _expressDetails.value = it }
            } else {
                queryFromCaiNiao(mailNumber)
                    .catch { _expressDetails.value = Result.failure(it) }
                    .collect { _expressDetails.value = it }
            }
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
        mailNumber: String, companyCode: String, phoneNumber: String?, secretKey: String, customer: String
    ) = ExpressRepository.queryExpressDetailsFromKuaiDi100(companyCode, mailNumber, phoneNumber, secretKey, customer)
        .map {
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

    private fun queryFromNewKuaiDi100(
        mailNumber: String, companyCode: String, phoneNumber: String?
    ) = ExpressRepository.queryExpressDetailsFromNewKuaiDi100(companyCode, mailNumber, phoneNumber)
        .map {
            val result = it.getOrNull()
            if (result == null) {
                Result.failure(Exception())
            } else {
                Result.success(
                    ExpressDetails(
                        dataSource = "快递 100 ",
                        status = KuaiDi100ExpressState.values()
                            .first { s -> s.stateCode.toString() == result.lastResult?.state }.stateName,
                        traces = result.lastResult?.data?.map { d -> d.toExpressTrace() }
                            ?.sortedDescending() ?: listOf()
                    )
                )
            }
        }
}