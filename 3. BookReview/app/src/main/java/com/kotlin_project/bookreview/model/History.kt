package com.kotlin_project.bookreview.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class History(
    @PrimaryKey(autoGenerate = true) val uid: Int?,
    @ColumnInfo(name = "keyword") val keyword: String?
)