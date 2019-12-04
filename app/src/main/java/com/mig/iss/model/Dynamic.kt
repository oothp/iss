package com.mig.iss.model

import kotlin.properties.Delegates

class Dynamic<T>(v: T) {
    private var listener: ((T) -> Unit)? = null

    var value: T by Delegates.observable(v) { prop, old, new ->
        listener?.invoke(new)
    }

    fun bind(listener: ((T) -> Unit)?) {
        this.listener = listener
    }

    fun bindAndFire(listener: ((T) -> Unit)?) {
        this.listener = listener
        listener?.invoke(value)
    }

    fun fire() {
        listener?.invoke(value)
    }
}