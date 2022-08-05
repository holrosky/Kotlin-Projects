package com.kotlin_project.calculator

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kotlin_project.calculator.dao.HistoryDAO
import com.kotlin_project.calculator.model.History

@Database(entities = [History::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun HistoryDAO(): HistoryDAO
}