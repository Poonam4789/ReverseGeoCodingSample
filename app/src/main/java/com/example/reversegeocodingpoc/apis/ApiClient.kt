package com.example.reversegeocodingpoc.apis

import com.example.reversegeocodingpoc.models.GeoCodingModel
import com.example.reversegeocodingpoc.models.PlacesApiModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiClient {
    @GET("/maps/api/geocode/json?")
    suspend fun getReverseGeoCodingData(
        @Query("latlng") latLng: String,
        @Query("key") key: String
    ): Response<GeoCodingModel>

    @GET("/places/api?q=india")
    suspend fun getReverseGeoCodingFromPlaces(
        @Query("lon") lng: String,
        @Query("lat") lat: String
    ): Response<PlacesApiModel>
}