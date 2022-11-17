package com.example.actiontest.repo

import com.example.actiontest.models.DogResponse

interface MainRepository {

    suspend fun performCall() : DogResponse?
}