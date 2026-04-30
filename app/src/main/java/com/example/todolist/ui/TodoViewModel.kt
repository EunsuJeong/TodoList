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
import com.example.todolist.data.preferences.TodoViewPreferences
import com.example.todolist.data.repository.TodoRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
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
    UPDATED_DESC,
    PRIORITY_DESC
}

enum class TodoPriorityFilter {
    ALL,
    HIGH,
    NORMAL,
    LOW
}

data class TodoUiState(
    val todos: List<TodoEntity> = emptyList(),
    val searchTodos: List<TodoEntity> = emptyList(),
    val selectedFilter: TodoFilter = TodoFilter.ALL,
    val selectedSort: TodoSort = TodoSort.CREATED_DESC,
    val selectedPriorityFilter: TodoPriorityFilter = TodoPriorityFilter.ALL,
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val selectedDate: Long = 0L,
    val visibleMonth: Long = 0L,
    val datesWithTodos: Set<Long> = emptySet(),
    val overdueDates: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val searchResultCount: Int = 0
)

/** combine() 중간 결과 – 검색 이전 단계의 상태를 전달하기 위한 내부 데이터 클래스 */
private data class BaseTodosState(
    val allTodos: List<TodoEntity>,
    val dateTodos: List<TodoEntity>,
    val statusFilteredDateTodos: List<TodoEntity>,
    val statusFilteredAllTodos: List<TodoEntity>,
    val priorityFilteredDateTodos: List<TodoEntity>,
    val priorityFilteredAllTodos: List<TodoEntity>,
    val selectedFilter: TodoFilter,
    val selectedSort: TodoSort,
    val selectedPriorityFilter: TodoPriorityFilter,
    val selectedDate: Long,
    val visibleMonth: Long,
    val datesWithTodos: Set<Long>,
    val overdueDates: Set<Long>
)

class TodoViewModel(private val repository: TodoRepository, private val preferences: TodoViewPreferences) : ViewModel() {
    private val selectedFilter = MutableStateFlow(preferences.getFilter())
    private val selectedSort = MutableStateFlow(preferences.getSort())
    private val _selectedPriorityFilter = MutableStateFlow(preferences.getPriorityFilter())
    private val _selectedDate = MutableStateFlow(todayStartOfDayMillis())
    private val visibleMonth = MutableStateFlow(monthStartMillis(todayStartOfDayMillis()))
    private val _searchInput = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val debouncedSearchQuery = _searchInput.debounce(300)

    private val effectiveSearchQuery = combine(_searchInput, debouncedSearchQuery) { input, debounced ->
        // Clear should reset filtering immediately without waiting for debounce.
        if (input.trim().isEmpty()) "" else debounced
    }

    val uiState: StateFlow<TodoUiState> = combine(
        repository.todos, selectedFilter, selectedSort, _selectedDate, visibleMonth
    ) { todos, filter, sort, date, month ->
        val dateTodos = todos.filter { it.scheduledDate == date }
        val datesWithTodos = todos.map { it.scheduledDate }.toSet()
        val today = todayStartOfDayMillis()
        val overdueDates = todos
            .asSequence()
            .filter { !it.isCompleted && it.scheduledDate < today }
            .map { it.scheduledDate }
            .toSet()
        val statusFilteredDate = when (filter) {
            TodoFilter.ALL -> dateTodos
            TodoFilter.ACTIVE -> dateTodos.filter { !it.isCompleted }
            TodoFilter.COMPLETED -> dateTodos.filter { it.isCompleted }
        }
        val statusFilteredAll = when (filter) {
            TodoFilter.ALL -> todos
            TodoFilter.ACTIVE -> todos.filter { !it.isCompleted }
            TodoFilter.COMPLETED -> todos.filter { it.isCompleted }
        }
        BaseTodosState(
            allTodos = todos,
            dateTodos = dateTodos,
            statusFilteredDateTodos = statusFilteredDate,
            statusFilteredAllTodos = statusFilteredAll,
            priorityFilteredDateTodos = statusFilteredDate,
            priorityFilteredAllTodos = statusFilteredAll,
            selectedFilter = filter,
            selectedSort = sort,
            selectedPriorityFilter = TodoPriorityFilter.ALL,
            selectedDate = date,
            visibleMonth = month,
            datesWithTodos = datesWithTodos,
            overdueDates = overdueDates
        )
    }.combine(_selectedPriorityFilter) { base, priorityFilter ->
        val priorityFilteredDate = when (priorityFilter) {
            TodoPriorityFilter.ALL -> base.statusFilteredDateTodos
            TodoPriorityFilter.HIGH -> base.statusFilteredDateTodos.filter { it.priority == 2 }
            TodoPriorityFilter.NORMAL -> base.statusFilteredDateTodos.filter { it.priority == 1 }
            TodoPriorityFilter.LOW -> base.statusFilteredDateTodos.filter { it.priority == 0 }
        }
        val priorityFilteredAll = when (priorityFilter) {
            TodoPriorityFilter.ALL -> base.statusFilteredAllTodos
            TodoPriorityFilter.HIGH -> base.statusFilteredAllTodos.filter { it.priority == 2 }
            TodoPriorityFilter.NORMAL -> base.statusFilteredAllTodos.filter { it.priority == 1 }
            TodoPriorityFilter.LOW -> base.statusFilteredAllTodos.filter { it.priority == 0 }
        }
        base.copy(
            priorityFilteredDateTodos = priorityFilteredDate,
            priorityFilteredAllTodos = priorityFilteredAll,
            selectedPriorityFilter = priorityFilter
        )
    }.combine(combine(_searchInput, effectiveSearchQuery) { input, effective -> input to effective }) { base, queries ->
        val inputQuery = queries.first
        val effectiveQuery = queries.second
        val trimmed = effectiveQuery.trim()
        val todoTabSearchFiltered = if (trimmed.isEmpty()) {
            base.priorityFilteredDateTodos
        } else {
            base.priorityFilteredDateTodos.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                    it.memo?.contains(trimmed, ignoreCase = true) == true
            }
        }

        val globalSearchFiltered = if (trimmed.isEmpty()) {
            emptyList()
        } else {
            base.priorityFilteredAllTodos.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                    it.memo?.contains(trimmed, ignoreCase = true) == true
            }
        }

        fun sortTodos(items: List<TodoEntity>): List<TodoEntity> {
            return when (base.selectedSort) {
                TodoSort.CREATED_DESC -> items.sortedByDescending { it.createdAt }
                TodoSort.CREATED_ASC -> items.sortedBy { it.createdAt }
                TodoSort.UPDATED_DESC -> items.sortedByDescending { it.updatedAt }
                TodoSort.PRIORITY_DESC -> items.sortedWith(compareBy({ -it.priority }, { -it.createdAt }))
            }
        }

        val sortedTodoTabTodos = sortTodos(todoTabSearchFiltered)
        val sortedSearchTodos = sortTodos(globalSearchFiltered)

        TodoUiState(
            todos = sortedTodoTabTodos,
            searchTodos = sortedSearchTodos,
            selectedFilter = base.selectedFilter,
            selectedSort = base.selectedSort,
            selectedPriorityFilter = base.selectedPriorityFilter,
            totalCount = base.dateTodos.size,
            activeCount = base.dateTodos.count { !it.isCompleted },
            completedCount = base.dateTodos.count { it.isCompleted },
            selectedDate = base.selectedDate,
            visibleMonth = base.visibleMonth,
            datesWithTodos = base.datesWithTodos,
            overdueDates = base.overdueDates,
            searchQuery = inputQuery,
            searchResultCount = sortedSearchTodos.size
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
        preferences.saveFilter(filter)
    }

    fun setPriorityFilter(filter: TodoPriorityFilter) {
        _selectedPriorityFilter.value = filter
        preferences.savePriorityFilter(filter)
    }

    fun setSort(sort: TodoSort) {
        selectedSort.value = sort
        preferences.saveSort(sort)
    }

    fun resetSearchFilters() {
        setFilter(TodoFilter.ALL)
        setPriorityFilter(TodoPriorityFilter.ALL)
        setSort(TodoSort.CREATED_DESC)
    }

    fun setSearchQuery(query: String) {
        _searchInput.value = query
    }

    fun clearSearchQuery() {
        _searchInput.value = ""
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

    fun addTodo(title: String, memo: String? = null, priority: Int = 1, repeatType: Int = 0) {
        viewModelScope.launch {
            repository.addTodo(title, _selectedDate.value, memo, priority, repeatType)
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

    fun updateTodoDetails(todoId: Long, newTitle: String, scheduledDate: Long, memo: String? = null, priority: Int = 1, repeatType: Int = 0) {
        viewModelScope.launch {
            repository.updateTodoDetails(
                todoId = todoId,
                newTitle = newTitle,
                scheduledDate = scheduledDate,
                memo = memo,
                priority = priority,
                repeatType = repeatType
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
    private val repository: TodoRepository,
    private val preferences: TodoViewPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            return TodoViewModel(repository, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
