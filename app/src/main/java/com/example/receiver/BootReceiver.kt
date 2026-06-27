package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmScheduler = AlarmScheduler(context)
            val database = AppDatabase.getDatabase(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val tasks = database.taskDao().getAllTasks().first()
                    tasks.forEach { task ->
                        if (!task.isCompleted && task.scheduledTime != null) {
                            alarmScheduler.scheduleTaskAlarm(task)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
