package com.example.todolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.data.local.monthStartMillis
import com.example.todolist.data.local.nextDayMillis
import com.example.todolist.data.local.nextMonthMillis
import com.example.todolist.data.local.previousDayMillis
import com.example.todolist.data.local.previousMonthMillis
import com.example.todolist.data.local.todayStartOfDayMillis
import com.example.todolist.data.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TodoFilter {
    ALL,
    ACTIVE,
    COMPLETED
}

enum class TodoSort {
    CREATED_DESC,
    CREATED_ASC,
    UPDATED_DESC
}

data class TodoUiState(
    val todos: List<TodoEntity> = emptyList(),
    val selectedFilter: TodoFilter = TodoFilter.ALL,
    val selectedSort: TodoSort = TodoSort.CREATED_DESC,
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val selectedDate: Long = 0L,
    val visibleMonth: Long = 0L,
    val datesWithTodos: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val searchResultCount: Int = 0
)

/** combine() 중간 결과 – 검색 이전 단계의 상태를 전달하기 위한 내부 데이터 클래스 */
private data class BaseTodosState(
    val dateTodos: List<TodoEntity>,
    val statusFilteredTodos: List<TodoEntity>,
    val selectedFilter: TodoFilter,
    val selectedSort: TodoSort,
    val selectedDate: Long,
    val visibleMonth: Long,
    val datesWithTodos: Set<Long>
)

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    private val selectedFilter = MutableStateFlow(TodoFilter.ALL)
    private val selectedSort = MutableStateFlow(TodoSort.CREATED_DESC)
    private val _selectedDate = MutableStateFlow(todayStartOfDayMillis())
    private val visibleMonth = MutableStateFlow(monthStartMillis(todayStartOfDayMillis()))
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<TodoUiState> = combine(
        repository.todos, selectedFilter, selectedSort, _selectedDate, visibleMonth
    ) { todos, filter, sort, date, month ->
        val dateTodos = todos.filter { it.scheduledDate == date }
        val datesWithTodos = todos.map { it.scheduledDate }.toSet()
        val statusFiltered = when (filter) {
            TodoFilter.ALL -> dateTodos
            TodoFilter.ACTIVE -> dateTodos.filter { !it.isCompleted }
            TodoFilter.COMPLETED -> dateTodos.filter { it.isCompleted }
        }
        BaseTodosState(
            dateTodos = dateTodos,
            statusFilteredTodos = statusFiltered,
            selectedFilter = filter,
            selectedSort = sort,
            selectedDate = date,
            visibleMonth = month,
            datesWithTodos = datesWithTodos
        )
    }.combine(_searchQuery) { base, query ->
        val trimmed = query.trim()
        val searchFiltered = if (trimmed.isEmpty()) {
            base.statusFilteredTodos
        } else {
            base.statusFilteredTodos.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                    it.memo?.contains(trimmed, ignoreCase = true) == true
            }
        }
        val sortedTodos = when (base.selectedSort) {
            TodoSort.CREATED_DESC -> searchFiltered.sortedByDescending { it.createdAt }
            TodoSort.CREATED_ASC -> searchFiltered.sortedBy { it.createdAt }
            TodoSort.UPDATED_DESC -> searchFiltered.sortedByDescending { it.updatedAt }
        }
        TodoUiState(
            todos = sortedTodos,
            selectedFilter = base.selectedFilter,
            selectedSort = base.selectedSort,
            totalCount = base.dateTodos.size,
            activeCount = base.dateTodos.count { !it.isCompleted },
            completedCount = base.dateTodos.count { it.isCompleted },
            selectedDate = base.selectedDate,
            visibleMonth = base.visibleMonth,
            datesWithTodos = base.datesWithTodos,
            searchQuery = query,
            searchResultCount = sortedTodos.size
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodoUiState(
                selectedDate = todayStartOfDayMillis(),
                visibleMonth = monthStartMillis(todayStartOfDayMillis())
            )
        )

    private fun updateSelectedDate(date: Long) {
        _selectedDate.value = date
        visibleMonth.value = monthStartMillis(date)
    }

    fun setFilter(filter: TodoFilter) {
        selectedFilter.value = filter
    }

    fun setSort(sort: TodoSort) {
        selectedSort.value = sort
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun setSelectedDate(date: Long) {
        updateSelectedDate(date)
    }

    fun goToPreviousDay() {
        updateSelectedDate(previousDayMillis(_selectedDate.value))
    }

    fun goToNextDay() {
        updateSelectedDate(nextDayMillis(_selectedDate.value))
    }

    fun goToPreviousMonth() {
        visibleMonth.value = previousMonthMillis(visibleMonth.value)
    }

    fun goToNextMonth() {
        visibleMonth.value = nextMonthMillis(visibleMonth.value)
    }

    fun goToToday() {
        updateSelectedDate(todayStartOfDayMillis())
    }

    fun addTodo(title: String, memo: String? = null) {
        viewModelScope.launch {
            repository.addTodo(title, _selectedDate.value, memo)
        }
    }

    fun toggleTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.toggleTodo(todo)
        }
    }

    fun updateTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.updateTodo(todo)
        }
    }

    fun updateTodoTitle(todoId: Long, newTitle: String) {
        viewModelScope.launch {
            repository.updateTodoTitle(todoId = todoId, newTitle = newTitle)
        }
    }

    fun updateTodoDetails(todoId: Long, newTitle: String, scheduledDate: Long, memo: String? = null) {
        viewModelScope.launch {
            repository.updateTodoDetails(
                todoId = todoId,
                newTitle = newTitle,
                scheduledDate = scheduledDate,
                memo = memo
            )
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }

    fun clearCompletedTodos() {
        viewModelScope.launch {
            repository.clearCompletedTodosForDate(_selectedDate.value)
        }
    }
}

class TodoViewModelFactory(
    private val repository: TodoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            return TodoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
