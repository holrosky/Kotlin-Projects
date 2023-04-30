package com.example.underground.data.repository

import com.example.underground.data.api.StationApi
import com.example.underground.data.db.StationDao
import com.example.underground.data.db.entity.StationUndergroundCrossRefEntity
import com.example.underground.data.db.entity.mapper.toStations
import com.example.underground.data.preference.PreferenceManager
import com.example.underground.domain.Station
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StationRepositoryImpl(
    private val stationApi: StationApi,
    private val stationDao: StationDao,
    private val preferenceManager: PreferenceManager,
    private val dispatcher: CoroutineDispatcher
) : StationRepository {

    override val stations: Flow<List<Station>> =
        stationDao.getStationWithUndergrounds()
            .distinctUntilChanged()
            .map { it.toStations() }
            .flowOn(dispatcher)

    override suspend fun refreshStations() = withContext(dispatcher) {
        val fileUpdatedTimeMillis = stationApi.getStationDataUpdatedTimeMillis()
        val lastDatabaseUpdatedTimeMillis = preferenceManager.getLong(KEY_LAST_DATABASE_UPDATED_TIME_MILLIS)

        if (lastDatabaseUpdatedTimeMillis == null || fileUpdatedTimeMillis > lastDatabaseUpdatedTimeMillis) {
            val stationUndergrounds = stationApi.getStationUndergrounds()
            stationDao.insertStations(stationUndergrounds.map { it.first })
            stationDao.insertUndergrounds(stationUndergrounds.map { it.second })
            stationDao.insertCrossReferences(
                stationUndergrounds.map { (station, underground) ->
                    StationUndergroundCrossRefEntity(
                        station.stationName,
                        underground.undergroundId
                    )
                }
            )
            preferenceManager.putLong(KEY_LAST_DATABASE_UPDATED_TIME_MILLIS, fileUpdatedTimeMillis)
        }
    }

    companion object {
        private const val KEY_LAST_DATABASE_UPDATED_TIME_MILLIS = "KEY_LAST_DATABASE_UPDATED_TIME_MILLIS"
    }
}