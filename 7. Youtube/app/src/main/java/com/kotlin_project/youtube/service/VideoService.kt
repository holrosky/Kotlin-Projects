package com.kotlin_project.youtube.service

import com.kotlin_project.youtube.dto.VideoDTO
import retrofit2.Call
import retrofit2.http.GET

interface VideoService {
    @GET("/v3/e8ee2d3b-cd1d-4657-a2f0-b5347e6ad444")
    fun listVideos(): Call<VideoDTO>
}