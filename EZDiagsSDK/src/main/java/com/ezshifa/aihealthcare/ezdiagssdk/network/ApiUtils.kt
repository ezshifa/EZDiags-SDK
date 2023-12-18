package com.ezshifa.aihealthcare.ezdiagssdk.network

import android.content.Context

class ApiUtils{

    companion object {

        val BASE_URL = "https://dev.ezshifa.com/emr/restapi/sdk/"
        fun getAPIService(context: Context): ApiInterface {
            return ApiClient.getClient(context, BASE_URL).create(ApiInterface::class.java)
        }
    }

}