package com.mig.iss.retrofit

import com.mig.iss.Const
import com.mig.iss.model.IssData
import com.mig.iss.model.People
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @get:GET(Const.PEOPLE_IN_SPACE)
    val people: Call<People>

    @get:GET(Const.ISS_POSITION)
    val issData: Call<IssData>
}
