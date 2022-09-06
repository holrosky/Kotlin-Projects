package com.kotlin_project.airbnb.service

import com.kotlin_project.airbnb.dto.AccommodationDTO
import retrofit2.Call
import retrofit2.http.GET

interface AccommodationService {
    @GET("https://run.mocky.io/v3/e5faf13d-63ff-412c-b761-48b958370ff5")
    fun getAccommdationList(): Call<AccommodationDTO>
}