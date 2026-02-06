package com.nutritiontracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.nutritiontracker.ui.components.FoodEntryItem
import com.nutritiontracker.ui.components.NutritionCard
import com.nutritiontracker.viewmodel.TrackerViewModel

@Composable
fun DailyTrackerScreen(viewModel: TrackerViewModel) {
    val entries by viewModel.todayEntries.collectAsState()
    val summary by viewModel.todaySummary.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Today's Nutrition",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summary.date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Calorie summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${summary.totalCalories}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "calories today",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${summary.entryCount} items logged",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Macro breakdown
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionCard(
                    label = "Protein",
                    value = String.format("%.1f", summary.totalProtein),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                NutritionCard(
                    label = "Carbs",
                    value = String.format("%.1f", summary.totalCarbohydrates),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                NutritionCard(
                    label = "Fat",
                    value = String.format("%.1f", summary.totalFat),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Additional nutrients
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionCard(
                    label = "Fiber",
                    value = String.format("%.1f", summary.totalFiber),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                NutritionCard(
                    label = "Sugar",
                    value = String.format("%.1f", summary.totalSugar),
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                NutritionCard(
                    label = "Sodium",
                    value = String.format("%.0f", summary.totalSodium),
                    unit = "mg",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Entries header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Food Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (entries.isEmpty()) {
            item {
                Text(
                    text = "No entries yet today.\nScan some food to get started!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(entries) { entry ->
            FoodEntryItem(
                entry = entry,
                onDelete = { viewModel.deleteEntry(entry) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
