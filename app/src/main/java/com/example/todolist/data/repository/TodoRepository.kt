package com.example.todolist.data.repository

import com.example.todolist.data.local.TodoDao
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.data.local.todayStartOfDayMillis
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val todos: Flow<List<TodoEntity>> = todoDao.getAllTodos()

    suspend fun addTodo(title: String, scheduledDate: Long = todayStartOfDayMillis(), memo: String? = null, priority: Int = 1) {
        if (title.isBlank()) return
        val now = System.currentTimeMillis()
        todoDao.insertTodo(
            TodoEntity(
                title = title.trim(),
                createdAt = now,
                updatedAt = now,
                scheduledDate = scheduledDate,
                memo = memo?.trim()?.ifEmpty { null },
                priority = priority
            )
        )
    }

    suspend fun updateTodo(todo: TodoEntity) {
        todoDao.updateTodo(todo.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateTodoTitle(todoId: Long, newTitle: String) {
        if (newTitle.isBlank()) return
        todoDao.updateTodoTitle(
            todoId = todoId,
            newTitle = newTitle.trim(),
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updateTodoDetails(todoId: Long, newTitle: String, scheduledDate: Long, memo: String? = null, priority: Int = 1) {
        if (newTitle.isBlank()) return
        todoDao.updateTodoDetails(
            todoId = todoId,
            newTitle = newTitle.trim(),
            scheduledDate = scheduledDate,
            memo = memo?.trim()?.ifEmpty { null },
            priority = priority,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun toggleTodo(todo: TodoEntity) {
        todoDao.updateTodo(
            todo.copy(
                isCompleted = !todo.isCompleted,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteTodo(todo: TodoEntity) {
        todoDao.deleteTodo(todo)
    }

    suspend fun clearCompletedTodos() {
        todoDao.clearCompletedTodos()
    }

    suspend fun clearCompletedTodosForDate(date: Long) {
        todoDao.clearCompletedTodosForDate(date)
    }
}
