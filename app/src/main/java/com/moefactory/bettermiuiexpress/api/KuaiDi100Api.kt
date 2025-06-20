package com.moefactory.bettermiuiexpress.api

import com.moefactory.bettermiuiexpress.model.KuaiDi100BaseResponse
import com.moefactory.bettermiuiexpress.model.Kuaidi100ExpressDetailsResult
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface KuaiDi100Api {

    @POST("https://p.kuaidi100.com/mobile/mobileapi.do")
    @FormUrlEncoded
    fun registerDeviceTrackId(
        @Field("method") method: String,
        @Field("json") paramString: String,
        @Field("token") token: String = "",
        @Field("hash") hash: String,
        @Field("userid") userId: Int = 0
    ): Call<KuaiDi100BaseResponse<String>>

    @POST("https://p.kuaidi100.com/mobile/mobileapi.do?method=query")
    @FormUrlEncoded
    fun queryExpressDetails(
        @Field("method") method: String,
        @Field("json") paramString: String,
        @Field("token") token: String = "",
        @Field("hash") hash: String,
        @Field("userid") userId: Int = 0
    ): Call<KuaiDi100BaseResponse<Kuaidi100ExpressDetailsResult>>

    @GET("https://www.kuaidi100.com/autonumber/auto")
    fun queryExpressCompany(
        @Query("num") mailNumber: String
    ): Call<String>
}