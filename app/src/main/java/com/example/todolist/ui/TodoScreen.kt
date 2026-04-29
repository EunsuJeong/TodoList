package com.example.todolist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.data.local.dateMillisOfMonthDay
import com.example.todolist.data.local.dayOfWeekOffsetOfMonthStart
import com.example.todolist.data.local.daysInMonth
import com.example.todolist.data.local.todayStartOfDayMillis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDate(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))

private fun formatMonth(millis: Long): String =
    SimpleDateFormat("yyyy년 M월", Locale.getDefault()).format(Date(millis))

private val dayLabels = listOf("일", "월", "화", "수", "목", "금", "토")

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TodoScreen(viewModel: TodoViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTodoId by remember { mutableStateOf<Long?>(null) }
    var editingTodoTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Todo List") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "전체 ${uiState.totalCount}개 · 진행중 ${uiState.activeCount}개 · 완료 ${uiState.completedCount}개",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.goToPreviousMonth() }) {
                    Text("◀")
                }
                Text(
                    text = formatMonth(uiState.visibleMonth),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { viewModel.goToNextMonth() }) {
                    Text("▶")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(onClick = { viewModel.goToToday() }) {
                    Text("오늘")
                }
            }

            MonthlyCalendar(
                visibleMonth = uiState.visibleMonth,
                selectedDate = uiState.selectedDate,
                datesWithTodos = uiState.datesWithTodos,
                onDateSelected = viewModel::setSelectedDate,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "선택 날짜 ${formatDate(uiState.selectedDate)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoFilterButton(
                    text = "전체",
                    selected = uiState.selectedFilter == TodoFilter.ALL,
                    onClick = { viewModel.setFilter(TodoFilter.ALL) }
                )
                TodoFilterButton(
                    text = "진행중",
                    selected = uiState.selectedFilter == TodoFilter.ACTIVE,
                    onClick = { viewModel.setFilter(TodoFilter.ACTIVE) }
                )
                TodoFilterButton(
                    text = "완료",
                    selected = uiState.selectedFilter == TodoFilter.COMPLETED,
                    onClick = { viewModel.setFilter(TodoFilter.COMPLETED) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoFilterButton(
                    text = "최신순",
                    selected = uiState.selectedSort == TodoSort.CREATED_DESC,
                    onClick = { viewModel.setSort(TodoSort.CREATED_DESC) }
                )
                TodoFilterButton(
                    text = "오래된순",
                    selected = uiState.selectedSort == TodoSort.CREATED_ASC,
                    onClick = { viewModel.setSort(TodoSort.CREATED_ASC) }
                )
                TodoFilterButton(
                    text = "수정순",
                    selected = uiState.selectedSort == TodoSort.UPDATED_DESC,
                    onClick = { viewModel.setSort(TodoSort.UPDATED_DESC) }
                )
            }

            Button(
                onClick = { viewModel.clearCompletedTodos() },
                enabled = uiState.completedCount > 0,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text("완료 항목 삭제")
            }

            if (uiState.todos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (uiState.selectedFilter) {
                            TodoFilter.ALL -> "이 날짜에 등록된 할 일이 없습니다."
                            TodoFilter.ACTIVE -> "이 날짜에 진행중인 할 일이 없습니다."
                            TodoFilter.COMPLETED -> "이 날짜에 완료된 할 일이 없습니다."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.todos,
                        key = { it.id }
                    ) { todo ->
                        TodoRow(
                            todo = todo,
                            onToggle = { viewModel.toggleTodo(todo) },
                            onEdit = {
                                editingTodoId = todo.id
                                editingTodoTitle = todo.title
                            },
                            onDelete = { viewModel.deleteTodo(todo) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TodoEditDialog(
            title = "할 일 추가",
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = {
                viewModel.addTodo(it)
                showAddDialog = false
            }
        )
    }

    editingTodoId?.let { todoId ->
        TodoEditDialog(
            title = "할 일 수정",
            initialValue = editingTodoTitle,
            onDismiss = {
                editingTodoId = null
                editingTodoTitle = ""
            },
            onConfirm = { newTitle ->
                if (newTitle.isNotBlank()) {
                    viewModel.updateTodoTitle(todoId = todoId, newTitle = newTitle)
                }
                editingTodoId = null
                editingTodoTitle = ""
            }
        )
    }
}

@Composable
private fun MonthlyCalendar(
    visibleMonth: Long,
    selectedDate: Long,
    datesWithTodos: Set<Long>,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = todayStartOfDayMillis()
    val offset = dayOfWeekOffsetOfMonthStart(visibleMonth)
    val totalDays = daysInMonth(visibleMonth)
    val totalCells = ((offset + totalDays + 6) / 7) * 7

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dayLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        for (weekStart in 0 until totalCells step 7) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (index in weekStart until weekStart + 7) {
                    val dayNumber = index - offset + 1
                    if (dayNumber in 1..totalDays) {
                        val dateMillis = dateMillisOfMonthDay(visibleMonth, dayNumber)
                        CalendarDayCell(
                            dayNumber = dayNumber,
                            isSelected = dateMillis == selectedDate,
                            isToday = dateMillis == today,
                            hasTodos = datesWithTodos.contains(dateMillis),
                            onClick = { onDateSelected(dateMillis) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasTodos: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val borderColor = if (isToday) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val dotColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (hasTodos) dotColor else Color.Transparent)
            )
        }
    }
}

@Composable
private fun TodoFilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick) {
            Text(text)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(text)
        }
    }
}

@Composable
private fun TodoRow(
    todo: TodoEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = todo.isCompleted,
            onCheckedChange = { onToggle() }
        )
        Text(
            text = todo.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )
        TextButton(onClick = onEdit) {
            Text("수정")
        }
        TextButton(onClick = onDelete) {
            Text("삭제")
        }
    }
}

@Composable
private fun TodoEditDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("할 일") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
