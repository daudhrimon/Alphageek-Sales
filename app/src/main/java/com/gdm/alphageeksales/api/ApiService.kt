package com.gdm.alphageeksales.api

import com.gdm.alphageeksales.data.local.down_sync.DownSyncResponse
import com.gdm.alphageeksales.data.remote.BaseResponse
import com.gdm.alphageeksales.data.remote.InventoryResponse
import com.gdm.alphageeksales.data.remote.bank.BankResponse
import com.gdm.alphageeksales.data.remote.country.CountryResponse
import com.gdm.alphageeksales.data.remote.document_type.DocumentTypeResponse
import com.gdm.alphageeksales.data.remote.education.EducationResponse
import com.gdm.alphageeksales.data.remote.lga.LgaResponse
import com.gdm.alphageeksales.data.remote.login.LoginResponse
import com.gdm.alphageeksales.data.remote.profile.ProfileResponse
import com.gdm.alphageeksales.data.remote.state.StateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST(EndPoints.UPDATE_PROFILE)
    suspend fun updateProfile(
        @Part("firstname") firstname: RequestBody, @Part("lastname") lastname: RequestBody,
        @Part("middlename") middle_name: RequestBody, @Part("gender") gender: RequestBody,
        @Part("phone") phone: RequestBody, @Part("address") address: RequestBody,
        @Part("country_id") country_id: RequestBody, @Part("state_id") state_id: RequestBody,
        @Part("lga") lga: RequestBody, @Part("nin") nin: RequestBody,
        @Part("bvn") bvn: RequestBody, @Part("lassra") lassra: RequestBody,
        @Part("education") education: RequestBody, @Part("bank_id") bank_id: RequestBody,
        @Part("account_name") account_name: RequestBody, @Part("account_number") account_number: RequestBody,
        @Part("guarantor_name") guarantor_name: RequestBody, @Part("guarantor_email") guarantor_email: RequestBody,
        @Part("guarantor_phone") guarantor_phone: RequestBody, @Part("guarantor_id_type") guarantor_id_type: RequestBody,
        @Part guarantor_Document: MultipartBody.Part?,@Part userImage: MultipartBody.Part?):Response<BaseResponse>

    @FormUrlEncoded
    @POST(EndPoints.SIGN_IN)
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("device_id") device_id: String,
        @Field("login_address") login_address: String,
        @Field("app_type") appType: Int,
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST(EndPoints.LOGOUT)
    suspend fun logout(
        @Field("ip_address") ip_address: String,
        @Field("logout_address") logout_address: String
    ): Response<BaseResponse>

    @FormUrlEncoded
    @POST(EndPoints.LIVE_TRAC)
    suspend fun liveTrac(
        @Field("gio_lat") gio_lat: String,
        @Field("gio_long") gio_long: String,
        @Field("address") address: String,
        @Field("app_type") app_type: Int
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST(EndPoints.SIGN_UP)
    suspend fun signUP(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("device_id") deviceId: String,
        @Field("ip_address") ipAddress: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST(EndPoints.STATE)
    suspend fun getStateList(@Field("country_id") countryID: String): Response<StateResponse>

    @Headers( "Content-Type: application/json; charset=utf-8")
    @POST(EndPoints.UP_SYNC)
    suspend fun upSyncData(@Body body: RequestBody): Response<BaseResponse>

    @FormUrlEncoded
    @POST(EndPoints.CHECKOUT_CONFIRMATION)
    suspend fun checkOutRequest(@Field("checkout_id") request_detail_body: String): Response<InventoryResponse>

    @FormUrlEncoded
    @POST(EndPoints.INVENTORY_REQUEST)
    suspend fun inventoryRequest(@Field("request_details") request_detail_body: String): Response<InventoryResponse>

    @FormUrlEncoded
    @POST(EndPoints.LGA)
    suspend fun getLGAList(@Field("state_id") stateID: String): Response<LgaResponse>

    @GET(EndPoints.COUNTRY)
    suspend fun getCountryList(): Response<CountryResponse>

    @GET(EndPoints.BANK)
    suspend fun getBankList(): Response<BankResponse>

    @GET(EndPoints.EDUCATION)
    suspend fun getEducationList(): Response<EducationResponse>

    @GET(EndPoints.DOCUMENT_TYPE)
    suspend fun getDocumentTypeList(): Response<DocumentTypeResponse>

    @GET(EndPoints.PROFILE)
    suspend fun getProfileInfo(): Response<ProfileResponse>

    @GET(EndPoints.DOWN_SYNC)
    suspend fun downSync(): Response<DownSyncResponse>
}