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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todolist.R
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.data.preferences.TodoViewPreferences
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

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))

private fun formatMonth(millis: Long): String =
    SimpleDateFormat("yyyy년 M월", Locale.getDefault()).format(Date(millis))

private fun formatCalendarA11yDate(millis: Long): String =
    SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault()).format(Date(millis))

private fun formatKoreanDateWithDay(millis: Long): String =
    SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN).format(Date(millis))

private val dayLabels = listOf("일", "월", "화", "수", "목", "금", "토")

enum class TodoMainTab { TODO, CALENDAR, SEARCH }


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TodoScreen(viewModel: TodoViewModel, preferences: TodoViewPreferences) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTodoId by remember { mutableStateOf<Long?>(null) }
    var editingTodoTitle by remember { mutableStateOf("") }
    var editingTodoMemo by remember { mutableStateOf("") }
    var editingTodoScheduledDate by remember { mutableStateOf(todayStartOfDayMillis()) }
    var editingTodoPriority by remember { mutableStateOf(1) }
    var editingTodoRepeatType by remember { mutableStateOf(0) }
    var addingTodoPriority by remember { mutableStateOf(1) }
    var addingTodoRepeatType by remember { mutableStateOf(0) }
    var selectedDetailTodo by remember { mutableStateOf<TodoEntity?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(preferences.getMainTab()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Todo List") })
        },
        floatingActionButton = {
            if (selectedTab == TodoMainTab.TODO) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Text("+")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == TodoMainTab.TODO,
                    onClick = { 
                        selectedTab = TodoMainTab.TODO
                        preferences.saveMainTab(TodoMainTab.TODO)
                    },
                    icon = {},
                    label = { Text("할 일") }
                )
                NavigationBarItem(
                    selected = selectedTab == TodoMainTab.CALENDAR,
                    onClick = { 
                        selectedTab = TodoMainTab.CALENDAR
                        preferences.saveMainTab(TodoMainTab.CALENDAR)
                    },
                    icon = {},
                    label = { Text("달력") }
                )
                NavigationBarItem(
                    selected = selectedTab == TodoMainTab.SEARCH,
                    onClick = { 
                        selectedTab = TodoMainTab.SEARCH
                        preferences.saveMainTab(TodoMainTab.SEARCH)
                    },
                    icon = {},
                    label = { Text("검색") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            TodoMainTab.TODO -> TodoListTabContent(
                uiState = uiState,
                viewModel = viewModel,
                onViewDetail = { selectedDetailTodo = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            TodoMainTab.CALENDAR -> CalendarTabContent(
                uiState = uiState,
                viewModel = viewModel,
                onDateSelected = { selectedDate ->
                    viewModel.setSelectedDate(selectedDate)
                    selectedTab = TodoMainTab.TODO
                    preferences.saveMainTab(TodoMainTab.TODO)
                },
                onGoToToday = {
                    viewModel.goToToday()
                    selectedTab = TodoMainTab.TODO
                    preferences.saveMainTab(TodoMainTab.TODO)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            TodoMainTab.SEARCH -> SearchTabContent(
                uiState = uiState,
                viewModel = viewModel,
                onViewDetail = { selectedDetailTodo = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    if (showAddDialog) {
        TodoEditDialog(
            title = "할 일 추가",
            initialValue = "",
            initialMemo = "",
            initialPriority = addingTodoPriority,
            initialRepeatType = addingTodoRepeatType,
            onDismiss = { 
                showAddDialog = false
                addingTodoPriority = 1
                addingTodoRepeatType = 0
            },
            onConfirm = { newTitle, newMemo, priority, repeatType ->
                viewModel.addTodo(newTitle, newMemo.trim().ifEmpty { null }, priority, repeatType)
                showAddDialog = false
                addingTodoPriority = 1
                addingTodoRepeatType = 0
            }
        )
    }

    selectedDetailTodo?.let { todo ->
        TodoDetailDialog(
            todo = todo,
            onDismiss = { selectedDetailTodo = null },
            onEdit = {
                selectedDetailTodo = null
                editingTodoId = todo.id
                editingTodoTitle = todo.title
                editingTodoMemo = todo.memo ?: ""
                editingTodoScheduledDate = todo.scheduledDate
                editingTodoPriority = todo.priority
                editingTodoRepeatType = todo.repeatType
            },
            onDelete = {
                viewModel.deleteTodo(todo)
                selectedDetailTodo = null
            }
        )
    }

    editingTodoId?.let { todoId ->
        TodoUpdateDialog(
            title = "할 일 수정",
            initialValue = editingTodoTitle,
            initialMemo = editingTodoMemo,
            initialScheduledDate = editingTodoScheduledDate,
            initialPriority = editingTodoPriority,
            initialRepeatType = editingTodoRepeatType,
            onDismiss = {
                editingTodoId = null
                editingTodoTitle = ""
                editingTodoMemo = ""
                editingTodoScheduledDate = todayStartOfDayMillis()
                editingTodoPriority = 1
                editingTodoRepeatType = 0
            },
            onConfirm = { newTitle, newScheduledDate, newMemo, priority, repeatType ->
                if (newTitle.isNotBlank()) {
                    viewModel.updateTodoDetails(
                        todoId = todoId,
                        newTitle = newTitle,
                        scheduledDate = newScheduledDate,
                        memo = newMemo.trim().ifEmpty { null },
                        priority = priority,
                        repeatType = repeatType
                    )
                }
                editingTodoId = null
                editingTodoTitle = ""
                editingTodoMemo = ""
                editingTodoScheduledDate = todayStartOfDayMillis()
                editingTodoPriority = 1
                editingTodoRepeatType = 0
            }
        )
    }
}

@Composable
private fun TodoListTabContent(
    uiState: TodoUiState,
    viewModel: TodoViewModel,
    onViewDetail: (TodoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SelectedDateHeader(
            selectedDate = uiState.selectedDate,
            totalCount = uiState.totalCount,
            activeCount = uiState.activeCount,
            completedCount = uiState.completedCount,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TodoFilterButton(text = "전체", selected = uiState.selectedFilter == TodoFilter.ALL, onClick = { viewModel.setFilter(TodoFilter.ALL) })
            TodoFilterButton(text = "진행중", selected = uiState.selectedFilter == TodoFilter.ACTIVE, onClick = { viewModel.setFilter(TodoFilter.ACTIVE) })
            TodoFilterButton(text = "완료", selected = uiState.selectedFilter == TodoFilter.COMPLETED, onClick = { viewModel.setFilter(TodoFilter.COMPLETED) })
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TodoFilterButton(text = "중요도 전체", selected = uiState.selectedPriorityFilter == TodoPriorityFilter.ALL, onClick = { viewModel.setPriorityFilter(TodoPriorityFilter.ALL) })
            TodoFilterButton(text = "높음", selected = uiState.selectedPriorityFilter == TodoPriorityFilter.HIGH, onClick = { viewModel.setPriorityFilter(TodoPriorityFilter.HIGH) })
            TodoFilterButton(text = "보통", selected = uiState.selectedPriorityFilter == TodoPriorityFilter.NORMAL, onClick = { viewModel.setPriorityFilter(TodoPriorityFilter.NORMAL) })
            TodoFilterButton(text = "낮음", selected = uiState.selectedPriorityFilter == TodoPriorityFilter.LOW, onClick = { viewModel.setPriorityFilter(TodoPriorityFilter.LOW) })
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TodoFilterButton(text = "최신순", selected = uiState.selectedSort == TodoSort.CREATED_DESC, onClick = { viewModel.setSort(TodoSort.CREATED_DESC) })
            TodoFilterButton(text = "오래된순", selected = uiState.selectedSort == TodoSort.CREATED_ASC, onClick = { viewModel.setSort(TodoSort.CREATED_ASC) })
            TodoFilterButton(text = "수정순", selected = uiState.selectedSort == TodoSort.UPDATED_DESC, onClick = { viewModel.setSort(TodoSort.UPDATED_DESC) })
            TodoFilterButton(text = "중요순", selected = uiState.selectedSort == TodoSort.PRIORITY_DESC, onClick = { viewModel.setSort(TodoSort.PRIORITY_DESC) })
        }
        if (uiState.completedCount > 0) {
            TextButton(
                onClick = { viewModel.clearCompletedTodos() },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "완료 항목 삭제 (${uiState.completedCount})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (uiState.todos.isEmpty()) {
            TodoEmptyState(
                searchQuery = "",
                selectedFilter = uiState.selectedFilter,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = uiState.todos, key = { it.id }) { todo ->
                    TodoRow(
                        todo = todo,
                        onToggle = { viewModel.toggleTodo(todo) },
                        onViewDetail = { onViewDetail(todo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarTabContent(
    uiState: TodoUiState,
    viewModel: TodoViewModel,
    onDateSelected: (Long) -> Unit,
    onGoToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.goToPreviousMonth() }) { Text("◀") }
            Text(text = formatMonth(uiState.visibleMonth), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { viewModel.goToNextMonth() }) { Text("▶") }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(onClick = onGoToToday) { Text("오늘") }
        }
        MonthlyCalendar(
            visibleMonth = uiState.visibleMonth,
            selectedDate = uiState.selectedDate,
            datesWithTodos = uiState.datesWithTodos,
            overdueDates = uiState.overdueDates,
            onDateSelected = onDateSelected,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
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
    }
}

@Composable
private fun SelectedDateHeader(
    selectedDate: Long,
    totalCount: Int,
    activeCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    val today = todayStartOfDayMillis()
    val selectedDateText = formatKoreanDateWithDay(selectedDate)
    val countText = "전체 ${totalCount}개, 진행중 ${activeCount}개, 완료 ${completedCount}개"
    val (statusLabel, statusContainerColor, statusTextColor) = when {
        selectedDate == today -> Triple(
            "오늘",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        selectedDate < today -> Triple(
            "지난 날짜",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        else -> Triple(
            "예정 날짜",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
    val headerContentDescription = "$selectedDateText, $statusLabel, $countText"

    Card(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = headerContentDescription
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDateText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusTextColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusContainerColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text(
                text = "전체 ${totalCount}개 · 진행중 ${activeCount}개 · 완료 ${completedCount}개",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchTabContent(
    uiState: TodoUiState,
    viewModel: TodoViewModel,
    onViewDetail: (TodoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
                placeholder = { Text("제목 또는 메모 검색") }
            )
            if (uiState.searchQuery.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearSearchQuery() }) { Text("✕") }
            }
        }
        val conditionSummary = "${filterLabel(uiState.selectedFilter)} · ${priorityFilterLabel(uiState.selectedPriorityFilter)} · ${sortLabel(uiState.selectedSort)}"
        if (uiState.searchQuery.trim().isNotEmpty()) {
            Text(
                text = "검색 결과 ${uiState.searchResultCount}개 · $conditionSummary",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        if (uiState.searchQuery.trim().isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "검색어를 입력해보세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "전체 할 일에서 제목과 메모를 검색합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "전체 Todo 검색 · $conditionSummary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (uiState.searchTodos.isEmpty()) {
            TodoEmptyState(
                searchQuery = uiState.searchQuery,
                selectedFilter = uiState.selectedFilter,
                modifier = Modifier.fillMaxSize(),
                conditionSummary = conditionSummary,
                onResetFilters = { viewModel.resetSearchFilters() }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = uiState.searchTodos, key = { it.id }) { todo ->
                    TodoRow(
                        todo = todo,
                        onToggle = { viewModel.toggleTodo(todo) },
                        onViewDetail = { onViewDetail(todo) },
                        showScheduledDate = true
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyCalendar(
    visibleMonth: Long,
    selectedDate: Long,
    datesWithTodos: Set<Long>,
    overdueDates: Set<Long>,
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
                    0 -> MaterialTheme.colorScheme.error
                    6 -> MaterialTheme.colorScheme.primary
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
                            dateMillis = dateMillis,
                            dayNumber = dayNumber,
                            columnIndex = columnIndex,
                            isSelected = dateMillis == selectedDate,
                            isToday = dateMillis == today,
                            hasTodos = datesWithTodos.contains(dateMillis),
                            hasOverdueTodo = overdueDates.contains(dateMillis),
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
    dateMillis: Long,
    dayNumber: Int,
    columnIndex: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasTodos: Boolean,
    hasOverdueTodo: Boolean,
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
        0 -> MaterialTheme.colorScheme.error
        6 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        baseTextColor
    }
    // 점 색상 (overdue 우선)
    val defaultDotColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val dotColor = if (hasOverdueTodo) {
        MaterialTheme.colorScheme.error
    } else {
        defaultDotColor
    }
    val todayLabel = stringResource(id = R.string.calendar_accessibility_today)
    val selectedLabel = stringResource(id = R.string.calendar_accessibility_selected)
    val hasTodoLabel = stringResource(id = R.string.calendar_accessibility_has_todo)
    val hasOverdueLabel = stringResource(id = R.string.calendar_accessibility_has_overdue)
    val dateText = formatCalendarA11yDate(dateMillis)
    val statusParts = mutableListOf<String>()
    if (isToday) statusParts.add(todayLabel)
    if (isSelected) statusParts.add(selectedLabel)
    if (hasOverdueTodo) {
        statusParts.add(hasOverdueLabel)
    } else if (hasTodos) {
        statusParts.add(hasTodoLabel)
    }
    val cellContentDescription = if (statusParts.isEmpty()) {
        dateText
    } else {
        "$dateText, ${statusParts.joinToString(", ")}"
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .semantics { contentDescription = cellContentDescription }
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
                    .background(if (hasTodos || hasOverdueTodo) dotColor else Color.Transparent)
            )
        }
    }
}

@Composable
private fun TodoEmptyState(
    searchQuery: String,
    selectedFilter: TodoFilter,
    modifier: Modifier = Modifier,
    conditionSummary: String? = null,
    onResetFilters: (() -> Unit)? = null
) {
    val (icon, title, subtitle) = when {
        searchQuery.trim().isNotEmpty() -> Triple(
            "🔍",
            "검색 결과가 없습니다.",
            "검색어를 바꾸거나 필터를 확인해보세요."
        )
        selectedFilter == TodoFilter.ACTIVE -> Triple(
            "📝",
            "진행중인 할 일이 없습니다.",
            "새 할 일을 추가하거나 완료된 항목을 확인해보세요."
        )
        selectedFilter == TodoFilter.COMPLETED -> Triple(
            "✓",
            "완료된 할 일이 없습니다.",
            "완료 체크한 할 일이 여기에 표시됩니다."
        )
        else -> Triple(
            "📝",
            "아직 등록된 할 일이 없습니다.",
            "오른쪽 아래 + 버튼으로 새 할 일을 추가해보세요."
        )
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (conditionSummary != null) {
                Text(
                    text = "현재 조건: $conditionSummary",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onResetFilters != null) {
                OutlinedButton(
                    onClick = onResetFilters,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "필터 초기화",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

private fun priorityLabel(priority: Int): String = when (priority) {
    0 -> "낮음"
    1 -> "보통"
    else -> "높음"
}

private fun repeatTypeLabel(repeatType: Int): String = when (repeatType) {
    1 -> "매일"
    2 -> "매주"
    3 -> "매월"
    else -> "반복 없음"
}

private fun filterLabel(filter: TodoFilter): String = when (filter) {
    TodoFilter.ALL -> "전체"
    TodoFilter.ACTIVE -> "진행중"
    TodoFilter.COMPLETED -> "완료"
}

private fun priorityFilterLabel(filter: TodoPriorityFilter): String = when (filter) {
    TodoPriorityFilter.ALL -> "중요도 전체"
    TodoPriorityFilter.HIGH -> "높음"
    TodoPriorityFilter.NORMAL -> "보통"
    TodoPriorityFilter.LOW -> "낮음"
}

private fun sortLabel(sort: TodoSort): String = when (sort) {
    TodoSort.CREATED_DESC -> "최신순"
    TodoSort.CREATED_ASC -> "오래된순"
    TodoSort.UPDATED_DESC -> "수정순"
    TodoSort.PRIORITY_DESC -> "중요순"
}

private fun priorityColor(priority: Int, colorScheme: androidx.compose.material3.ColorScheme): androidx.compose.ui.graphics.Color = when (priority) {
    0 -> colorScheme.secondaryContainer
    1 -> colorScheme.surfaceVariant
    else -> colorScheme.errorContainer
}

@Composable
private fun PriorityLabel(priority: Int) {
    val label = priorityLabel(priority)
    val backgroundColor = when (priority) {
        0 -> MaterialTheme.colorScheme.secondaryContainer
        1 -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val textColor = when (priority) {
        0 -> MaterialTheme.colorScheme.onSecondaryContainer
        1 -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onErrorContainer
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
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
    onViewDetail: () -> Unit,
    showScheduledDate: Boolean = false
) {
    val contentAlpha = if (todo.isCompleted) 0.5f else 1f
    val isOverdue = !todo.isCompleted && todo.scheduledDate < todayStartOfDayMillis()
    val overdueColor = MaterialTheme.colorScheme.error
    val cardContainerColor = if (isOverdue) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewDetail() }
                    .padding(vertical = 4.dp)
            ) {
                if (isOverdue) {
                    Text(
                        text = "지난 일정",
                        style = MaterialTheme.typography.labelSmall,
                        color = overdueColor.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                if (!todo.memo.isNullOrBlank()) {
                    Text(
                        text = todo.memo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showScheduledDate) {
                    Text(
                        text = "예정일 ${formatDate(todo.scheduledDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    )
                }
                if (todo.repeatType != 0) {
                    Text(
                        text = repeatTypeLabel(todo.repeatType),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha)
                    )
                }
            }
            PriorityLabel(priority = todo.priority)
        }
    }
}

@Composable
private fun TodoEditDialog(
    title: String,
    initialValue: String,
    initialMemo: String,
    initialPriority: Int,
    initialRepeatType: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int) -> Unit
) {
    var text by remember(initialValue) { mutableStateOf(initialValue) }
    var memo by remember(initialMemo) { mutableStateOf(initialMemo) }
    var priority by remember(initialPriority) { mutableStateOf(initialPriority) }
    var repeatType by remember(initialRepeatType) { mutableStateOf(initialRepeatType) }

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
                Text(
                    text = "중요도",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "낮음", 1 to "보통", 2 to "높음").forEach { (value, label) ->
                        OutlinedButton(
                            onClick = { priority = value },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
                Text(
                    text = "반복",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "없음", 1 to "매일", 2 to "매주", 3 to "매월").forEach { (value, label) ->
                        OutlinedButton(
                            onClick = { repeatType = value },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = if (repeatType == value)
                                ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else
                                ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text, memo, priority, repeatType) },
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
    initialPriority: Int,
    initialRepeatType: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String, Int, Int) -> Unit
) {
    var text by remember(initialValue) { mutableStateOf(initialValue) }
    var memo by remember(initialMemo) { mutableStateOf(initialMemo) }
    var scheduledDate by remember(initialScheduledDate) { mutableStateOf(initialScheduledDate) }
    var priority by remember(initialPriority) { mutableStateOf(initialPriority) }
    var repeatType by remember(initialRepeatType) { mutableStateOf(initialRepeatType) }

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
                    text = "중요도",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "낮음", 1 to "보통", 2 to "높음").forEach { (value, label) ->
                        OutlinedButton(
                            onClick = { priority = value },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
                Text(
                    text = "반복",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "없음", 1 to "매일", 2 to "매주", 3 to "매월").forEach { (value, label) ->
                        OutlinedButton(
                            onClick = { repeatType = value },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = if (repeatType == value)
                                ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else
                                ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }
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
                onClick = { onConfirm(text, scheduledDate, memo, priority, repeatType) },
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
private fun TodoDetailDialog(
    todo: TodoEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = todo.title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // 메모 섹션
                if (!todo.memo.isNullOrEmpty()) {
                    Text(
                        text = todo.memo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    Text(
                        text = "메모 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
                // 정보 행
                DetailInfoRow(label = "예정일", value = formatDate(todo.scheduledDate))
                DetailInfoRow(
                    label = "중요도",
                    value = priorityLabel(todo.priority)
                )
                DetailInfoRow(
                    label = "반복",
                    value = repeatTypeLabel(todo.repeatType)
                )
                DetailInfoRow(
                    label = "상태",
                    value = if (todo.isCompleted) "완료" else "진행중"
                )
                DetailInfoRow(label = "생성일", value = formatDateTime(todo.createdAt))
                DetailInfoRow(label = "수정일", value = formatDateTime(todo.updatedAt))
            }
        },
        confirmButton = {
            Button(onClick = onEdit) {
                Text("수정")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("닫기")
                }
                TextButton(onClick = onDelete) {
                    Text(
                        text = "삭제",
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    )
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.65f)
        )
    }
}
