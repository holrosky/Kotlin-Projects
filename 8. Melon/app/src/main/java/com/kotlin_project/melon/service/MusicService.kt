package com.kotlin_project.melon.service

import com.kotlin_project.melon.dto.MusicDTO
import retrofit2.Call
import retrofit2.http.GET

interface MusicService {
    @GET("/v3/bdbd4d78-46cd-4f78-852c-598b0766244e")
    fun listMusics(): Call<MusicDTO>
}