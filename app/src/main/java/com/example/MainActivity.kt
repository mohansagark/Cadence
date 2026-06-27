package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.db.AppDatabase
import com.example.data.repository.TaskRepository
import com.example.presentation.MainScreen
import com.example.presentation.home.HomeViewModel
import com.example.presentation.home.HomeViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.worker.SyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        "SyncWork",
        ExistingPeriodicWorkPolicy.KEEP,
        syncRequest
    )
    
    val database = AppDatabase.getDatabase(applicationContext)
    val alarmScheduler = com.example.receiver.AlarmScheduler(applicationContext)
    val settingsManager = com.example.data.local.prefs.SettingsManager(applicationContext)
    val repository = TaskRepository(database.taskDao(), alarmScheduler, settingsManager)
    val factory = HomeViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

    setContent {
      MyApplicationTheme {
        com.example.presentation.RootNavGraph(homeViewModel = viewModel, settingsManager = settingsManager)
      }
    }
  }
}
