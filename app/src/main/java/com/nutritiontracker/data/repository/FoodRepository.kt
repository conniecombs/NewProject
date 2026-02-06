package com.nutritiontracker.data.repository

import com.nutritiontracker.data.db.FoodEntryDao
import com.nutritiontracker.data.model.DailySummary
import com.nutritiontracker.data.model.FoodEntry
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FoodRepository(private val foodEntryDao: FoodEntryDao) {

    fun getAllEntries(): Flow<List<FoodEntry>> = foodEntryDao.getAllEntries()

    suspend fun insert(entry: FoodEntry): Long = foodEntryDao.insert(entry)

    suspend fun delete(entry: FoodEntry) = foodEntryDao.delete(entry)

    suspend fun deleteById(id: Long) = foodEntryDao.deleteById(id)

    fun getEntriesForToday(): Flow<List<FoodEntry>> {
        val (start, end) = getDayRange(Calendar.getInstance())
        return foodEntryDao.getEntriesForDay(start, end)
    }

    fun getEntriesForDate(year: Int, month: Int, day: Int): Flow<List<FoodEntry>> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        val (start, end) = getDayRange(cal)
        return foodEntryDao.getEntriesForDay(start, end)
    }

    fun getEntriesForCurrentWeek(): Flow<List<FoodEntry>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfWeek = cal.timeInMillis

        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val endOfWeek = cal.timeInMillis

        return foodEntryDao.getEntriesForWeek(startOfWeek, endOfWeek)
    }

    suspend fun getDailySummary(year: Int, month: Int, day: Int): DailySummary {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        val (start, end) = getDayRange(cal)
        val result = foodEntryDao.getDaySummary(start, end)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return DailySummary(
            date = dateFormat.format(cal.time),
            totalCalories = result?.totalCalories ?: 0,
            totalProtein = result?.totalProtein ?: 0.0,
            totalCarbohydrates = result?.totalCarbs ?: 0.0,
            totalFat = result?.totalFat ?: 0.0,
            totalFiber = result?.totalFiber ?: 0.0,
            totalSugar = result?.totalSugar ?: 0.0,
            totalSodium = result?.totalSodium ?: 0.0,
            entryCount = result?.entryCount ?: 0
        )
    }

    private fun getDayRange(cal: Calendar): Pair<Long, Long> {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = cal.timeInMillis
        return Pair(startOfDay, endOfDay)
    }
}
