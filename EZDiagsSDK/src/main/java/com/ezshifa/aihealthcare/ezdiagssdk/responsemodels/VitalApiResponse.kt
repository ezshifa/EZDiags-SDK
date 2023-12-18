package com.ezshifa.aihealthcare.models.vitalshistoryapiresponsemodels

import com.google.gson.annotations.SerializedName


data class VitalApiResponse (

  @SerializedName("statusCode"    ) var statusCode    : Int,
  @SerializedName("message"       ) var message       : String,
  @SerializedName("showQuestions" ) var showQuestions : Boolean,
  @SerializedName("vitals"        ) var vitals        : Vitals? = Vitals()

)