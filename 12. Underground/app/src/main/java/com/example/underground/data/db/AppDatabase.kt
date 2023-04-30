package com.example.underground.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.underground.data.db.entity.StationEntity
import com.example.underground.data.db.entity.StationUndergroundCrossRefEntity
import com.example.underground.data.db.entity.UndergroundEntity

@Database(
    entities = [StationEntity::class, UndergroundEntity::class, StationUndergroundCrossRefEntity::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun stationDao(): StationDao

    companion object {

        private const val DATABASE_NAME = "station.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
    }
}