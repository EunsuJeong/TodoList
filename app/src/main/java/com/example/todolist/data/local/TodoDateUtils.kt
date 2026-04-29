package com.example.todolist.data.local

import java.util.Calendar

fun normalizeToStartOfDayMillis(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun todayStartOfDayMillis(): Long = normalizeToStartOfDayMillis(System.currentTimeMillis())