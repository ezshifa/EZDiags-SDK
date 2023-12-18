package com.ezshifa.aihealthcare.models.vitalshistoryapiresponsemodels

import com.google.gson.annotations.SerializedName


data class Vitals (

  @SerializedName("pulse"      ) var pulse      : String? = null,
  @SerializedName("temperature") var temperature : String? = null,
  @SerializedName("oxygen"     ) var oxygen     : String? = null,
  @SerializedName("sugar"      ) var sugar      : String? = null,
  @SerializedName("bp"         ) var bp         : String? = null,
  @SerializedName("bps"        ) var bps         : String? = null,
  @SerializedName("bpd"        ) var bpd         : String? = null,
  @SerializedName("new_id"     ) var new_id     : Int? = null

)