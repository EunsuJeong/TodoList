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

fun monthStartMillis(date: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = date
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun previousMonthMillis(date: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = monthStartMillis(date)
        add(Calendar.MONTH, -1)
    }
    return monthStartMillis(cal.timeInMillis)
}

fun nextMonthMillis(date: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = monthStartMillis(date)
        add(Calendar.MONTH, 1)
    }
    return monthStartMillis(cal.timeInMillis)
}

fun daysInMonth(date: Long): Int {
    return Calendar.getInstance().apply {
        timeInMillis = monthStartMillis(date)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun dayOfWeekOffsetOfMonthStart(date: Long): Int {
    return Calendar.getInstance().apply {
        timeInMillis = monthStartMillis(date)
    }.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
}

fun dateMillisOfMonthDay(monthDate: Long, day: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = monthStartMillis(monthDate)
        set(Calendar.DAY_OF_MONTH, day)
    }.timeInMillis
}

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