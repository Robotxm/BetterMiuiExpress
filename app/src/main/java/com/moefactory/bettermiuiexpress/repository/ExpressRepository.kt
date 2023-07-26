package com.moefactory.bettermiuiexpress.repository

import com.moefactory.bettermiuiexpress.base.NetworkBoundResource

object ExpressRepository {

    /**** KuaiDi100 Begin ****/

    fun queryCompanyFromKuaiDi100(mailNumber: String, secretKey: String?) = NetworkBoundResource(
        fetch = { ExpressActualRepository.queryCompanyActual(mailNumber, secretKey) }
    )

    fun queryExpressDetailsFromKuaiDi100(
        companyCode: String, mailNumber: String, phoneNumber: String?, secretKey: String, customer: String
    ) = NetworkBoundResource(
        fetch = {
            ExpressActualRepository.queryExpressDetailsFromKuaiDi100Actual(
                companyCode, mailNumber, phoneNumber, secretKey, customer
            )
        }
    )
    //
    fun queryExpressDetailsFromNewKuaiDi100(companyCode: String, mailNumber: String, phoneNumber: String?) = NetworkBoundResource(
        fetch = {
            ExpressActualRepository.queryExpressDetailsFromNewKuaiDi100Actual(companyCode, mailNumber, phoneNumber)
        }
    )

    /***** KuaiDi100 End *****/

    /***** CaiNiao Begin *****/

    fun queryExpressDetailsFromCaiNiao(mailNumber: String) = NetworkBoundResource(
        fetch = {
            val token = ExpressActualRepository.getCaiNiaoToken()
            ExpressActualRepository.queryExpressDetailsFromCaiNiaoActual(mailNumber, token)
        }
    )

    /***** CaiNiao End *****/
}