package com.example.underground.data.api

import com.example.underground.data.db.entity.StationEntity
import com.example.underground.data.db.entity.UndergroundEntity

interface StationApi {

    suspend fun getStationDataUpdatedTimeMillis(): Long

    suspend fun getStationUndergrounds(): List<Pair<StationEntity, UndergroundEntity>>
}