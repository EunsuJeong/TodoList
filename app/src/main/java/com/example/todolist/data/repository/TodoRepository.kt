package com.example.todolist.data.repository

import com.example.todolist.data.local.TodoDao
import com.example.todolist.data.local.TodoEntity
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val todos: Flow<List<TodoEntity>> = todoDao.getAllTodos()

    suspend fun addTodo(title: String) {
        if (title.isBlank()) return
        todoDao.insertTodo(TodoEntity(title = title.trim()))
    }

    suspend fun updateTodo(todo: TodoEntity) {
        todoDao.updateTodo(todo)
    }

    suspend fun updateTodoTitle(todoId: Long, newTitle: String) {
        if (newTitle.isBlank()) return
        todoDao.updateTodoTitle(todoId = todoId, newTitle = newTitle.trim())
    }

    suspend fun toggleTodo(todo: TodoEntity) {
        todoDao.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
    }

    suspend fun deleteTodo(todo: TodoEntity) {
        todoDao.deleteTodo(todo)
    }
}
