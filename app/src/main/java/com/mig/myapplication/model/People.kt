package com.mig.myapplication.model

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class People {

    @SerializedName("number")
    var number: Int = 0

    @SerializedName("people")
    var people: ArrayList<Person>? = null
}
