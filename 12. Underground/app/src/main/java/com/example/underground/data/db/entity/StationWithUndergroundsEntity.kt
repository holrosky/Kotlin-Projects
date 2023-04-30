package com.example.underground.data.db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

class StationWithUndergroundsEntity (
    @Embedded val station: StationEntity,
    @Relation(
        parentColumn = "stationName",
        entityColumn = "undergroundId",
        associateBy = Junction(StationUndergroundCrossRefEntity::class)
    )
    val subways: List<UndergroundEntity>
)