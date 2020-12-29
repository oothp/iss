package com.mig.iss.model

import com.google.gson.annotations.SerializedName

class PassTimeData {

    @SerializedName("response")
    val passTimes: List<PassData> = arrayListOf()

    class PassData(var risetime: Long, var duration: Long)
}