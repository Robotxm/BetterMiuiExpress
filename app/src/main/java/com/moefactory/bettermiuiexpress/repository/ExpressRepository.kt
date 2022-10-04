package com.moefactory.bettermiuiexpress.repository

import com.moefactory.bettermiuiexpress.base.NetworkBoundResource

object ExpressRepository {

    /**** KuaiDi100 Begin ****/

    fun queryCompanyFromKuaiDi100(mailNumber: String, secretKey: String) = NetworkBoundResource(
        fetch = { ExpressActualRepository.queryCompanyActual(mailNumber, secretKey) }
    )

    fun queryExpressDetailsFromKuaiDi100(
        companyCode: String,
        mailNumber: String,
        phoneNumber: String?,
        secretKey: String,
        customer: String
    ) = NetworkBoundResource(
        fetch = {
            ExpressActualRepository.queryExpressDetailsFromKuaiDi100Actual(
                companyCode, mailNumber, phoneNumber, secretKey, customer
            )
        }
    )

    /***** KuaiDi100 End *****/

    /***** CaiNiao Begin *****/

    fun queryExpressDetailsFromCaiNiao(mailNumber: String) = NetworkBoundResource(
        fetch = { ExpressActualRepository.queryExpressDetailsFromCaiNiaoActual(mailNumber) }
    )

    /***** CaiNiao End *****/
}