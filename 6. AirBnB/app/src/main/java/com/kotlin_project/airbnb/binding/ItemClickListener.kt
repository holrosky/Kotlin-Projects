package com.kotlin_project.airbnb.binding

import com.kotlin_project.airbnb.model.AccommodationModel

interface ItemClickListener {
    fun sendValue(value: AccommodationModel, position: Int)
}