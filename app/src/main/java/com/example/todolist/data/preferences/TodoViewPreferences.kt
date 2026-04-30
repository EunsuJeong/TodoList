package com.example.todolist.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.todolist.ui.TodoFilter
import com.example.todolist.ui.TodoPriorityFilter
import com.example.todolist.ui.TodoSort
import com.example.todolist.ui.TodoMainTab

class TodoViewPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "todo_view_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_FILTER = "selected_filter"
        private const val KEY_PRIORITY_FILTER = "selected_priority_filter"
        private const val KEY_SORT = "selected_sort"
        private const val KEY_MAIN_TAB = "selected_main_tab"

        private const val DEFAULT_FILTER = "ALL"
        private const val DEFAULT_PRIORITY_FILTER = "ALL"
        private const val DEFAULT_SORT = "CREATED_DESC"
        private const val DEFAULT_MAIN_TAB = "TODO"
    }

    // Save methods
    fun saveFilter(filter: TodoFilter) {
        prefs.edit().putString(KEY_FILTER, filter.name).apply()
    }

    fun savePriorityFilter(filter: TodoPriorityFilter) {
        prefs.edit().putString(KEY_PRIORITY_FILTER, filter.name).apply()
    }

    fun saveSort(sort: TodoSort) {
        prefs.edit().putString(KEY_SORT, sort.name).apply()
    }

    fun saveMainTab(tab: TodoMainTab) {
        prefs.edit().putString(KEY_MAIN_TAB, tab.name).apply()
    }

    // Restore methods
    fun getFilter(): TodoFilter {
        return try {
            val name = prefs.getString(KEY_FILTER, DEFAULT_FILTER) ?: DEFAULT_FILTER
            TodoFilter.valueOf(name)
        } catch (e: IllegalArgumentException) {
            TodoFilter.ALL
        }
    }

    fun getPriorityFilter(): TodoPriorityFilter {
        return try {
            val name = prefs.getString(KEY_PRIORITY_FILTER, DEFAULT_PRIORITY_FILTER) ?: DEFAULT_PRIORITY_FILTER
            TodoPriorityFilter.valueOf(name)
        } catch (e: IllegalArgumentException) {
            TodoPriorityFilter.ALL
        }
    }

    fun getSort(): TodoSort {
        return try {
            val name = prefs.getString(KEY_SORT, DEFAULT_SORT) ?: DEFAULT_SORT
            TodoSort.valueOf(name)
        } catch (e: IllegalArgumentException) {
            TodoSort.CREATED_DESC
        }
    }

    fun getMainTab(): TodoMainTab {
        return try {
            val name = prefs.getString(KEY_MAIN_TAB, DEFAULT_MAIN_TAB) ?: DEFAULT_MAIN_TAB
            TodoMainTab.valueOf(name)
        } catch (e: IllegalArgumentException) {
            TodoMainTab.TODO
        }
    }
}
