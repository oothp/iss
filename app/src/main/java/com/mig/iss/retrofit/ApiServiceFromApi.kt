package com.mig.iss.retrofit

import android.util.Log
import com.google.gson.Gson
import com.mig.iss.Const
import com.mig.iss.model.IssData
import com.mig.iss.model.Dynamic
import com.mig.iss.model.PassTimeData
import com.mig.iss.model.People
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiServiceFromApi : ApiService2 {

    override val people = Dynamic<People?>(null)
    override val issData = Dynamic<IssData?>(null)
    override val issPassTimeData = Dynamic<PassTimeData?>(null)

    override fun getIssData() {

        RetrofitClient.getClient(Const.API_SPACE_BASE)
                .create(ApiService::class.java)
                .issData.enqueue(object : Callback<IssData> {

                    override fun onResponse(call: Call<IssData>, response: Response<IssData>) {
//                      Log.e(TAG, "[RESPONSE issData]: ${Gson().toJson(response.body())}")
                        issData.value = response.body()
                    }

                    override fun onFailure(call: Call<IssData>, t: Throwable) {
                        t.printStackTrace()
                        Log.e(TAG, "[Response FAIL]: $t")
                    }
                })
    }

    override fun getPeopleOnISS() {
        RetrofitClient.getClient(Const.API_SPACE_BASE)
                .create(ApiService::class.java)
                .people.enqueue(object : Callback<People> {

                    override fun onResponse(call: Call<People>, response: Response<People>) {
                        Log.e(TAG, "[RESPONSE peopleOnISS]: ${Gson().toJson(response.body())}")
                        people.value = response.body()
                    }

                    override fun onFailure(call: Call<People>, t: Throwable) {
                        t.printStackTrace()
                        Log.e(TAG, "[Response FAIL]: $t")
                    }
                })
    }

    override fun getPassTimes(lat: Double, lon: Double, alt: Long?, n: Int?) {
        RetrofitClient.getClient(Const.API_SPACE_BASE)
                .create(ApiService::class.java)
                .getPassTimes(lat, lon, alt, n).enqueue(object : Callback<PassTimeData> {

                    override fun onResponse(call: Call<PassTimeData>, response: Response<PassTimeData>) {
                        Log.e(TAG, "[RESPONSE getPassTimes]: ${Gson().toJson(response.body())}")
                        issPassTimeData.value = response.body()
                    }

                    override fun onFailure(call: Call<PassTimeData>, t: Throwable) {
                        t.printStackTrace()
                        Log.e(TAG, "[Response getPassTimes FAIL]: $t")
                    }
                })
    }

    companion object {
        private val TAG = ApiServiceFromApi::class.java.simpleName + " ==>>"
    }
}
