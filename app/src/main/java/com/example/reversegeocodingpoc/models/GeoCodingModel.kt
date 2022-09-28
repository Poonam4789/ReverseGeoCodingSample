package com.example.reversegeocodingpoc.models

import androidx.annotation.Keep

@Keep
class GeoCodingModel {
    var error_message: String? = null
    var results: List<CustomA>? = null
    var status: String? = null
}