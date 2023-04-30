package com.example.underground.data.db

import androidx.room.*
import com.example.underground.data.db.entity.StationEntity
import com.example.underground.data.db.entity.StationUndergroundCrossRefEntity
import com.example.underground.data.db.entity.StationWithUndergroundsEntity
import com.example.underground.data.db.entity.UndergroundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {

    @Transaction
    @Query("SELECT * FROM StationEntity")
    fun getStationWithUndergrounds(): Flow<List<StationWithUndergroundsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(station: List<StationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUndergrounds(undergrounds: List<UndergroundEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossReferences(reference: List<StationUndergroundCrossRefEntity>)
}