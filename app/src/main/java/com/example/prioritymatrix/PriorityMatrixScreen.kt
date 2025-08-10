package com.example.prioritymatrix

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityMatrixScreen(
    tasks: List<Task>,
    onToggleComplete: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onAddTask: (Task) -> Unit
) {
    var showCompleted by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredTasks = tasks
        .filter { if (showCompleted) true else !it.isCompleted }
        .filter {
            searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }

    Box(Modifier.fillMaxSize()) {

        Column(Modifier.fillMaxSize()) {
            // ==== HEADER ====
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Priority Matrix (${filteredTasks.size})",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Switch(
                            checked = showCompleted,
                            onCheckedChange = { showCompleted = it }
                        )
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ==== QUADRANT SPLIT ====
            val q1 = filteredTasks.filter { it.isImportant && it.isUrgent }
            val q2 = filteredTasks.filter { it.isImportant && !it.isUrgent }
            val q3 = filteredTasks.filter { !it.isImportant && it.isUrgent }
            val q4 = filteredTasks.filter { !it.isImportant && !it.isUrgent }

            val cfg = LocalConfiguration.current
            val cols = if (cfg.screenWidthDp > 600) 4 else 2
            val cardHeight = (cfg.screenHeightDp.dp / (if (cols == 2) 2 else 1)) - 64.dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { QuadrantCard("Important & Urgent", q1, cardHeight, onToggleComplete, onDeleteTask, MaterialTheme.colorScheme.errorContainer) }
                item { QuadrantCard("Important & Not Urgent", q2, cardHeight, onToggleComplete, onDeleteTask, MaterialTheme.colorScheme.tertiaryContainer) }
                item { QuadrantCard("Not Important & Urgent", q3, cardHeight, onToggleComplete, onDeleteTask, MaterialTheme.colorScheme.secondaryContainer) }
                item { QuadrantCard("Not Important & Not Urgent", q4, cardHeight, onToggleComplete, onDeleteTask, MaterialTheme.colorScheme.surfaceVariant) }
            }
        }

        // ==== FAB ====
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }

        // ==== DIALOG ====
        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = onAddTask
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit
) {
    val quadrants = listOf(
        "Important & Urgent",
        "Important & Not Urgent",
        "Not Important & Urgent",
        "Not Important & Not Urgent"
    )

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var quad by remember { mutableStateOf(quadrants[0]) }
    var dueMillis by remember { mutableStateOf<Long?>(null) }
    var dueText by remember { mutableStateOf("Select due date & time") }

    val ctx = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = quad,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Quadrant") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        quadrants.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = { quad = it; expanded = false }
                            )
                        }
                    }
                }

                OutlinedButton(onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        ctx,
                        { _, y, m, d ->
                            cal.set(Calendar.YEAR, y)
                            cal.set(Calendar.MONTH, m)
                            cal.set(Calendar.DAY_OF_MONTH, d)
                            TimePickerDialog(
                                ctx,
                                { _, h, min ->
                                    cal.set(Calendar.HOUR_OF_DAY, h)
                                    cal.set(Calendar.MINUTE, min)
                                    dueMillis = cal.timeInMillis
                                    dueText = "%02d/%02d/%04d %02d:%02d".format(d, m + 1, y, h, min)
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(dueText)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val (imp, urg) = when (quad) {
                        "Important & Urgent" -> true to true
                        "Important & Not Urgent" -> true to false
                        "Not Important & Urgent" -> false to true
                        else -> false to false
                    }
                    onConfirm(
                        Task(
                            title = title,
                            description = desc,
                            priority = PriorityLevel.MEDIUM,
                            isImportant = imp,
                            isUrgent = urg,
                            isCompleted = false,
                            dueDateTime = dueMillis
                        )
                    )
                    onDismiss()
                }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun QuadrantCard(
    title: String,
    taskList: List<Task>,
    cardHeight: androidx.compose.ui.unit.Dp,
    onToggleComplete: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    containerColor: Color
) {
    Card(
        modifier = Modifier
            .height(cardHeight)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor)
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text("${taskList.size}", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(4.dp))
            if (taskList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(taskList) { TaskRow(it, onToggleComplete, onDeleteTask) }
                }
            }
        }
    }
}

@Composable
fun TaskRow(task: Task, onToggleComplete: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    val overdue = task.dueDateTime != null && task.dueDateTime < System.currentTimeMillis()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggleComplete(task.copy(isCompleted = !task.isCompleted)) }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = task.isCompleted, onCheckedChange = {
            onToggleComplete(task.copy(isCompleted = it))
        })
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                color = if (overdue) Color.Red else Color.Unspecified
            )
            if (task.description.isNotBlank()) {
                Text(task.description, style = MaterialTheme.typography.bodySmall)
            }
            if (task.dueDateTime != null) {
                Text(
                    "Due: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(task.dueDateTime))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (overdue) Color.Red else Color.Unspecified
                )
            }
        }
        IconButton(onClick = { onDeleteTask(task) }) {
            Icon(Icons.Default.Delete, contentDescription = null)
        }
    }
}
