package com.nutritiontracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nutritiontracker.data.model.FoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {

    @Insert
    suspend fun insert(entry: FoodEntry): Long

    @Delete
    suspend fun delete(entry: FoodEntry)

    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<FoodEntry>>

    @Query(
        """
        SELECT * FROM food_entries
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay
        ORDER BY timestamp DESC
        """
    )
    fun getEntriesForDay(startOfDay: Long, endOfDay: Long): Flow<List<FoodEntry>>

    @Query(
        """
        SELECT * FROM food_entries
        WHERE timestamp >= :startOfWeek AND timestamp < :endOfWeek
        ORDER BY timestamp DESC
        """
    )
    fun getEntriesForWeek(startOfWeek: Long, endOfWeek: Long): Flow<List<FoodEntry>>

    @Query(
        """
        SELECT
            SUM(calories) as totalCalories,
            SUM(protein) as totalProtein,
            SUM(carbohydrates) as totalCarbs,
            SUM(fat) as totalFat,
            SUM(fiber) as totalFiber,
            SUM(sugar) as totalSugar,
            SUM(sodium) as totalSodium,
            COUNT(*) as entryCount
        FROM food_entries
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay
        """
    )
    suspend fun getDaySummary(startOfDay: Long, endOfDay: Long): DaySummaryResult?

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class DaySummaryResult(
    val totalCalories: Int?,
    val totalProtein: Double?,
    val totalCarbs: Double?,
    val totalFat: Double?,
    val totalFiber: Double?,
    val totalSugar: Double?,
    val totalSodium: Double?,
    val entryCount: Int?
)
