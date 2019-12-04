package com.mig.iss.viewmodel

import com.google.android.gms.maps.model.LatLng
import com.mig.iss.model.Dynamic

interface MainActivityViewModel {

    val items: Dynamic<List<ItemDataViewModel>>
    val coordinates: Dynamic<LatLng>

    val showPeople: Dynamic<Boolean>
    val progress: Dynamic<Boolean>

    fun refreshCurrentIssLocation()
    fun getIssPeople()
}
