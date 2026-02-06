package com.nutritiontracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_analyses")
data class PendingAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    val mealType: String,
    val provider: String,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: String = STATUS_PENDING
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_PROCESSING = "processing"
        const val STATUS_FAILED = "failed"
    }
}
