package com.example.underground.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UndergroundEntity(
    @PrimaryKey val undergroundId: Int,
)