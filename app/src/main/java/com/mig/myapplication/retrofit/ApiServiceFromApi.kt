package com.mig.myapplication.retrofit

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mig.myapplication.Const
import com.mig.myapplication.model.CurrentInfo
import com.mig.myapplication.model.Dynamic
import com.mig.myapplication.model.People
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiServiceFromApi : ApiService2 {

    override val people = Dynamic<People?>(null)
    override val currentInfo = Dynamic<CurrentInfo?>(null)

    override fun getCurrentLocation() {

        RetrofitClient.getClient(Const.API_SPACE_BASE)
            .create(ApiService::class.java)
            .currentInfo.enqueue(object : Callback<CurrentInfo> {

            override fun onResponse(call: Call<CurrentInfo>, response: Response<CurrentInfo>) {
//                Log.e(TAG, "[RESPONSE currentLocation]: ${Gson().toJson(response.body())}")
                currentInfo.value = response.body()
            }

            override fun onFailure(call: Call<CurrentInfo>, t: Throwable) {
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

    companion object {
        private val TAG = ApiServiceFromApi::class.java.simpleName
    }
}
