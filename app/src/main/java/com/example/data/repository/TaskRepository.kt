package com.example.data.repository

import android.util.Log
import com.example.data.local.db.TaskDao
import com.example.data.local.db.TaskEntity
import com.example.receiver.AlarmScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val taskDao: TaskDao,
    private val alarmScheduler: AlarmScheduler,
    private val settingsManager: com.example.data.local.prefs.SettingsManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    init {
        // Start one-way sync listener from Firestore to local on startup
        scope.launch {
            listenToFirestore()
        }
    }

    private fun getUserId() = auth.currentUser?.uid

    private fun listenToFirestore() {
        if (!settingsManager.isCloudSyncEnabled) return
        val userId = getUserId() ?: return
        firestore.collection("users").document(userId).collection("tasks")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TaskRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }

                scope.launch {
                    snapshot?.documents?.forEach { doc ->
                        try {
                            val id = doc.getString("id") ?: return@forEach
                            val title = doc.getString("title") ?: return@forEach
                            val type = doc.getString("type") ?: "ONE_TIME"
                            val category = doc.getString("category") ?: "General"
                            val colorAccent = doc.getLong("colorAccent") ?: 0xFF4F46E5
                            val priority = doc.getLong("priority")?.toInt() ?: 0
                            val scheduledTime = doc.getString("scheduledTime")
                            val targetDate = doc.getLong("targetDate")
                            val activeDays = doc.getString("activeDays")
                            val isCompleted = doc.getBoolean("isCompleted") ?: false
                            
                            val task = TaskEntity(
                                id = id,
                                title = title,
                                description = doc.getString("description"),
                                type = type,
                                category = category,
                                colorAccent = colorAccent,
                                priority = priority,
                                scheduledTime = scheduledTime,
                                targetDate = targetDate,
                                activeDays = activeDays,
                                isCompleted = isCompleted
                            )
                            // Basic conflict resolution: just overwrite local if changed externally
                            taskDao.insertTask(task)
                            
                            if (!task.isCompleted && task.scheduledTime != null) {
                                alarmScheduler.scheduleTaskAlarm(task)
                            } else if (task.isCompleted) {
                                alarmScheduler.cancelTaskAlarm(task.id)
                            }
                        } catch (e: Exception) {
                            Log.e("TaskRepository", "Error parsing task", e)
                        }
                    }
                }
            }
    }

    suspend fun insert(task: TaskEntity) {
        taskDao.insertTask(task)
        if (!task.isCompleted && task.scheduledTime != null) {
            alarmScheduler.scheduleTaskAlarm(task)
        }
        syncToCloud(task)
    }
    
    suspend fun update(task: TaskEntity) {
        taskDao.updateTask(task)
        if (!task.isCompleted && task.scheduledTime != null) {
            alarmScheduler.scheduleTaskAlarm(task)
        } else if (task.isCompleted || task.scheduledTime == null) {
            alarmScheduler.cancelTaskAlarm(task.id)
        }
        syncToCloud(task)
    }

    suspend fun deleteById(id: String) {
        taskDao.deleteTaskById(id)
        alarmScheduler.cancelTaskAlarm(id)
        getUserId()?.let { userId ->
            try {
                firestore.collection("users").document(userId).collection("tasks").document(id).delete().await()
            } catch (e: Exception) {
                Log.e("TaskRepository", "Failed to delete task from cloud", e)
            }
        }
    }

    private fun syncToCloud(task: TaskEntity) {
        if (!settingsManager.isCloudSyncEnabled) return
        val userId = getUserId() ?: return
        scope.launch {
            try {
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
            } catch (e: Exception) {
                Log.e("TaskRepository", "Failed to sync task to cloud", e)
            }
        }
    }
    
    suspend fun syncAllLocalTasksToCloud() {
        if (!settingsManager.isCloudSyncEnabled) return
        val userId = getUserId() ?: return
        try {
            val localTasks = taskDao.getAllTasks().first()
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
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to sync all local tasks", e)
        }
    }
}
