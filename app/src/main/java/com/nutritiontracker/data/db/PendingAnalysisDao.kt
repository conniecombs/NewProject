package com.nutritiontracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nutritiontracker.data.model.PendingAnalysis
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingAnalysisDao {

    @Insert
    suspend fun insert(analysis: PendingAnalysis): Long

    @Update
    suspend fun update(analysis: PendingAnalysis)

    @Delete
    suspend fun delete(analysis: PendingAnalysis)

    @Query("SELECT * FROM pending_analyses WHERE status = 'pending' ORDER BY timestamp ASC")
    suspend fun getPendingAnalyses(): List<PendingAnalysis>

    @Query("SELECT * FROM pending_analyses ORDER BY timestamp DESC")
    fun getAllPending(): Flow<List<PendingAnalysis>>

    @Query("SELECT COUNT(*) FROM pending_analyses WHERE status = 'pending'")
    fun getPendingCount(): Flow<Int>

    @Query("DELETE FROM pending_analyses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE pending_analyses SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
