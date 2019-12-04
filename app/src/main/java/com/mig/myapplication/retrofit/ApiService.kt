package com.mig.myapplication.retrofit

import com.mig.myapplication.Const
import com.mig.myapplication.model.CurrentInfo
import com.mig.myapplication.model.People
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @get:GET(Const.PEOPLE_IN_SPACE)
    val people: Call<People>

    @get:GET(Const.ISS_POSITION)
    val currentInfo: Call<CurrentInfo>
}
