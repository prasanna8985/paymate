package com.project.Paymate.responsiveLayer

import com.project.Paymate.responsiveLayer.models.CommonResponse
import com.project.Paymate.responsiveLayer.responses.LoginResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {

    @FormUrlEncoded
    @POST("users.php")
    suspend fun users(
       @Field("name") name: String,
       @Field("mobile") mobile: String,
       @Field("password") password: String,
       @Field("location") location: String,
    ): Response<CommonResponse>


    @FormUrlEncoded
    @POST("exchange.php")
    fun exchange(
        @Field("requesterId") requesterId: String,
        @Field("receiverId") receiverId: String,
        @Field("amount") amount: String,
        @Field("currencyFrom") currencyFrom: String,
        @Field("currencyTo") currencyTo: String,
        @Field("status") status: String,
        @Field("timestamp") timestamp: String,
        @Field("phoneNumber") phoneNumber: String,
        @Field("note") note: String,
        @Field("meetLocation") meetLocation: String,
        @Field("meetTime") meetTime: String,
        @Field("upiId") upiId: String,
        @Field("isUpiPayment") isUpiPayment: String,
    ): Call<CommonResponse>

    @FormUrlEncoded
    @POST("getData.php")
    suspend fun login(
       @Query("condition") condition: String,
       @Field("mobile") mobile: String,
       @Field("password") password: String,
    ): Response<LoginResponse>

    @GET("getData.php")
    suspend fun getLocation(
       @Query("condition") condition: String,
    ): Response<LoginResponse>


}
