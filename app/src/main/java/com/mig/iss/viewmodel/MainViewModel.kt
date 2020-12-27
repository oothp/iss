package com.mig.iss.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.mig.iss.model.Dynamic
import com.mig.iss.model.IssPosition
import com.mig.iss.model.People
import com.mig.iss.model.enums.PeopleViewState
import com.mig.iss.retrofit.ApiServiceFromApi
import kotlin.properties.Delegates.observable

class MainViewModel : ViewModel(), MainActivityViewModel {

    private val apiService by lazy { ApiServiceFromApi() }

    private var peopleEntity: People? by observable(null as People?) { _, _, new ->
        items.value = new?.people?.map { ItemDataViewModel(it) } ?: listOf()
    }

    private var issPosition: IssPosition? by observable(null as IssPosition?) { _, _, new ->
        coordinates.value = LatLng(
            new?.latitude?.toDouble() ?: 0.0,
            new?.longitude?.toDouble() ?: 0.0
        )
    }

    //===
    private var loadingIssInfo: Boolean by observable(false) { _, _, new ->
        issInfoLoaded.value = !new
    }

    private var loadingPeople: Boolean by observable(false) { _, _, new ->
        peopleLoaded.value = !new
    }
    override val peopleLoaded: Dynamic<Boolean> = Dynamic(false)
    override val issInfoLoaded: Dynamic<Boolean> = Dynamic(false)

    // ===== binding

    var peopleState: Dynamic<PeopleViewState> = Dynamic(PeopleViewState.NONE)

    private var viewState: PeopleViewState by observable(PeopleViewState.NONE) { _, _, new ->
//        showPeople.value = new == PeopleViewState.VISIBLE
        peopleState.value = new
    }

    override val items: Dynamic<List<ItemDataViewModel>> = Dynamic(ArrayList())
    override val coordinates: Dynamic<LatLng> = Dynamic(LatLng(0.0, 0.0))

    init {
        viewState = PeopleViewState.NONE

        apiService.currentInfo.bind { info ->
            issPosition = info?.issPosition
            loadingIssInfo = false
        }

        apiService.people.bind { people ->
            peopleEntity = people
            loadingPeople = false
            viewState = PeopleViewState.CLOSED
//            togglePeople()
        }

        refreshCurrentIssLocation()
        getIssPeople()
    }

    override fun refreshCurrentIssLocation() {
        loadingIssInfo = true
        apiService.getCurrentLocation()
    }

    override fun getIssPeople() {
        loadingPeople = true
        apiService.getPeopleOnISS()
    }

    fun togglePeople() {
//        viewState = when (viewState) {
//            PeopleViewState.NONE, PeopleViewState.HIDDEN -> PeopleViewState.VISIBLE
//            PeopleViewState.VISIBLE -> PeopleViewState.HIDDEN
//        }
    }

    //    fun togglePeople() {
//        onHandleClicked()
//    }
}
