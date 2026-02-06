package com.nutritiontracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nutritiontracker.data.db.AppDatabase
import com.nutritiontracker.data.model.DailySummary
import com.nutritiontracker.data.model.FoodEntry
import com.nutritiontracker.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    private val _todayEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val todayEntries: StateFlow<List<FoodEntry>> = _todayEntries.asStateFlow()

    private val _weekEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val weekEntries: StateFlow<List<FoodEntry>> = _weekEntries.asStateFlow()

    private val _todaySummary = MutableStateFlow(emptySummary("Today"))
    val todaySummary: StateFlow<DailySummary> = _todaySummary.asStateFlow()

    private val _weekDailySummaries = MutableStateFlow<List<DailySummary>>(emptyList())
    val weekDailySummaries: StateFlow<List<DailySummary>> = _weekDailySummaries.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).foodEntryDao()
        repository = FoodRepository(dao)
        loadTodayData()
        loadWeekData()
    }

    fun loadTodayData() {
        viewModelScope.launch {
            repository.getEntriesForToday().collect { entries ->
                _todayEntries.value = entries
            }
        }
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val summary = repository.getDailySummary(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            _todaySummary.value = summary
        }
    }

    fun loadWeekData() {
        viewModelScope.launch {
            repository.getEntriesForCurrentWeek().collect { entries ->
                _weekEntries.value = entries
            }
        }
        viewModelScope.launch {
            val summaries = mutableListOf<DailySummary>()
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)

            for (i in 0 until 7) {
                val summary = repository.getDailySummary(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                summaries.add(summary)
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            _weekDailySummaries.value = summaries
        }
    }

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            loadTodayData()
            loadWeekData()
        }
    }

    fun refresh() {
        loadTodayData()
        loadWeekData()
    }

    private fun emptySummary(date: String) = DailySummary(
        date = date,
        totalCalories = 0,
        totalProtein = 0.0,
        totalCarbohydrates = 0.0,
        totalFat = 0.0,
        totalFiber = 0.0,
        totalSugar = 0.0,
        totalSodium = 0.0,
        entryCount = 0
    )
}
