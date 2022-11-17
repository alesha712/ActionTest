package com.example.actiontest.api

import com.example.actiontest.models.DogResponse
import retrofit2.Call
import retrofit2.http.GET

interface DogApi {

    @GET("api/breeds/image/random/10")
    fun callApi() : Call<DogResponse>
}