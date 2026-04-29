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
    val datesWithTodos: Set<Long> = emptySet()
)

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    private val selectedFilter = MutableStateFlow(TodoFilter.ALL)
    private val selectedSort = MutableStateFlow(TodoSort.CREATED_DESC)
    private val _selectedDate = MutableStateFlow(todayStartOfDayMillis())
    private val visibleMonth = MutableStateFlow(monthStartMillis(todayStartOfDayMillis()))

    val uiState: StateFlow<TodoUiState> = combine(
        repository.todos, selectedFilter, selectedSort, _selectedDate, visibleMonth
    ) { todos, filter, sort, date, month ->
        val dateTodos = todos.filter { it.scheduledDate == date }
        val datesWithTodos = todos.map { it.scheduledDate }.toSet()

        val filteredTodos = when (filter) {
            TodoFilter.ALL -> dateTodos
            TodoFilter.ACTIVE -> dateTodos.filter { !it.isCompleted }
            TodoFilter.COMPLETED -> dateTodos.filter { it.isCompleted }
        }

        val sortedTodos = when (sort) {
            TodoSort.CREATED_DESC -> filteredTodos.sortedByDescending { it.createdAt }
            TodoSort.CREATED_ASC -> filteredTodos.sortedBy { it.createdAt }
            TodoSort.UPDATED_DESC -> filteredTodos.sortedByDescending { it.updatedAt }
        }

        TodoUiState(
            todos = sortedTodos,
            selectedFilter = filter,
            selectedSort = sort,
            totalCount = dateTodos.size,
            activeCount = dateTodos.count { !it.isCompleted },
            completedCount = dateTodos.count { it.isCompleted },
            selectedDate = date,
            visibleMonth = month,
            datesWithTodos = datesWithTodos
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

    fun addTodo(title: String) {
        viewModelScope.launch {
            repository.addTodo(title, _selectedDate.value)
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
