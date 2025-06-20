package com.moefactory.bettermiuiexpress.repository

import com.moefactory.bettermiuiexpress.base.NetworkBoundResource

object ExpressRepository {

    fun queryCompanyFromKuaiDi100(mailNumber: String) = NetworkBoundResource(
        fetch = { ExpressActualRepository.queryCompanyActual(mailNumber) }
    )

    fun queryExpressDetailsFromKuaiDi100(companyCode: String, mailNumber: String, phoneNumber: String?, trackId: String) = NetworkBoundResource(
        fetch = {
            ExpressActualRepository.queryExpressDetailsFromKuaiDi100Actual(companyCode, mailNumber, phoneNumber, trackId)
        }
    )
}