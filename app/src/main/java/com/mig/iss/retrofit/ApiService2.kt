package com.mig.iss.retrofit

import com.mig.iss.model.CurrentInfo
import com.mig.iss.model.Dynamic
import com.mig.iss.model.People

interface ApiService2 {

    val people: Dynamic<People?>
    val currentInfo: Dynamic<CurrentInfo?>

    fun getCurrentLocation()
    fun getPeopleOnISS()
}
