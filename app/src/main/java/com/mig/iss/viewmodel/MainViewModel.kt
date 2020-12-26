package com.mig.iss.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.mig.iss.model.Dynamic
import com.mig.iss.model.IssPosition
import com.mig.iss.model.People
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
        progressIssInfo.value = new
    }

    private var loadingPeople: Boolean by observable(false) { _, _, new ->
        progressPeople.value = new
    }
    override val progressPeople: Dynamic<Boolean> = Dynamic(false)
    override val progressIssInfo: Dynamic<Boolean> = Dynamic(false)

    // ===== binding

//    val isDebug = BuildConfig.DEBUG

    var onHandleClicked: () -> Unit = {}

    var peopleVisible: Boolean = false
//    private var viewState: PeopleViewState by observable(PeopleViewState.NONE) { _, _, new ->
//        showPeople.value = new == PeopleViewState.VISIBLE
//    }

//    private var requestInProgress: Boolean by observable(false) { _, _, new ->
//        progress.value = new
//    }

//    override val showPeople: Dynamic<Boolean> = Dynamic(false)
//    override val progress: Dynamic<Boolean> = Dynamic(false)

    override val items: Dynamic<List<ItemDataViewModel>> = Dynamic(ArrayList())
    override val coordinates: Dynamic<LatLng> = Dynamic(LatLng(0.0, 0.0))

    init {
        apiService.currentInfo.bind { info ->
            issPosition = info?.issPosition
            loadingIssInfo = false
        }

        apiService.people.bind { people ->
            peopleEntity = people
            loadingPeople = false

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
