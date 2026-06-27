package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.db.TaskEntity
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleTaskAlarm(task: TaskEntity) {
        if (task.isCompleted || task.scheduledTime == null || task.targetDate == null) return

        try {
            val timeParts = task.scheduledTime.split(":")
            if (timeParts.size != 2) return
            
            val hour = timeParts[0].toIntOrNull() ?: return
            val minute = timeParts[1].toIntOrNull() ?: return

            val calendar = Calendar.getInstance().apply {
                timeInMillis = task.targetDate
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            // Only schedule if time is in the future
            if (calendar.timeInMillis <= System.currentTimeMillis()) return

            val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
                putExtra("title", task.title)
                putExtra("description", task.description ?: "You have a task scheduled.")
                putExtra("taskId", task.id)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Requires exact alarm permission on Android 12+ (added to manifest)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Scheduled alarm for task ${task.title} at ${calendar.time}")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling alarm", e)
        }
    }

    fun cancelTaskAlarm(taskId: String) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
