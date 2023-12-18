package com.ezshifa.aihealthcare.ezdiagssdk.responsemodels

import com.google.gson.annotations.SerializedName

data class VideoData(
    @SerializedName("vid"      ) var vid      : Int?    = null,
    @SerializedName("filename" ) var filename : String? = null
)
