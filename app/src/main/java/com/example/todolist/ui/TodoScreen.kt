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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.data.local.dateMillisOfMonthDay
import com.example.todolist.data.local.dayOfWeekOffsetOfMonthStart
import com.example.todolist.data.local.daysInMonth
import com.example.todolist.data.local.nextDayMillis
import com.example.todolist.data.local.previousDayMillis
import com.example.todolist.data.local.todayStartOfDayMillis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDate(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))

private fun formatMonth(millis: Long): String =
    SimpleDateFormat("yyyy년 M월", Locale.getDefault()).format(Date(millis))

private val dayLabels = listOf("일", "월", "화", "수", "목", "금", "토")

private val sundayColor = Color(0xFFD32F2F)   // 부드러운 빨강
private val saturdayColor = Color(0xFF1565C0)  // 부드러운 파랑

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TodoScreen(viewModel: TodoViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTodoId by remember { mutableStateOf<Long?>(null) }
    var editingTodoTitle by remember { mutableStateOf("") }
    var editingTodoMemo by remember { mutableStateOf("") }
    var editingTodoScheduledDate by remember { mutableStateOf(todayStartOfDayMillis()) }

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
                    .padding(bottom = 8.dp),
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
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDate(uiState.selectedDate),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "전체 ${uiState.totalCount}개 · 진행중 ${uiState.activeCount}개 · 완료 ${uiState.completedCount}개",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 검색어 입력창
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("할 일 검색") },
                    placeholder = { Text("제목 검색") }
                )
                if (uiState.searchQuery.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearSearchQuery() }) {
                        Text("✕")
                    }
                }
            }

            if (uiState.searchQuery.trim().isNotEmpty()) {
                Text(
                    text = "검색 결과 ${uiState.searchResultCount}개",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

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
                        text = if (uiState.searchQuery.trim().isNotEmpty()) {
                            "검색 결과가 없습니다."
                        } else {
                            when (uiState.selectedFilter) {
                                TodoFilter.ALL -> "이 날짜에 등록된 할 일이 없습니다."
                                TodoFilter.ACTIVE -> "이 날짜에 진행중인 할 일이 없습니다."
                                TodoFilter.COMPLETED -> "이 날짜에 완료된 할 일이 없습니다."
                            }
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
                                editingTodoMemo = todo.memo ?: ""
                                editingTodoScheduledDate = todo.scheduledDate
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
            initialMemo = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { newTitle, newMemo ->
                viewModel.addTodo(newTitle, newMemo.trim().ifEmpty { null })
                showAddDialog = false
            }
        )
    }

    editingTodoId?.let { todoId ->
        TodoUpdateDialog(
            title = "할 일 수정",
            initialValue = editingTodoTitle,
            initialMemo = editingTodoMemo,
            initialScheduledDate = editingTodoScheduledDate,
            onDismiss = {
                editingTodoId = null
                editingTodoTitle = ""
                editingTodoMemo = ""
                editingTodoScheduledDate = todayStartOfDayMillis()
            },
            onConfirm = { newTitle, newScheduledDate, newMemo ->
                if (newTitle.isNotBlank()) {
                    viewModel.updateTodoDetails(
                        todoId = todoId,
                        newTitle = newTitle,
                        scheduledDate = newScheduledDate,
                        memo = newMemo.trim().ifEmpty { null }
                    )
                }
                editingTodoId = null
                editingTodoTitle = ""
                editingTodoMemo = ""
                editingTodoScheduledDate = todayStartOfDayMillis()
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
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            dayLabels.forEachIndexed { idx, label ->
                val labelColor = when (idx) {
                    0 -> sundayColor
                    6 -> saturdayColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = labelColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(6.dp))

        for (weekStart in 0 until totalCells step 7) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (index in weekStart until weekStart + 7) {
                    val dayNumber = index - offset + 1
                    val columnIndex = index % 7
                    if (dayNumber in 1..totalDays) {
                        val dateMillis = dateMillisOfMonthDay(visibleMonth, dayNumber)
                        CalendarDayCell(
                            dayNumber = dayNumber,
                            columnIndex = columnIndex,
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
    columnIndex: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasTodos: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 배경: 선택 날짜
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    // 테두리: 오늘이면서 선택되지 않은 경우만 표시
    val borderColor = if (isToday && !isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }
    // 텍스트 색상: 선택 > 주말 > 기본 순
    val baseTextColor = when (columnIndex) {
        0 -> sundayColor
        6 -> saturdayColor
        else -> MaterialTheme.colorScheme.onSurface
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        baseTextColor
    }
    // 점 색상
    val dotColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.size(2.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todo.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
            if (!todo.memo.isNullOrEmpty()) {
                Text(
                    text = todo.memo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }
        }
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
    initialMemo: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var text by remember(initialValue) { mutableStateOf(initialValue) }
    var memo by remember(initialMemo) { mutableStateOf(initialMemo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("할 일") }
                )
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("메모 (선택)") },
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text, memo) },
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

@Composable
private fun TodoUpdateDialog(
    title: String,
    initialValue: String,
    initialMemo: String,
    initialScheduledDate: Long,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String) -> Unit
) {
    var text by remember(initialValue) { mutableStateOf(initialValue) }
    var memo by remember(initialMemo) { mutableStateOf(initialMemo) }
    var scheduledDate by remember(initialScheduledDate) { mutableStateOf(initialScheduledDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("할 일") }
                )
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("메모 (선택)") },
                    minLines = 2,
                    maxLines = 4
                )
                Text(
                    text = "예정일 ${formatDate(scheduledDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { scheduledDate = previousDayMillis(scheduledDate) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("이전날")
                    }
                    OutlinedButton(
                        onClick = { scheduledDate = todayStartOfDayMillis() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("오늘")
                    }
                    OutlinedButton(
                        onClick = { scheduledDate = nextDayMillis(scheduledDate) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("다음날")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text, scheduledDate, memo) },
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
