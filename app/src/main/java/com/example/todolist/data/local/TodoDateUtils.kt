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

fun previousDayMillis(date: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = date
    cal.add(Calendar.DAY_OF_MONTH, -1)
    return normalizeToStartOfDayMillis(cal.timeInMillis)
}

fun nextDayMillis(date: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = date
    cal.add(Calendar.DAY_OF_MONTH, 1)
    return normalizeToStartOfDayMillis(cal.timeInMillis)
}