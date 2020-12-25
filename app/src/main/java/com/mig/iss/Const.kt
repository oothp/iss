package com.mig.iss

/**
 * Created by mig on 05/04/2017.
 */

object Const {

    const val API_SPACE_BASE = "http://api.open-notify.org"
//    const val API_SPACE_BASE = "http://getbible.net/json?"

    const val ISS_POSITION = "/iss-now.json"
    const val PEOPLE_IN_SPACE = "/astros.json"

    const val MAP_API_KEY = "AIzaSyBRloJZ8MOQhvyr-pCaCXvgnVirqf62Wik"

    // ======================================

    const val MAP_STYLE_AUBERGINE = R.raw.map_style_aubergine_json
    const val MAP_STYLE_SILVER = R.raw.map_style_silver_json
    const val MAP_STYLE_NIGHT = R.raw.map_style_night_json

    const val LOCATION_REFRESH_INTERVAL: Long = 3500
    const val INITIAL_REQUEST_DELAY: Long = 7000
}
