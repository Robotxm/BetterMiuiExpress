package com.moefactory.bettermiuiexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moefactory.bettermiuiexpress.model.ExpressDetails
import com.moefactory.bettermiuiexpress.model.KuaiDi100ExpressState
import com.moefactory.bettermiuiexpress.model.toExpressTrace
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import com.moefactory.bettermiuiexpress.repository.ExpressRepository
import com.moefactory.bettermiuiexpress.utils.ExpressCompanyUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface ExpressDetailsUiState {
    data object Loading : ExpressDetailsUiState
    data object Empty : ExpressDetailsUiState
    data class Content(val details: ExpressDetails) : ExpressDetailsUiState
    data class Error(val cause: Throwable) : ExpressDetailsUiState
}

class ExpressDetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ExpressDetailsUiState>(ExpressDetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var queryJob: Job? = null

    fun queryExpressDetails(
        mailNumber: String,
        companyCode: String?,
        phoneNumber: String?,
        trackId: String?,
        refresh: Boolean = false,
        onSaveTrackId: (String) -> Unit,
    ) {
        // A retained ViewModel keeps its request and result across activity recreation.
        if (queryJob != null && !refresh) return
        queryJob?.cancel()
        _uiState.value = ExpressDetailsUiState.Loading
        queryJob = viewModelScope.launch {
            try {
                val effectiveTrackId = trackId?.takeIf { it.isNotEmpty() } ?: run {
                    val generatedTrackId = UUID.randomUUID().toString()
                    check(ExpressActualRepository.registerDeviceTrackIdActual(generatedTrackId))
                    onSaveTrackId(generatedTrackId)
                    generatedTrackId
                }
                val convertedCode = companyCode?.takeIf { it.isNotEmpty() }
                    ?.let(ExpressCompanyUtils::convertCode)
                val resolvedCode = convertedCode ?: ExpressRepository.queryCompanyFromKuaiDi100(mailNumber)
                    .first().getOrThrow().firstOrNull()?.companyCode
                if (resolvedCode.isNullOrEmpty()) {
                    _uiState.value = ExpressDetailsUiState.Empty
                    return@launch
                }
                val result = ExpressRepository.queryExpressDetailsFromKuaiDi100(
                    resolvedCode, mailNumber, phoneNumber, effectiveTrackId
                ).first().getOrThrow() ?: error("Empty express response")
                val traces = result.lastResult?.data?.map { it.toExpressTrace() }
                    ?.sortedDescending().orEmpty()
                _uiState.value = if (traces.isEmpty()) ExpressDetailsUiState.Empty else {
                    ExpressDetailsUiState.Content(ExpressDetails(
                        dataSource = "快递 100 ",
                        status = KuaiDi100ExpressState.entries.firstOrNull {
                            it.stateCode.toString() == result.lastResult?.state
                        }?.stateName ?: "暂无信息",
                        traces = traces
                    ))
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = ExpressDetailsUiState.Error(error)
            }
        }
    }
}
