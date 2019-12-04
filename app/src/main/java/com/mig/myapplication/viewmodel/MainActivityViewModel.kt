package com.mig.myapplication.viewmodel

import com.google.android.gms.maps.model.LatLng
import com.mig.myapplication.model.Dynamic

interface MainActivityViewModel {

    val items: Dynamic<List<ItemDataViewModel>>
    val coordinates: Dynamic<LatLng>

    val showPeople: Dynamic<Boolean>
    val progress: Dynamic<Boolean>

    fun refreshCurrentIssLocation()
    fun getIssPeople()
}
