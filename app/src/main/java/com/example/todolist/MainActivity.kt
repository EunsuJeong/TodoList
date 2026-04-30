package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.data.local.TodoDatabase
import com.example.todolist.data.preferences.TodoViewPreferences
import com.example.todolist.data.repository.TodoRepository
import com.example.todolist.ui.TodoScreen
import com.example.todolist.ui.TodoViewModel
import com.example.todolist.ui.TodoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = TodoRepository(TodoDatabase.getInstance(applicationContext).todoDao())
        val preferences = TodoViewPreferences(applicationContext)

        setContent {
            val viewModel: TodoViewModel = viewModel(
                factory = TodoViewModelFactory(repository, preferences)
            )

            MaterialTheme {
                Surface {
                    TodoScreen(viewModel = viewModel, preferences = preferences)
                }
            }
        }
    }
}
