package com.mig.myapplication.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

@Keep
class People {

    @SerializedName("number")
    var number: Int = 0

    @SerializedName("people")
    var people: ArrayList<Person>? = null
}
