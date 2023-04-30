package com.example.underground.data.db.entity.mapper

import com.example.underground.data.db.entity.StationWithUndergroundsEntity
import com.example.underground.data.db.entity.UndergroundEntity
import com.example.underground.domain.Station
import com.example.underground.domain.Underground

fun StationWithUndergroundsEntity.toStation() = Station(
    name = station.stationName,
    isFavorited = station.isFavorited,
    connectedUndergrounds = subways.toSubways()
)

fun List<StationWithUndergroundsEntity>.toStations() = map { it.toStation() }

fun List<UndergroundEntity>.toSubways(): List<Underground> = map { Underground.findById(it.undergroundId) }