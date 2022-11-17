package com.example.actiontest

import com.example.actiontest.api.DogApi
import com.example.actiontest.repo.DogRepo
import com.google.gson.GsonBuilder
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val appModule = module {
    single {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        Retrofit.Builder()
            .baseUrl(IMAGES_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(DogApi::class.java)
    }
    single {
        DogRepo(get())
    }
}