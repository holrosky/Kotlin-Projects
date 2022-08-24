package com.kotlin_project.bookreview

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kotlin_project.bookreview.dao.HistoryDao
import com.kotlin_project.bookreview.dao.ReviewDao
import com.kotlin_project.bookreview.model.History
import com.kotlin_project.bookreview.model.Review

@Database(entities = [History::class, Review::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
}