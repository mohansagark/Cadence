package com.example.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.db.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigateTo: (String) -> Unit = {}, onLogout: () -> Unit = {}) {
    val auth = remember { FirebaseAuth.getInstance() }
    val user = auth.currentUser
    val displayName = user?.displayName ?: "User"
    val initials = displayName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
    
    val tasks by viewModel.uiState.collectAsStateWithLifecycle()
    
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var isProfileDrawerOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                            Text(
                                text = dateFormat.format(Date()),
                                style = MaterialTheme.typography.bodySmall,
                                color = com.example.ui.theme.Slate500,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Good morning \uD83D\uDC4B",
                                style = MaterialTheme.typography.headlineSmall,
                                color = com.example.ui.theme.Slate900
                            )
                        }
                    },
                    actions = {
                        Box {
                            Surface(
                                onClick = { isProfileDrawerOpen = true },
                                shape = RoundedCornerShape(16.dp),
                                color = com.example.ui.theme.DayFlowPrimaryLight,
                                border = BorderStroke(2.dp, Color.White),
                                shadowElevation = 2.dp,
                                modifier = Modifier.padding(end = 16.dp).size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (initials.isNotEmpty()) initials else "U",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = com.example.ui.theme.DayFlowPrimary
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = com.example.ui.theme.DayFlowBackgroundLight
                    )
                )
            },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        },
        containerColor = com.example.ui.theme.DayFlowBackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProgressRingCard(progress = progress)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Today's Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.Slate900,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks yet. Enjoy your day!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
    
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, type, date, time, priority, color ->
                viewModel.addTask(title, type, date, time, priority, color)
                showAddTaskDialog = false
            }
        )
    }

    // Profile Side Drawer Overlay
    if (isProfileDrawerOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { isProfileDrawerOpen = false }
        )
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = isProfileDrawerOpen,
        enter = androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }) + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }) + androidx.compose.animation.fadeOut(),
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().fillMaxWidth(0.6f)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = com.example.ui.theme.DayFlowSurfaceLight,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 48.dp, horizontal = 16.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = com.example.ui.theme.DayFlowPrimaryLight,
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (initials.isNotEmpty()) initials else "U",
                                style = MaterialTheme.typography.headlineMedium,
                                color = com.example.ui.theme.DayFlowPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.Slate900
                        )
                        Text(
                            text = "View Profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.DayFlowPrimary,
                            modifier = Modifier.clickable { 
                                isProfileDrawerOpen = false
                                onNavigateTo("profile")
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = com.example.ui.theme.Slate100)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Menu Items
                DrawerMenuItem(
                    icon = Icons.Filled.Person,
                    text = "Profile & Settings",
                    onClick = { isProfileDrawerOpen = false; onNavigateTo("profile") }
                )
                DrawerMenuItem(
                    icon = Icons.Filled.Info,
                    text = "About App",
                    onClick = { isProfileDrawerOpen = false; onNavigateTo("about") }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = com.example.ui.theme.Slate100)
                Spacer(modifier = Modifier.height(16.dp))
                
                DrawerMenuItem(
                    icon = Icons.Filled.Logout,
                    text = "Logout",
                    onClick = { 
                        isProfileDrawerOpen = false
                        auth.signOut()
                        onLogout()
                    }
                )
            }
        }
    }
    } // End of outer Box
}

@Composable
fun DrawerMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = com.example.ui.theme.Slate500)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = com.example.ui.theme.Slate900, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProgressRingCard(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = com.example.ui.theme.Slate200,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
            )
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = com.example.ui.theme.DayFlowPrimary,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = com.example.ui.theme.DayFlowPrimary
                )
                Text(
                    text = "COMPLETED",
                    style = MaterialTheme.typography.labelSmall,
                    color = com.example.ui.theme.Slate400
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = com.example.ui.theme.DayFlowSurfaceLight,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, com.example.ui.theme.Slate100),
            shadowElevation = 2.dp
        ) {
            Text(
                text = if (progress == 1f) "\"All done!\" \uD83C\uDF89" else "\"You're crushing it!\" \uD83D\uDE80",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                color = com.example.ui.theme.Slate700,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.DayFlowSurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, com.example.ui.theme.Slate100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (task.isCompleted) com.example.ui.theme.Slate200 else Color(task.colorAccent))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) com.example.ui.theme.Slate400 else com.example.ui.theme.Slate800
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(task.category) {
                            "Work" -> Icons.Filled.Build
                            "Health" -> Icons.Filled.Favorite
                            "Study" -> Icons.Filled.DateRange
                            else -> Icons.Filled.List
                        },
                        contentDescription = "Category",
                        modifier = Modifier.size(14.dp),
                        tint = com.example.ui.theme.Slate400
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (task.type == "ROUTINE") "Routine • ${task.category}" else "One-Time • ${task.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.Slate400
                    )
                    
                    if (task.scheduledTime != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = com.example.ui.theme.Slate400)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(task.scheduledTime, style = MaterialTheme.typography.bodySmall, color = com.example.ui.theme.Slate400)
                    }
                    
                    if (task.priority > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when(task.priority) { 1 -> "!" 2 -> "!!" else -> "!!!" },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = when(task.priority) { 1 -> Color(0xFFF59E0B) 2 -> Color(0xFFF97316) else -> Color(0xFFEF4444) }
                        )
                    }
                }
            }
            
            IconButton(onClick = onToggle) {
                if (task.isCompleted) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = com.example.ui.theme.DayFlowPrimary
                    )
                } else {
                    Icon(
                        Icons.Outlined.Circle,
                        contentDescription = "Not Completed",
                        tint = com.example.ui.theme.Slate400
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, String?, Int, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var isRoutine by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var priority by remember { mutableStateOf(0) } // 0: None, 1: Low, 2: Medium, 3: High
    var colorAccent by remember { mutableStateOf(0xFF4F46E5) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
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
    
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerState.hour.toString().padStart(2, '0')
                    val m = timePickerState.minute.toString().padStart(2, '0')
                    selectedTime = "$h:$m"
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = com.example.ui.theme.DayFlowSurfaceLight,
        dragHandle = { BottomSheetDefaults.DragHandle(color = com.example.ui.theme.Slate300) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "What do you want to achieve?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.Slate900
            )
            
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Task Title", color = com.example.ui.theme.Slate400) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = com.example.ui.theme.Slate100,
                    unfocusedContainerColor = com.example.ui.theme.Slate50,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = com.example.ui.theme.Slate900,
                    unfocusedTextColor = com.example.ui.theme.Slate900
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedDate != null) com.example.ui.theme.DayFlowPrimaryLight else com.example.ui.theme.Slate50,
                    border = BorderStroke(1.dp, if (selectedDate != null) com.example.ui.theme.DayFlowPrimary.copy(alpha = 0.3f) else com.example.ui.theme.Slate200)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Filled.DateRange, 
                            contentDescription = null, 
                            tint = if (selectedDate != null) com.example.ui.theme.DayFlowPrimary else com.example.ui.theme.Slate500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedDate != null) SimpleDateFormat("MMM dd", Locale.getDefault()).format(selectedDate) else "Date",
                            color = if (selectedDate != null) com.example.ui.theme.DayFlowPrimary else com.example.ui.theme.Slate500,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Surface(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedTime != null) com.example.ui.theme.DayFlowPrimaryLight else com.example.ui.theme.Slate50,
                    border = BorderStroke(1.dp, if (selectedTime != null) com.example.ui.theme.DayFlowPrimary.copy(alpha = 0.3f) else com.example.ui.theme.Slate200)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Schedule, 
                            contentDescription = null, 
                            tint = if (selectedTime != null) com.example.ui.theme.DayFlowPrimary else com.example.ui.theme.Slate500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedTime ?: "Time",
                            color = if (selectedTime != null) com.example.ui.theme.DayFlowPrimary else com.example.ui.theme.Slate500,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Priority Selection
            Text("Priority", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = com.example.ui.theme.Slate900)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(
                    0 to "None",
                    1 to "Low",
                    2 to "Med",
                    3 to "High"
                ).forEach { (prio, label) ->
                    val isSelected = priority == prio
                    Surface(
                        onClick = { priority = prio },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) com.example.ui.theme.DayFlowPrimary else com.example.ui.theme.Slate50,
                        border = BorderStroke(1.dp, if (isSelected) com.example.ui.theme.DayFlowPrimary else com.example.ui.theme.Slate200)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                label, 
                                color = if (isSelected) Color.White else com.example.ui.theme.Slate700,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Color Selection
            Text("Color Accent", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = com.example.ui.theme.Slate900)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    0xFF4F46E5, // Indigo
                    0xFFEF4444, // Red
                    0xFFF59E0B, // Amber
                    0xFF10B981, // Emerald
                    0xFF8B5CF6  // Violet
                ).forEach { color ->
                    val isSelected = colorAccent == color
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(color))
                            .clickable { colorAccent = color }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Surface(
                onClick = { isRoutine = !isRoutine },
                shape = RoundedCornerShape(16.dp),
                color = com.example.ui.theme.Slate50,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Daily Routine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = com.example.ui.theme.Slate900)
                        Text("Repeat this task every day", style = MaterialTheme.typography.bodySmall, color = com.example.ui.theme.Slate500)
                    }
                    Switch(
                        checked = isRoutine,
                        onCheckedChange = { isRoutine = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = com.example.ui.theme.DayFlowPrimary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, if (isRoutine) "ROUTINE" else "ONE_TIME", selectedDate, selectedTime, priority, colorAccent)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.ui.theme.DayFlowPrimary,
                    disabledContainerColor = com.example.ui.theme.Slate200
                )
            ) {
                Text("Create Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
