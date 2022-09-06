package com.kotlin_project.airbnb.model


data class AccommodationModel(
    val id: Int,
    val title: String,
    val price: String,
    val imgUrl: String,
    val lat: Double,
    val lng: Double
)
