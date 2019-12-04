package com.mig.myapplication.retrofit

import com.mig.myapplication.model.CurrentInfo
import com.mig.myapplication.model.Dynamic
import com.mig.myapplication.model.People

interface ApiService2 {

    val people: Dynamic<People?>
    val currentInfo: Dynamic<CurrentInfo?>

    fun getCurrentLocation()
    fun getPeopleOnISS()
}
