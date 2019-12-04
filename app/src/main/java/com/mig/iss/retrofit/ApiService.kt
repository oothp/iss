package com.mig.iss.retrofit

import com.mig.iss.Const
import com.mig.iss.model.CurrentInfo
import com.mig.iss.model.People
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @get:GET(Const.PEOPLE_IN_SPACE)
    val people: Call<People>

    @get:GET(Const.ISS_POSITION)
    val currentInfo: Call<CurrentInfo>
}
