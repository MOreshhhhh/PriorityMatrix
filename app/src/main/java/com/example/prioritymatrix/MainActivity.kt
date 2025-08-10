package com.example.prioritymatrix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.example.prioritymatrix.ui.theme.PriorityMatrixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "prioritymatrix-db"
        ).build()

        val viewModel = ViewModelProvider(
            this,
            TasksViewModelFactory(db.taskDao())
        )[TasksViewModel::class.java]

        setContent {
            PriorityMatrixTheme {
                val tasks = viewModel.tasks.collectAsStateWithLifecycle(initialValue = emptyList())

                PriorityMatrixScreen(
                    tasks = tasks.value,
                    onToggleComplete = { updatedTask -> viewModel.updateTask(updatedTask) },
                    onDeleteTask = { task -> viewModel.deleteTask(task) },
                    onAddTask = { newTask -> viewModel.addTaskFromEntity(newTask) }
                )
            }
        }
    }
}
