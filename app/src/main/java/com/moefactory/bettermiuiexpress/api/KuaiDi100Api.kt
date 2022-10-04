package com.moefactory.bettermiuiexpress.api

import com.moefactory.bettermiuiexpress.model.BaseKuaiDi100Response
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface KuaiDi100Api {

    @POST("https://poll.kuaidi100.com/poll/query.do")
    @FormUrlEncoded
    suspend fun queryPackage(
        @Field("param") param: KuaiDi100RequestParam,
        @Field("customer") customer: String,
        @Field("key") secretKey: String // This field will be removed after calculating sign
    ): BaseKuaiDi100Response

    @POST("https://www.kuaidi100.com/autonumber/auto")
    @FormUrlEncoded
    suspend fun queryExpressCompany(
        @Field("key") secretKey: String,
        @Field("num") mailNumber: String
    ): String
}