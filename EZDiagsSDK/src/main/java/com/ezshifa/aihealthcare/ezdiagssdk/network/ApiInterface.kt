package com.ezshifa.aihealthcare.ezdiagssdk.network

import com.ezshifa.aihealthcare.ezdiagssdk.responsemodels.UploadVideoResponse
import com.ezshifa.aihealthcare.models.vitalshistoryapiresponsemodels.VitalApiResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @POST("RestController_sdk.php?view=upload_ai_video")
    @Multipart
    fun UploadVideo(
        @Part fileName: MultipartBody.Part,
        @Query("userid") username: String,
        @Query("userkey") userKey: String

    ): Call<UploadVideoResponse>


    @POST("RestController_sdk.php?view=process_ai")
    @FormUrlEncoded
    fun getVitalsResults(
        @Field("fileName") fileName: String,
        @Field("vid") vid: Int,
        @Field("userid") username: String,
        @Field("userkey") userKey: String
    ): Call<VitalApiResponse>


}
