package com.example.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.prefs.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(onBack: () -> Unit, settingsManager: SettingsManager) {
    var isAppLockEnabled by remember { mutableStateOf(settingsManager.isAppLockEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.DayFlowSurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.Slate100)
            ) {
                Surface(
                    onClick = { 
                        isAppLockEnabled = !isAppLockEnabled
                        settingsManager.isAppLockEnabled = isAppLockEnabled
                    },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = com.example.ui.theme.Slate500)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App Lock", style = MaterialTheme.typography.bodyLarge, color = com.example.ui.theme.Slate900)
                        Text("Require biometric authentication", style = MaterialTheme.typography.bodyMedium, color = com.example.ui.theme.Slate500)
                    }
                    Switch(
                        checked = isAppLockEnabled,
                        onCheckedChange = {
                            isAppLockEnabled = it
                            settingsManager.isAppLockEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = com.example.ui.theme.DayFlowPrimary,
                            checkedTrackColor = com.example.ui.theme.DayFlowPrimary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            }
        }
    }
}
