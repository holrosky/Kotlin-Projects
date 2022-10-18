package com.example.finedust.model.monitoringStation

import com.example.finedust.model.airInfo.AirInfo
import com.google.gson.annotations.SerializedName

data class Body(
    @SerializedName("items")
    val monitoringStations: List<MonitoringStation?>?,
    val numOfRows: Int?,
    val pageNo: Int?,
    val totalCount: Int?
)