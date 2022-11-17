package com.example.actiontest.repo

import com.example.actiontest.api.DogApi
import com.example.actiontest.models.DogResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DogRepo(private val dogApi: DogApi) : MainRepository {

    override suspend fun performCall(): DogResponse? {

        return withContext(Dispatchers.IO) {
            val response = dogApi.callApi().execute()

            if (response.code() == 200) {
                if (response.body() !== null && response.body()!!.status == "success") {
                    response.body()!!
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
}