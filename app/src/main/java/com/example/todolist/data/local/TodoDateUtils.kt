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

/**
 * Returns the next scheduledDate based on repeatType.
 * repeatType: 0=none, 1=daily, 2=weekly, 3=monthly
 * Returns null if repeatType is 0 (no repeat).
 * For monthly repeat, if the target month has fewer days, clamps to the last day of that month.
 */
fun nextRepeatDateMillis(currentDate: Long, repeatType: Int): Long? {
    return when (repeatType) {
        1 -> {
            val cal = Calendar.getInstance()
            cal.timeInMillis = currentDate
            cal.add(Calendar.DAY_OF_MONTH, 1)
            normalizeToStartOfDayMillis(cal.timeInMillis)
        }
        2 -> {
            val cal = Calendar.getInstance()
            cal.timeInMillis = currentDate
            cal.add(Calendar.DAY_OF_MONTH, 7)
            normalizeToStartOfDayMillis(cal.timeInMillis)
        }
        3 -> {
            val cal = Calendar.getInstance()
            cal.timeInMillis = currentDate
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.MONTH, 1)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, minOf(dayOfMonth, maxDay))
            normalizeToStartOfDayMillis(cal.timeInMillis)
        }
        else -> null
    }
}