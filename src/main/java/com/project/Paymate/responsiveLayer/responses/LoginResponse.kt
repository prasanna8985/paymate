package com.project.Paymate.responsiveLayer.responses

import com.google.gson.annotations.SerializedName
import com.project.Paymate.responsiveLayer.models.User

data class LoginResponse (
   @SerializedName("error"   ) var error   : Boolean?        = null,
   @SerializedName("message" ) var message : String?         = null,
   @SerializedName("data"    ) var data    : ArrayList<User> = arrayListOf()
)