package com.example.watertracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WaterLog(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "cup") val had: Boolean
)

@Entity
data class Updated(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "lastUpdated") val date: String
)