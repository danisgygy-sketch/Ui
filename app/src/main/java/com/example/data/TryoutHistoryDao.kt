package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TryoutHistoryDao {
    @Query("SELECT * FROM tryout_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TryoutHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: TryoutHistoryItem)

    @Query("DELETE FROM tryout_history")
    suspend fun clearAllHistory()
}
