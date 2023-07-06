package com.moefactory.bettermiuiexpress.api

import com.moefactory.bettermiuiexpress.base.app.CAI_NIAO_APP_KEY
import com.moefactory.bettermiuiexpress.model.CaiNiaoBaseResponse
import com.moefactory.bettermiuiexpress.model.CaiNiaoExpressDetailsResponse
import com.moefactory.bettermiuiexpress.model.CaiNiaoRequestData
import com.moefactory.bettermiuiexpress.model.PlaceholderObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CaiNiaoApi {

    @GET("https://acs.m.taobao.com/h5/mtop.taobao.logisticstracedetailservice.queryalltrace/1.0/")
    fun getToken(
        @Query("appKey") appKey: String = CAI_NIAO_APP_KEY
    ): Call<CaiNiaoBaseResponse<PlaceholderObject>>

    @GET("https://acs.m.taobao.com/h5/mtop.taobao.logisticstracedetailservice.queryalltrace/1.0/")
    fun queryExpressDetails(
        @Query("data") data: CaiNiaoRequestData,
        @Query("token") token: String, // This field will be removed after calculating signature
        @Query("appKey") appKey: String = CAI_NIAO_APP_KEY,
    ): Call<CaiNiaoBaseResponse<CaiNiaoExpressDetailsResponse>>
}