package com.mig.iss.model

import com.google.gson.annotations.SerializedName

class CurrentInfo {

    @SerializedName("timestamp")
    val timestamp: Long = 0

    @SerializedName("iss_position")
    val issPosition: IssPosition? = null
}
