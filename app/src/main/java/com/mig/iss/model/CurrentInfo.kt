package com.mig.iss.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by mig on 05/04/2017.
 */

@Keep
class CurrentInfo {

    @SerializedName("timestamp")
    val timestamp: Long = 0

    @SerializedName("iss_position")
    val issPosition: IssPosition? = null
}
