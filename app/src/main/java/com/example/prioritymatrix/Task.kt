package com.example.prioritymatrix

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: PriorityLevel
)

enum class PriorityLevel {
    LOW, MEDIUM, HIGH
}
