package com.example.underground.data.repository

import com.example.underground.domain.Station
import kotlinx.coroutines.flow.Flow

interface StationRepository {

    val stations: Flow<List<Station>>

    suspend fun refreshStations()
}