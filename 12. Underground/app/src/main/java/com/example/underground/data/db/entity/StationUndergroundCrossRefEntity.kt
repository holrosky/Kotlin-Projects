package com.example.underground.data.db.entity

import androidx.room.Entity

@Entity(primaryKeys = ["stationName", "undergroundId"])
class StationUndergroundCrossRefEntity(
    val stationName: String,
    val undergroundId: Int
)