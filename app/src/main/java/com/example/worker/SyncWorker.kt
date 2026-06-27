package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.db.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return Result.success()
        val firestore = FirebaseFirestore.getInstance()
        val database = AppDatabase.getDatabase(applicationContext)
        val settingsManager = com.example.data.local.prefs.SettingsManager(applicationContext)

        if (!settingsManager.isCloudSyncEnabled) {
            return Result.success()
        }

        return try {
            val localTasks = database.taskDao().getAllTasks().first()
            localTasks.forEach { task ->
                val taskMap = hashMapOf(
                    "id" to task.id,
                    "title" to task.title,
                    "description" to task.description,
                    "type" to task.type,
                    "category" to task.category,
                    "colorAccent" to task.colorAccent,
                    "priority" to task.priority,
                    "scheduledTime" to task.scheduledTime,
                    "targetDate" to task.targetDate,
                    "activeDays" to task.activeDays,
                    "isCompleted" to task.isCompleted
                )
                firestore.collection("users").document(userId).collection("tasks")
                    .document(task.id)
                    .set(taskMap, SetOptions.merge())
                    .await()
            }
            Log.d("SyncWorker", "Successfully synced tasks to cloud")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing tasks", e)
            Result.retry()
        }
    }
}
