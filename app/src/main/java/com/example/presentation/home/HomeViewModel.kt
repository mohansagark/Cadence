package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.db.TaskEntity
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(private val repository: TaskRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.syncAllLocalTasksToCloud()
        }
    }

    val uiState: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }
    
    fun addTask(title: String, type: String, targetDate: Long?, scheduledTime: String?, priority: Int = 0, colorAccent: Long = 0xFF4F46E5) {
        viewModelScope.launch {
            val newTask = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                description = null,
                type = type,
                category = "General",
                colorAccent = colorAccent,
                priority = priority,
                scheduledTime = scheduledTime,
                targetDate = targetDate ?: System.currentTimeMillis(),
                activeDays = null
            )
            repository.insert(newTask)
        }
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteById(taskId)
        }
    }
}

class HomeViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
