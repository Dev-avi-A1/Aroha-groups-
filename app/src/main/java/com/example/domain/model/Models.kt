package com.example.domain.model

data class Milestone(
    val title: String,
    val isCompleted: Boolean
)

data class MissionTask(
    val title: String,
    val isCompleted: Boolean
)

enum class FocusPreset(val label: String, val minutes: Int) {
    POMODORO("Pomodoro", 25),
    DEEP_WORK("Deep Work", 45),
    DEEP_STUDY("Deep Study", 60),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

enum class AnalyticsRange(val label: String) {
    SEVEN_DAYS("7D"),
    THIRTY_DAYS("30D"),
    NINETY_DAYS("90D"),
    ONE_YEAR("1Y")
}

data class CoachMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionableSuggestion: String? = null
)
