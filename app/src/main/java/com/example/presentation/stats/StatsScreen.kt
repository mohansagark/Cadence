package com.example.presentation.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.home.HomeViewModel
import com.example.data.local.db.TaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(homeViewModel: HomeViewModel) {
    val tasks by homeViewModel.uiState.collectAsStateWithLifecycle()
    
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    
    // Category Breakdown
    val categoryCounts = tasks.groupBy { it.category }.mapValues { it.value.size }
    
    val workCount = categoryCounts["Work"] ?: 0
    val healthCount = categoryCounts["Health"] ?: 0
    val studyCount = categoryCounts["Study"] ?: 0
    val otherCount = categoryCounts["General"] ?: 0 // 'General' is the default in HomeViewModel
    
    val workPercent = if (totalCount > 0) (workCount * 100) / totalCount else 0
    val healthPercent = if (totalCount > 0) (healthCount * 100) / totalCount else 0
    val studyPercent = if (totalCount > 0) (studyCount * 100) / totalCount else 0
    val otherPercent = if (totalCount > 0) (otherCount * 100) / totalCount else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress & Stats", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.DayFlowBackgroundLight
                )
            )
        },
        containerColor = com.example.ui.theme.DayFlowBackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Total Tasks",
                        value = "$totalCount",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completed",
                        value = "$completedCount",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.DayFlowSurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.Slate100)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Weekly Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.Slate900)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            val heights = listOf(0.4f, 0.8f, 1f, 0.6f, 0.9f, 0.3f, 0.5f)
                            
                            days.zip(heights).forEach { (day, height) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height((120 * height).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(com.example.ui.theme.DayFlowPrimary.copy(alpha = height))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(day, style = MaterialTheme.typography.labelSmall, color = com.example.ui.theme.Slate500)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.DayFlowSurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.Slate100)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Task Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.Slate900)
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryRow("Work", workPercent, Color(0xFF4F46E5))
                        CategoryRow("Health", healthPercent, Color(0xFFEF4444))
                        CategoryRow("Study", studyPercent, Color(0xFFF59E0B))
                        CategoryRow("General", otherPercent, Color(0xFF10B981))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRow(name: String, percentage: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(modifier = Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium, color = com.example.ui.theme.Slate700, modifier = Modifier.weight(1f))
        Text("$percentage%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.Slate900)
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.DayFlowSurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.Slate100)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = com.example.ui.theme.Slate500)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = com.example.ui.theme.DayFlowPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
