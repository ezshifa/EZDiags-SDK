package com.ezshifa.aihealthcare.ezdiagssdk.responsemodels

import com.google.gson.annotations.SerializedName

data class UploadVideoResponse(
    @SerializedName("statusCode" ) var statusCode : Int?    = null,
    @SerializedName("message"    ) var message    : String? = null,
    @SerializedName("data"       ) var data       : VideoData?   = VideoData()

)