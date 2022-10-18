package com.example.finedust.model.airInfo

import com.google.gson.annotations.SerializedName

data class Body(
    @SerializedName("items")
    val airInfos: List<AirInfo?>?,
    val numOfRows: Int?,
    val pageNo: Int?,
    val totalCount: Int?
)