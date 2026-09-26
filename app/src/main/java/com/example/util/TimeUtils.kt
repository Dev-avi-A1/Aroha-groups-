package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility for date, time, and system-timezone-aware greetings.
 */
object TimeUtils {

    /**
     * Determines greeting based on the hour of the day in the mobile system's TimeZone:
     * - 05:00 - 11:59: "Good morning"
     * - 12:00 - 16:59: "Good afternoon"
     * - 17:00 - 21:59: "Good evening"
     * - 22:00 - 04:59: "Good night"
     */
    fun getGreeting(
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault(),
        currentTimeMillis: Long = System.currentTimeMillis()
    ): String {
        val calendar = Calendar.getInstance(timeZone, locale).apply {
            timeInMillis = currentTimeMillis
        }
        return getGreeting(calendar)
    }

    fun getGreeting(calendar: Calendar): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return getGreetingForHour(hour)
    }

    fun getGreetingForHour(hourOfDay: Int): String {
        return when (hourOfDay) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    fun getFullGreeting(
        userName: String?,
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        val greeting = getGreeting(timeZone, locale)
        val name = if (!userName.isNullOrBlank()) userName else "Seeker"
        return "$greeting, $name"
    }

    fun getFormattedCurrentDate(
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault(),
        date: Date = Date()
    ): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d", locale)
        sdf.timeZone = timeZone
        return sdf.format(date)
    }

    fun getTodayDateString(timeZone: TimeZone = TimeZone.getDefault()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = timeZone
        return sdf.format(Date())
    }

    fun formatDateForDisplay(dateStr: String, timeZone: TimeZone = TimeZone.getDefault()): String {
        if (dateStr.isBlank()) return "Today"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                this.timeZone = timeZone
            }
            val date = inputFormat.parse(dateStr) ?: return dateStr
            val outputFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).apply {
                this.timeZone = timeZone
            }
            val today = getTodayDateString(timeZone)
            if (dateStr == today) {
                "${outputFormat.format(date)} (Today)"
            } else {
                outputFormat.format(date)
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getCalendarDays(
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault(),
        pastDays: Int = 3,
        futureDays: Int = 10
    ): List<DayOption> {
        val today = getTodayDateString(timeZone)
        val calendar = Calendar.getInstance(timeZone, locale)
        calendar.add(Calendar.DAY_OF_YEAR, -pastDays)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", locale).apply { this.timeZone = timeZone }
        val dayOfWeekFormat = SimpleDateFormat("EEE", locale).apply { this.timeZone = timeZone }
        val dayOfMonthFormat = SimpleDateFormat("d", locale).apply { this.timeZone = timeZone }

        val days = mutableListOf<DayOption>()
        for (i in 0..(pastDays + futureDays)) {
            val dateStr = dateFormat.format(calendar.time)
            val dow = dayOfWeekFormat.format(calendar.time).uppercase()
            val dom = dayOfMonthFormat.format(calendar.time)
            days.add(
                DayOption(
                    dateStr = dateStr,
                    dayOfWeek = dow,
                    dayOfMonth = dom,
                    isToday = (dateStr == today)
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }
}

data class DayOption(
    val dateStr: String, // "yyyy-MM-dd"
    val dayOfWeek: String, // "MON", "TUE"
    val dayOfMonth: String, // "25"
    val isToday: Boolean
)
