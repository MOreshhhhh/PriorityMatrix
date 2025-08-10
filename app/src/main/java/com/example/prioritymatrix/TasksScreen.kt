package com.example.prioritymatrix

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun TasksScreen(viewModel: TasksViewModel) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(PriorityLevel.LOW) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Title field
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Priority dropdown
        PriorityDropdown(selectedPriority) { selectedPriority = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Task button
        Button(
            onClick = {
                if (title.isNotBlank()) {
                    val newTask = Task(
                        title = title,
                        description = description,
                        priority = selectedPriority,
                        isImportant = selectedPriority != PriorityLevel.LOW, // Example mapping
                        isUrgent = selectedPriority == PriorityLevel.HIGH,  // Example mapping
                        isCompleted = false
                    )
                    viewModel.addTaskFromEntity(newTask)

                    // Reset fields
                    title = ""
                    description = ""
                    selectedPriority = PriorityLevel.LOW
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Task list
        LazyColumn {
            items(tasks) { task ->
                TaskItem(task = task, onDelete = { viewModel.deleteTask(task) })
            }
        }
    }
}

@Composable
fun PriorityDropdown(selected: PriorityLevel, onPrioritySelected: (PriorityLevel) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Priority: ${selected.name}")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PriorityLevel.values().forEach { priority ->
                DropdownMenuItem(
                    text = { Text(priority.name) },
                    onClick = {
                        onPrioritySelected(priority)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                if (task.description.isNotEmpty()) {
                    Text(task.description, style = MaterialTheme.typography.bodyMedium)
                }
                Text("Priority: ${task.priority.name}")
            }
            Button(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}
