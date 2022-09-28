package com.example.reversegeocodingpoc.apis

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiAdapter {
    /** By using google maps api
     * //https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=xyz
     * By using places api
     * http://places.navicmaps.com/places/api?q=india&lon=10&lat=52
     */
    var geoBaseUrl = "https://maps.googleapis.com"
    var placesBaseUrl = "https://places.navicmaps.com"

    fun getAPIClient(baseUrl: String): ApiClient {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiClient::class.java)
    }
}