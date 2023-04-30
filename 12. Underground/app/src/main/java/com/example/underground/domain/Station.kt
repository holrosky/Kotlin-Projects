package com.example.underground.domain

data class Station(
    val name: String,
    val isFavorited: Boolean,
    val connectedUndergrounds: List<Underground>
)