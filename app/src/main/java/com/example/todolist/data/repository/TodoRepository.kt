package com.example.todolist.data.repository

import com.example.todolist.data.local.TodoDao
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.data.local.todayStartOfDayMillis
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val todos: Flow<List<TodoEntity>> = todoDao.getAllTodos()

    suspend fun addTodo(title: String) {
        if (title.isBlank()) return
        val now = System.currentTimeMillis()
        todoDao.insertTodo(
            TodoEntity(
                title = title.trim(),
                createdAt = now,
                updatedAt = now,
                scheduledDate = todayStartOfDayMillis()
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
}
