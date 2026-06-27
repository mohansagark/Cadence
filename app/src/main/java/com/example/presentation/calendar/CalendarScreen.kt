package com.example.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.home.HomeViewModel
import com.example.presentation.home.TaskCard
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(homeViewModel: HomeViewModel) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    
    val tasks by homeViewModel.uiState.collectAsStateWithLifecycle()
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentMonth.timeInMillis
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newMonth = Calendar.getInstance().apply { timeInMillis = millis }
                        currentMonth = newMonth
                        selectedDate = newMonth.clone() as Calendar
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.DayFlowBackgroundLight
                )
            )
        },
        containerColor = com.example.ui.theme.DayFlowBackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        val newMonth = currentMonth.clone() as Calendar
                        newMonth.add(Calendar.MONTH, -1)
                        currentMonth = newMonth
                    },
                    modifier = Modifier.background(com.example.ui.theme.DayFlowSurfaceLight, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month", tint = com.example.ui.theme.Slate900)
                }
                
                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                TextButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = monthFormat.format(currentMonth.time),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.Slate900
                    )
                }
                
                IconButton(
                    onClick = { 
                        val newMonth = currentMonth.clone() as Calendar
                        newMonth.add(Calendar.MONTH, 1)
                        currentMonth = newMonth
                    },
                    modifier = Modifier.background(com.example.ui.theme.DayFlowSurfaceLight, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month", tint = com.example.ui.theme.Slate900)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CalendarGrid(currentMonth = currentMonth, selectedDate = selectedDate, onDateSelected = { selectedDate = it })
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            Text(dateFormat.format(selectedDate.time), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val selectedDateTasks = tasks.filter { task ->
                val taskCalendar = Calendar.getInstance().apply { timeInMillis = task.targetDate ?: 0L }
                taskCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                taskCalendar.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
            }
            
            if (selectedDateTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks scheduled for this day.", color = com.example.ui.theme.Slate500)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(selectedDateTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { homeViewModel.toggleTaskCompletion(task) },
                            onDelete = { homeViewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(currentMonth: Calendar, selectedDate: Calendar, onDateSelected: (Calendar) -> Unit) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val days = (1..daysInMonth).toList()
    
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        weekDays.forEach { day ->
            Text(day, style = MaterialTheme.typography.labelSmall, color = com.example.ui.theme.Slate500)
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        val tempCal = currentMonth.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        
        items(firstDayOfWeek) {
            Box(modifier = Modifier.aspectRatio(1f))
        }
        
        items(days) { day ->
            val isSelected = day == selectedDate.get(Calendar.DAY_OF_MONTH) && 
                             currentMonth.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) && 
                             currentMonth.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) com.example.ui.theme.DayFlowPrimary else Color.Transparent)
                    .clickable { 
                        val newDate = currentMonth.clone() as Calendar
                        newDate.set(Calendar.DAY_OF_MONTH, day)
                        onDateSelected(newDate) 
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.toString(),
                        color = if (isSelected) Color.White else com.example.ui.theme.Slate800
                    )
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White.copy(alpha = 0.7f) else com.example.ui.theme.DayFlowPrimaryLight))
                }
            }
        }
    }
}
