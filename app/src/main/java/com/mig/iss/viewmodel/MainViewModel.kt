package com.mig.iss.viewmodel

import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.mig.iss.model.Dynamic
import com.mig.iss.model.IssData
import com.mig.iss.model.People
import com.mig.iss.model.enums.PeopleViewState
import com.mig.iss.retrofit.ApiServiceFromApi
import kotlin.properties.Delegates.observable

class MainViewModel : ViewModel(), MainActivityViewModel {

    private val apiService by lazy { ApiServiceFromApi() }

    private var peopleEntity: People? by observable(null as People?) { _, _, new ->
        humansOnIss.value = new?.people?.map { ItemDataViewModel(it) } ?: listOf()
    }

    private var issData: IssData? by observable(null as IssData?) { _, _, new ->
        coordinates.value = LatLng(
                new?.issPosition?.latitude?.toDouble() ?: 0.0,
                new?.issPosition?.longitude?.toDouble() ?: 0.0
        )
    }

    var territory: Dynamic<String?> = Dynamic(null)

    //    var country: Dynamic<String> = Dynamic("")
    var humanCount: Dynamic<String> = Dynamic("")

    //===
    private var loadingIssData: Boolean by observable(false) { _, _, new ->
        issDataLoaded.value = !new
    }

    private var loadingPeople: Boolean by observable(false) { _, _, new ->
        peopleLoaded.value = !new
    }

    override val peopleLoaded: Dynamic<Boolean> = Dynamic(false)
    override val issDataLoaded: Dynamic<Boolean> = Dynamic(false)

    // ===== binding

    var peopleState: Dynamic<PeopleViewState> = Dynamic(PeopleViewState.NONE)

    private var viewState: PeopleViewState by observable(PeopleViewState.NONE) { _, _, new ->
////        showPeople.value = new == PeopleViewState.VISIBLE
////        peopleState.value = new
    }

    override val humansOnIss: Dynamic<List<ItemDataViewModel>> = Dynamic(ArrayList())
    override val coordinates: Dynamic<LatLng> = Dynamic(LatLng(0.0, 0.0))

    init {
        viewState = PeopleViewState.NONE

        apiService.issData.bind {
            issData = it
            loadingIssData = false
        }

        apiService.people.bind { people ->
            peopleEntity = people
            loadingPeople = false
            viewState = PeopleViewState.CLOSED

            humanCount.value = people?.number.toString()
        }

        getIssPeople()
//        apiService.getPassTimes(23.89273703098297, 54.91271014509626, 30, 4) // ready
    }

    fun getUpdatedTerritory(geocoder: Geocoder) {
        issData?.let {

            val lat = it.issPosition.latitude.toDouble()
            val lon = it.issPosition.longitude.toDouble()

            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (addresses.isNotEmpty()) {

                it.territory = buildTerritoryString(addresses)

                if (territory.value != it.territory) {
                    territory.value = it.territory
                }
            } else {
                Log.e("=====>>", "TERRITORY no value")
                territory.value = "¯\\_(ツ)_/¯"
//                country.value = ""
            }
        }
    }

    private fun buildTerritoryString(addresses: List<Address>): String {

        Log.e("===>>", "${addresses[0]}")

        val addressLine = addresses[0].getAddressLine(0)

//        country.value = addresses[0].countryName ?: addressLine

        val countryName: String = addresses[0].countryName ?: ""

        return when {
            addresses[0].subAdminArea != null -> if (!addresses[0].subAdminArea.contains(countryName))
                addresses[0].subAdminArea.plus(", ").plus(countryName)
            else
                addresses[0].subAdminArea

            addresses[0].locality != null -> if (!addresses[0].locality.contains(countryName))
                addresses[0].locality.plus(", ").plus(countryName)
            else
                addresses[0].locality

            addresses[0].adminArea != null ->
                if (!addresses[0].adminArea.contains(countryName))
                    addresses[0].adminArea.plus(", ").plus(countryName)
                else
                    addresses[0].adminArea

            addressLine.length < 100 -> if (!addressLine.contains(countryName) && addressLine != countryName)
                addressLine.plus(", ").plus(countryName)
            else
                addressLine

            else -> return addresses[0].countryName
        }
    }

    override fun refreshIssData() {
        loadingIssData = true
        apiService.getIssData()
    }

    override fun getIssPeople() {
        loadingPeople = true
        apiService.getPeopleOnISS()
    }
}
