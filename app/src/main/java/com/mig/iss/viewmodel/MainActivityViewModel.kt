package com.mig.iss.viewmodel

import com.google.android.gms.maps.model.LatLng
import com.mig.iss.model.Dynamic

interface MainActivityViewModel {

    val humansOnIss: Dynamic<List<ItemDataViewModel>>
    val coordinates: Dynamic<LatLng>

    val peopleLoaded: Dynamic<Boolean>
    val issDataLoaded: Dynamic<Boolean>

    fun refreshIssData()
    fun getIssPeople()
}
