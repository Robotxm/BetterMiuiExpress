package com.moefactory.bettermiuiexpress.api

import com.moefactory.bettermiuiexpress.model.BaseKuaiDi100Response
import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import com.moefactory.bettermiuiexpress.model.NewKuaiDi100BaseResponse
import com.moefactory.bettermiuiexpress.model.NewKuaiDi100RequestParam
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

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

    @POST("https://p.kuaidi100.com/mobile/mobileapi.do?method=query")
    @Headers("User-Agent: Dalvik/2.1.0 (Linux; U; Android 5.1.1; PCT-AL10 Build/LYZ28N)")
    @FormUrlEncoded
    suspend fun queryPackageNew(
        @Field("method") method: String = "query",
        @Field("json") paramString: String,
        @Field("token") token: String = "",
        @Field("hash") hash: String,
        @Field("userid") userId: Int = 0
    ): NewKuaiDi100BaseResponse

    @GET("https://www.kuaidi100.com/autonumber/auto")
    @Headers("User-Agent: Dalvik/2.1.0 (Linux; U; Android 5.1.1; PCT-AL10 Build/LYZ28N)")
    suspend fun queryExpressCompanyNew(
        @Query("num") mailNumber: String
    ): String
}