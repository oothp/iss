package com.mig.iss.retrofit

import com.mig.iss.model.IssData
import com.mig.iss.model.Dynamic
import com.mig.iss.model.People

interface ApiService2 {

    val people: Dynamic<People?>
    val issData: Dynamic<IssData?>

    fun getIssData()
    fun getPeopleOnISS()
}
