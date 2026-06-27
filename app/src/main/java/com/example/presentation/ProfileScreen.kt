package com.example.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val user = auth.currentUser
    val displayName = user?.displayName ?: "User"
    val initials = displayName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
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
        ) {
            // Header Profile Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = com.example.ui.theme.DayFlowPrimaryLight,
                    border = BorderStroke(3.dp, Color.White),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (initials.isNotEmpty()) initials else "U",
                            style = MaterialTheme.typography.displaySmall,
                            color = com.example.ui.theme.DayFlowPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.Slate900
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Menus
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.DayFlowSurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, com.example.ui.theme.Slate100)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Filled.Person,
                        title = "Profile Settings",
                        onClick = { onNavigateTo("profile_settings") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = com.example.ui.theme.Slate100)
                    ProfileMenuRow(
                        icon = Icons.Filled.DarkMode,
                        title = "Appearance",
                        onClick = { onNavigateTo("appearance_settings") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = com.example.ui.theme.Slate100)
                    ProfileMenuRow(
                        icon = Icons.Filled.Lock,
                        title = "Security",
                        onClick = { onNavigateTo("security_settings") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = com.example.ui.theme.Slate100)
                    ProfileMenuRow(
                        icon = Icons.Filled.Notifications,
                        title = "Notifications",
                        onClick = { onNavigateTo("notification_settings") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = com.example.ui.theme.Slate100)
                    ProfileMenuRow(
                        icon = Icons.Filled.Refresh,
                        title = "Data & Sync",
                        onClick = { onNavigateTo("sync_settings") }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(com.example.ui.theme.Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = com.example.ui.theme.Slate500, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = com.example.ui.theme.Slate900,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                tint = com.example.ui.theme.Slate400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
