package com.nutritiontracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nutritiontracker.data.model.DailySummary
import com.nutritiontracker.ui.components.NutritionCard
import com.nutritiontracker.viewmodel.TrackerViewModel

@Composable
fun WeeklyTrackerScreen(viewModel: TrackerViewModel) {
    val weekEntries by viewModel.weekEntries.collectAsState()
    val dailySummaries by viewModel.weekDailySummaries.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val weekTotalCalories = dailySummaries.sumOf { it.totalCalories }
    val weekTotalProtein = dailySummaries.sumOf { it.totalProtein }
    val weekTotalCarbs = dailySummaries.sumOf { it.totalCarbohydrates }
    val weekTotalFat = dailySummaries.sumOf { it.totalFat }
    val weekTotalFiber = dailySummaries.sumOf { it.totalFiber }
    val weekTotalSugar = dailySummaries.sumOf { it.totalSugar }
    val weekAvgCalories = if (dailySummaries.isNotEmpty()) {
        weekTotalCalories / dailySummaries.count { it.entryCount > 0 }.coerceAtLeast(1)
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Weekly Overview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Weekly total card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$weekTotalCalories",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "total calories this week",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Avg: $weekAvgCalories cal/day  |  ${weekEntries.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Weekly macro totals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionCard(
                    label = "Protein",
                    value = String.format("%.1f", weekTotalProtein),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                NutritionCard(
                    label = "Carbs",
                    value = String.format("%.1f", weekTotalCarbs),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                NutritionCard(
                    label = "Fat",
                    value = String.format("%.1f", weekTotalFat),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionCard(
                    label = "Fiber",
                    value = String.format("%.1f", weekTotalFiber),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                NutritionCard(
                    label = "Sugar",
                    value = String.format("%.1f", weekTotalSugar),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Daily breakdown
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Daily Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (dailySummaries.isEmpty() || dailySummaries.all { it.entryCount == 0 }) {
            item {
                Text(
                    text = "No entries this week yet.\nStart tracking your meals!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(dailySummaries.filter { it.entryCount > 0 }) { summary ->
            DayCard(summary)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DayCard(summary: DailySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = summary.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${summary.entryCount} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${summary.totalCalories} cal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "P:${String.format("%.0f", summary.totalProtein)}g  C:${String.format("%.0f", summary.totalCarbohydrates)}g  F:${String.format("%.0f", summary.totalFat)}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
