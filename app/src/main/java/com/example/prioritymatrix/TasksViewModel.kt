package com.example.prioritymatrix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(private val dao: TaskDao) : ViewModel() {

    val tasks: StateFlow<List<Task>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addTask(title: String, description: String, priority: PriorityLevel) {
        if (title.isBlank()) return
        viewModelScope.launch {
            dao.insertTask(Task(title = title, description = description, priority = priority))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            dao.updateTask(task)
        }
    }
}

class TasksViewModelFactory(private val dao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
