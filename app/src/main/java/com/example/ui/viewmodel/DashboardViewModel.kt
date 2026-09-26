package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AIInsightEntity
import com.example.data.local.entity.AttributeEntity
import com.example.data.local.entity.MissionEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.ArohaRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.util.DayOption
import com.example.util.TimeUtils
import java.util.TimeZone

private data class DashboardRepoData(
    val user: UserEntity?,
    val mission: MissionEntity?,
    val allSchedules: List<ScheduleEntity>,
    val attributes: List<AttributeEntity>,
    val insights: List<AIInsightEntity>
)

/**
 * UI State representation for the Dashboard screen.
 * Encapsulates all data-driven updates reactively bound to local Room database flows.
 */
data class DashboardUiState(
    val user: UserEntity? = null,
    val todayMission: MissionEntity? = null,
    val scheduleItems: List<ScheduleEntity> = emptyList(),
    val allScheduleItems: List<ScheduleEntity> = emptyList(),
    val attributes: List<AttributeEntity> = emptyList(),
    val insights: List<AIInsightEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val timeZone: TimeZone = TimeZone.getDefault(),
    val selectedDate: String = TimeUtils.getTodayDateString(),
    val calendarDays: List<DayOption> = emptyList()
) {
    val completedScheduleCount: Int
        get() = scheduleItems.count { it.isCompleted }

    val totalScheduleCount: Int
        get() = scheduleItems.size

    val activeInsight: AIInsightEntity?
        get() = insights.firstOrNull()

    val greeting: String
        get() = TimeUtils.getGreeting(timeZone)

    val formattedDate: String
        get() = TimeUtils.getFormattedCurrentDate(timeZone)

    val formattedSelectedDate: String
        get() = TimeUtils.formatDateForDisplay(selectedDate, timeZone)

    val personalizedGreeting: String
        get() = TimeUtils.getFullGreeting(user?.name, timeZone)
}

/**
 * Dedicated ViewModel for the Dashboard screen.
 * Manages UI state and provides a foundation for reactive, data-driven updates from the database.
 */
class DashboardViewModel(
    private val repository: ArohaRepository
) : ViewModel() {

    private val _selectedDate = kotlinx.coroutines.flow.MutableStateFlow(TimeUtils.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate

    private val repoBaseFlow = combine(
        repository.userFlow,
        repository.todayMissionFlow,
        repository.allScheduleFlow,
        repository.attributesFlow,
        repository.insightsFlow
    ) { user, mission, allSchedules, attributes, insights ->
        DashboardRepoData(user, mission, allSchedules, attributes, insights)
    }

    // Single unified UI State for the Dashboard, combining repository flows and selected date
    val uiState: StateFlow<DashboardUiState> = combine(
        repoBaseFlow,
        _selectedDate
    ) { data, selectedDate ->
        val todayStr = TimeUtils.getTodayDateString()
        val isToday = (selectedDate == todayStr)
        val filtered = data.allSchedules.filter {
            it.date == selectedDate || (it.date.isBlank() && isToday)
        }.sortedBy { it.time }

        DashboardUiState(
            user = data.user,
            todayMission = data.mission,
            scheduleItems = filtered,
            allScheduleItems = data.allSchedules,
            attributes = data.attributes,
            insights = data.insights,
            isLoading = false,
            selectedDate = selectedDate,
            calendarDays = TimeUtils.getCalendarDays()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true, calendarDays = TimeUtils.getCalendarDays())
    )

    // Direct access to individual flows for backwards compatibility and fine-grained UI reactivity
    val userState: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayMissionState: StateFlow<MissionEntity?> = repository.todayMissionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val scheduleState: StateFlow<List<ScheduleEntity>> = repository.scheduleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attributesState: StateFlow<List<AttributeEntity>> = repository.attributesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val insightsState: StateFlow<List<AIInsightEntity>> = repository.insightsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    // --- User Actions & Database Updates ---

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun addScheduleItem(
        time: String,
        title: String,
        subtitle: String,
        type: String,
        date: String = _selectedDate.value
    ) {
        viewModelScope.launch {
            val targetDate = if (date.isBlank()) _selectedDate.value else date
            repository.addScheduleItem(
                time = time,
                title = title,
                subtitle = subtitle,
                type = type,
                date = targetDate
            )
            _eventFlow.emit("Added to schedule: $title")
        }
    }

    fun bulkAddSchedule(items: List<ScheduleEntity>) {
        viewModelScope.launch {
            repository.bulkAddSchedule(items)
            _eventFlow.emit("Bulk added ${items.size} schedule items")
        }
    }

    fun deleteScheduleItem(item: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteScheduleItem(item)
            _eventFlow.emit("Removed: ${item.title}")
        }
    }

    fun applyPresetSchedule(presetName: String, targetDate: String = _selectedDate.value) {
        val date = if (targetDate.isBlank()) _selectedDate.value else targetDate
        val items = when (presetName) {
            "Elite Deep Work Day" -> listOf(
                ScheduleEntity(time = "07:00", title = "Morning Protocol", subtitle = "Hydration, breathwork & sunlight", type = "Routine", date = date),
                ScheduleEntity(time = "09:00", title = "Architecture & Deep Build", subtitle = "Zero-distraction high cognitive work", type = "Deep Work", date = date),
                ScheduleEntity(time = "12:00", title = "Mindful Refuel & Walk", subtitle = "Nervous system reset & clean meal", type = "Routine", date = date),
                ScheduleEntity(time = "13:30", title = "Execution & Implementation", subtitle = "Core feature completion & testing", type = "Deep Work", date = date),
                ScheduleEntity(time = "16:30", title = "Strength & Athletic Training", subtitle = "Compound lifts & anaerobic intervals", type = "Workout", date = date),
                ScheduleEntity(time = "20:30", title = "Sanctuary Reflection & Debrief", subtitle = "Log wins and design tomorrow's plan", type = "Reflection", date = date)
            )
            "Intense Study & Mastery" -> listOf(
                ScheduleEntity(time = "08:00", title = "Core Theory & Documentation", subtitle = "Deep study of advanced concepts", type = "Study", date = date),
                ScheduleEntity(time = "10:30", title = "Hands-on Technical Practice", subtitle = "Apply learnings in code sandbox", type = "Deep Work", date = date),
                ScheduleEntity(time = "14:00", title = "Research Paper & Analysis", subtitle = "Analyze reference implementations", type = "Study", date = date),
                ScheduleEntity(time = "16:30", title = "Cardio & Physical Reset", subtitle = "Zone-2 recovery run or cycling", type = "Workout", date = date),
                ScheduleEntity(time = "19:30", title = "Active Recall & Synthesis", subtitle = "Synthesize key learnings into knowledge base", type = "Study", date = date)
            )
            "Sprint Execution Day" -> listOf(
                ScheduleEntity(time = "08:30", title = "Standup & Goal Calibration", subtitle = "Review daily objectives & blockers", type = "Routine", date = date),
                ScheduleEntity(time = "09:30", title = "Feature Build Sprint 1", subtitle = "UI layout & user interaction flow", type = "Deep Work", date = date),
                ScheduleEntity(time = "13:00", title = "Code Review & Refactoring", subtitle = "Review PRs & optimize performance", type = "Deep Work", date = date),
                ScheduleEntity(time = "15:30", title = "Feature Build Sprint 2", subtitle = "Integrations & test suite execution", type = "Deep Work", date = date),
                ScheduleEntity(time = "18:00", title = "Daily Retro & Wins Log", subtitle = "Mark completed deliverables", type = "Reflection", date = date)
            )
            "Weekend Wellness & Recharge" -> listOf(
                ScheduleEntity(time = "08:00", title = "Outdoor Nature Walk", subtitle = "Fresh air, movement & meditation", type = "Routine", date = date),
                ScheduleEntity(time = "10:30", title = "Creative Reading & Journaling", subtitle = "Free writing & philosophical inquiry", type = "Reflection", date = date),
                ScheduleEntity(time = "14:00", title = "High-Leverage Learning", subtitle = "Explore emerging technologies", type = "Study", date = date),
                ScheduleEntity(time = "17:30", title = "Full Mobility & Yoga Session", subtitle = "Fascial release & deep relaxation", type = "Workout", date = date)
            )
            else -> emptyList()
        }
        if (items.isNotEmpty()) {
            bulkAddSchedule(items)
        }
    }

    fun parseAndBulkAdd(rawText: String, targetDate: String = _selectedDate.value, defaultType: String = "Deep Work"): Int {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return 0

        val date = if (targetDate.isBlank()) _selectedDate.value else targetDate
        val items = mutableListOf<ScheduleEntity>()
        val timeRegex = Regex("""^(\d{1,2}:\d{2})\s*[-–—:]?\s*(.*)$""")

        lines.forEachIndexed { index, line ->
            val match = timeRegex.find(line)
            val (time, rawTitle) = if (match != null) {
                val rawTime = match.groupValues[1]
                val formattedTime = if (rawTime.length == 4) "0$rawTime" else rawTime
                formattedTime to match.groupValues[2].trim()
            } else {
                val autoHour = (8 + index * 2).coerceAtMost(22)
                val formattedTime = String.format("%02d:00", autoHour)
                formattedTime to line
            }

            val title: String
            val subtitle: String
            if (rawTitle.contains(" - ")) {
                val parts = rawTitle.split(" - ", limit = 2)
                title = parts[0].trim()
                subtitle = parts[1].trim()
            } else if (rawTitle.contains(" — ")) {
                val parts = rawTitle.split(" — ", limit = 2)
                title = parts[0].trim()
                subtitle = parts[1].trim()
            } else {
                title = rawTitle.ifBlank { "Task ${index + 1}" }
                subtitle = "Scheduled item"
            }

            val type = when {
                title.contains("routine", ignoreCase = true) || title.contains("morning", ignoreCase = true) || title.contains("walk", ignoreCase = true) -> "Routine"
                title.contains("study", ignoreCase = true) || title.contains("read", ignoreCase = true) || title.contains("learn", ignoreCase = true) -> "Study"
                title.contains("workout", ignoreCase = true) || title.contains("gym", ignoreCase = true) || title.contains("run", ignoreCase = true) -> "Workout"
                title.contains("reflect", ignoreCase = true) || title.contains("journal", ignoreCase = true) -> "Reflection"
                else -> defaultType
            }

            items.add(
                ScheduleEntity(
                    time = time,
                    title = title,
                    subtitle = subtitle,
                    type = type,
                    date = date
                )
            )
        }

        if (items.isNotEmpty()) {
            bulkAddSchedule(items)
        }
        return items.size
    }

    fun toggleMissionTask(taskIndex: Int) {
        viewModelScope.launch {
            repository.toggleMissionTask(taskIndex)
        }
    }

    fun toggleScheduleItem(item: ScheduleEntity) {
        viewModelScope.launch {
            repository.toggleScheduleItem(item)
        }
    }

    fun applyInsight(insight: AIInsightEntity) {
        viewModelScope.launch {
            repository.applyInsight(insight)
            _eventFlow.emit("Applied: ${insight.title}")
        }
    }

    companion object {
        fun provideFactory(repository: ArohaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(repository) as T
                }
            }
    }
}
