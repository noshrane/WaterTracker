package com.example.watertracker.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface WaterLogDao {
    @Query("SELECT lastUpdated FROM Updated")
    fun getDate(): String?

    @Query("SELECT cup FROM WaterLog WHERE uid = :cupID")
    fun getVal(cupID: Int): Boolean

    @Query("INSERT OR REPLACE INTO WaterLog (uid, cup) VALUES (:cupID, :had)")
    suspend fun updateCup(cupID: Int, had: Boolean)

    @Query("INSERT OR REPLACE INTO Updated (uid, lastUpdated) VALUES (1, :date)")
    suspend fun updateDate(date: String)

    @Query("UPDATE WaterLog SET cup = 0")
    suspend fun resetAllCups()

    @Query("SELECT COUNT(*) FROM WaterLog WHERE cup = 1")
    fun getCount(): Int
}