package com.moefactory.bettermiuiexpress.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moefactory.bettermiuiexpress.ktx.toLiveData
import com.moefactory.bettermiuiexpress.model.ExpressDetails
import com.moefactory.bettermiuiexpress.model.KuaiDi100Company
import com.moefactory.bettermiuiexpress.model.KuaiDi100ExpressState
import com.moefactory.bettermiuiexpress.model.toExpressTrace
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import com.moefactory.bettermiuiexpress.repository.ExpressRepository
import com.moefactory.bettermiuiexpress.utils.ExpressCompanyUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ExpressDetailsViewModel : ViewModel() {

    private val _expressDetails = MutableLiveData<Result<ExpressDetails>>()
    val expressDetails = _expressDetails.toLiveData()

    private val _kuaiDi100CompanyInfo = MutableLiveData<KuaiDi100Company>()
    val kuaiDi100CompanyInfo = _kuaiDi100CompanyInfo.toLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun queryExpressDetails(
        mailNumber: String,
        companyCode: String?,
        phoneNumber: String?,
        trackId: String?,
        onSaveTrackId: (String) -> Unit,
    ) {
        suspend fun doRequest(trackId: String) {
            if (!companyCode.isNullOrEmpty()) {
                val convertedCompanyCode = ExpressCompanyUtils.convertCode(companyCode)
                val flow = if (convertedCompanyCode != null) {
                    queryFromKuaiDi100(mailNumber, convertedCompanyCode, phoneNumber, trackId)
                } else {
                    ExpressRepository.queryCompanyFromKuaiDi100(mailNumber)
                        .flatMapConcat {
                            val response = it.getOrNull()!!
                            queryFromKuaiDi100(mailNumber, response[0].companyCode, phoneNumber, trackId)
                        }
                }
                flow.catch { _expressDetails.value = Result.failure(it) }
                    .collect { _expressDetails.value = it }
            }
        }

        viewModelScope.launch {
            if (trackId.isNullOrEmpty()) {
                val generatedTrackId = UUID.randomUUID().toString()
                ExpressActualRepository.registerDeviceTrackIdActual(generatedTrackId)
                onSaveTrackId(generatedTrackId)

                doRequest(generatedTrackId)
            } else {
                doRequest(trackId)
            }
        }
    }

    private fun queryFromKuaiDi100(
        mailNumber: String, companyCode: String, phoneNumber: String?, trackId: String
    ) = ExpressRepository.queryExpressDetailsFromKuaiDi100(companyCode, mailNumber, phoneNumber, trackId)
        .map {
            val result = it.getOrNull()
            if (result == null) {
                Result.failure(Exception())
            } else {
                Result.success(
                    ExpressDetails(
                        dataSource = "快递 100 ",
                        status = KuaiDi100ExpressState.entries
                            .first { s -> s.stateCode.toString() == result.lastResult?.state }.stateName,
                        traces = result.lastResult?.data?.map { d -> d.toExpressTrace() }
                            ?.sortedDescending() ?: listOf()
                    )
                )
            }
        }
}