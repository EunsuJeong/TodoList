package com.example.todolist.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY id DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Query("UPDATE todos SET title = :newTitle, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun updateTodoTitle(todoId: Long, newTitle: String, updatedAt: Long)

    @Query(
        "UPDATE todos SET title = :newTitle, scheduledDate = :scheduledDate, memo = :memo, priority = :priority, repeatType = :repeatType, updatedAt = :updatedAt WHERE id = :todoId"
    )
    suspend fun updateTodoDetails(
        todoId: Long,
        newTitle: String,
        scheduledDate: Long,
        memo: String?,
        priority: Int,
        repeatType: Int,
        updatedAt: Long
    )

    @Query("DELETE FROM todos WHERE isCompleted = 1")
    suspend fun clearCompletedTodos()

    @Query("DELETE FROM todos WHERE isCompleted = 1 AND scheduledDate = :date")
    suspend fun clearCompletedTodosForDate(date: Long)

    @Query("""
        SELECT COUNT(*) FROM todos
        WHERE title = :title
        AND scheduledDate = :scheduledDate
        AND repeatType = :repeatType
        AND isCompleted = 0
    """)
    suspend fun countActiveRecurringCandidate(
        title: String,
        scheduledDate: Long,
        repeatType: Int
    ): Int

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
}
