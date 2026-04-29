package com.example.todolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.local.TodoEntity
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
    val completedCount: Int = 0
)

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    private val selectedFilter = MutableStateFlow(TodoFilter.ALL)
    private val selectedSort = MutableStateFlow(TodoSort.CREATED_DESC)

    val uiState: StateFlow<TodoUiState> = combine(repository.todos, selectedFilter, selectedSort) { todos, filter, sort ->
        val filteredTodos = when (filter) {
            TodoFilter.ALL -> todos
            TodoFilter.ACTIVE -> todos.filter { !it.isCompleted }
            TodoFilter.COMPLETED -> todos.filter { it.isCompleted }
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
            totalCount = todos.size,
            activeCount = todos.count { !it.isCompleted },
            completedCount = todos.count { it.isCompleted }
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodoUiState()
        )

    fun setFilter(filter: TodoFilter) {
        selectedFilter.value = filter
    }

    fun setSort(sort: TodoSort) {
        selectedSort.value = sort
    }

    fun addTodo(title: String) {
        viewModelScope.launch {
            repository.addTodo(title)
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
            repository.clearCompletedTodos()
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
