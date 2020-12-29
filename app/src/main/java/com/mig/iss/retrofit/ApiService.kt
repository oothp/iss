package com.mig.iss.retrofit

import com.mig.iss.Const
import com.mig.iss.model.IssData
import com.mig.iss.model.PassTimeData
import com.mig.iss.model.People
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    companion object {
        const val PARAM_LAT = "lat"
        const val PARAM_LON = "lon"
        const val PARAM_ALT = "alt"
        const val PARAM_N = "n"
    }

    @get:GET(Const.PEOPLE_IN_SPACE)
    val people: Call<People>

    @get:GET(Const.ISS_POSITION)
    val issData: Call<IssData>

    @GET(Const.ISS_PASS)
    fun getPassTimes(@Query(PARAM_LAT) lat: Double,
                     @Query(PARAM_LON) lon: Double,
                     @Query(PARAM_ALT) alt: Long? = null,
                     @Query(PARAM_N) n: Int? = null): Call<PassTimeData>
}
