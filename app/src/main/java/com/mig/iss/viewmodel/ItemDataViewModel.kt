package com.mig.iss.viewmodel

import androidx.lifecycle.ViewModel
import com.mig.iss.model.Person

class ItemDataViewModel(person: Person) : ViewModel() {
    val name: String = person.name
    val craft: String = person.craft
}