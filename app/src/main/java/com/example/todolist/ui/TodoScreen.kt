package com.example.todolist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todolist.data.local.TodoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTodoId by remember { mutableStateOf<Long?>(null) }
    var editingTodoTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Todo List") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "전체 ${uiState.totalCount}개 · 진행중 ${uiState.activeCount}개 · 완료 ${uiState.completedCount}개",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoFilterButton(
                    text = "전체",
                    selected = uiState.selectedFilter == TodoFilter.ALL,
                    onClick = { viewModel.setFilter(TodoFilter.ALL) }
                )
                TodoFilterButton(
                    text = "진행중",
                    selected = uiState.selectedFilter == TodoFilter.ACTIVE,
                    onClick = { viewModel.setFilter(TodoFilter.ACTIVE) }
                )
                TodoFilterButton(
                    text = "완료",
                    selected = uiState.selectedFilter == TodoFilter.COMPLETED,
                    onClick = { viewModel.setFilter(TodoFilter.COMPLETED) }
                )
            }

            Button(
                onClick = { viewModel.clearCompletedTodos() },
                enabled = uiState.completedCount > 0,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text("완료 항목 삭제")
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.todos,
                    key = { it.id }
                ) { todo ->
                    TodoRow(
                        todo = todo,
                        onToggle = { viewModel.toggleTodo(todo) },
                        onEdit = {
                            editingTodoId = todo.id
                            editingTodoTitle = todo.title
                        },
                        onDelete = { viewModel.deleteTodo(todo) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        TodoEditDialog(
            title = "할 일 추가",
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = {
                viewModel.addTodo(it)
                showAddDialog = false
            }
        )
    }

    editingTodoId?.let { todoId ->
        TodoEditDialog(
            title = "할 일 수정",
            initialValue = editingTodoTitle,
            onDismiss = {
                editingTodoId = null
                editingTodoTitle = ""
            },
            onConfirm = { newTitle ->
                if (newTitle.isNotBlank()) {
                    viewModel.updateTodoTitle(todoId = todoId, newTitle = newTitle)
                }
                editingTodoId = null
                editingTodoTitle = ""
            }
        )
    }
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
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = todo.isCompleted,
            onCheckedChange = { onToggle() }
        )
        Text(
            text = todo.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )
        TextButton(onClick = onEdit) {
            Text("수정")
        }
        TextButton(onClick = onDelete) {
            Text("삭제")
        }
    }
}

@Composable
private fun TodoEditDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("할 일") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
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
