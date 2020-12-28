package com.mig.iss.model

import com.google.gson.annotations.SerializedName

class IssData {

    @SerializedName("timestamp")
    val timestamp: Long = 0

    @SerializedName("iss_position")
    val issPosition: IssPosition = IssPosition(0f, 0f)

    var territory: String? = null
}
