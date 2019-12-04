package com.mig.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.mig.myapplication.model.Person

class ItemDataViewModel(person: Person) : ViewModel() {
    val name: String = person.name
    val craft: String  = person.craft
}