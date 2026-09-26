package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.data.remote.AICoachService
import com.example.data.repository.ArohaRepository
import com.example.domain.model.CoachMessage
import com.example.domain.model.FocusPreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ArohaViewModel(
    val repository: ArohaRepository,
    private val aiCoachService: AICoachService = AICoachService()
) : ViewModel() {

    // Repository Flows
    val userState: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val goalsState: StateFlow<List<GoalEntity>> = repository.goalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitsState: StateFlow<List<HabitEntity>> = repository.habitsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMissionState: StateFlow<MissionEntity?> = repository.todayMissionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMissionsState: StateFlow<List<MissionEntity>> = repository.allMissionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleState: StateFlow<List<ScheduleEntity>> = repository.scheduleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attributesState: StateFlow<List<AttributeEntity>> = repository.attributesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievementsState: StateFlow<List<AchievementEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val insightsState: StateFlow<List<AIInsightEntity>> = repository.insightsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntriesState: StateFlow<List<JournalEntryEntity>> = repository.journalEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSyncCount: StateFlow<Int> = repository.syncQueueFlow
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentXpTransactions: StateFlow<List<XPTransactionEntity>> = repository.xpTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Focus State ---
    private val _currentFocusPreset = MutableStateFlow(FocusPreset.DEEP_WORK)
    val currentFocusPreset: StateFlow<FocusPreset> = _currentFocusPreset.asStateFlow()

    private val _focusTimeRemainingSeconds = MutableStateFlow(45 * 60)
    val focusTimeRemainingSeconds: StateFlow<Int> = _focusTimeRemainingSeconds.asStateFlow()

    private val _isFocusRunning = MutableStateFlow(false)
    val isFocusRunning: StateFlow<Boolean> = _isFocusRunning.asStateFlow()

    private val _isFocusCompleted = MutableStateFlow(false)
    val isFocusCompleted: StateFlow<Boolean> = _isFocusCompleted.asStateFlow()

    private val _selectedFocusGoal = MutableStateFlow("Master Modern Android Architecture")
    val selectedFocusGoal: StateFlow<String> = _selectedFocusGoal.asStateFlow()

    private val _isAmbientSoundEnabled = MutableStateFlow(true)
    val isAmbientSoundEnabled: StateFlow<Boolean> = _isAmbientSoundEnabled.asStateFlow()

    private val _isDistractionFree = MutableStateFlow(false)
    val isDistractionFree: StateFlow<Boolean> = _isDistractionFree.asStateFlow()

    private var focusTimerJob: Job? = null

    // --- AI Coach Chat State ---
    private val _coachMessages = MutableStateFlow<List<CoachMessage>>(
        listOf(
            CoachMessage(
                id = "init_1",
                isUser = false,
                text = "${com.example.util.TimeUtils.getGreeting()}, Seeker. I am your Strategic Evolution Coach. Today's greatest return lies in safeguarding your 09:00 Deep Work session. How can I optimize your trajectory?",
                actionableSuggestion = "Start 45m Focus Block"
            )
        )
    )
    val coachMessages: StateFlow<List<CoachMessage>> = _coachMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // --- Toast / Message Banner ---
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // --- Focus Logic ---
    fun selectFocusPreset(preset: FocusPreset) {
        if (_isFocusRunning.value) return
        _currentFocusPreset.value = preset
        _focusTimeRemainingSeconds.value = preset.minutes * 60
        _isFocusCompleted.value = false
    }

    fun setSelectedFocusGoal(goalTitle: String) {
        _selectedFocusGoal.value = goalTitle
    }

    fun toggleFocusRunning() {
        if (_isFocusRunning.value) {
            pauseFocusTimer()
        } else {
            startFocusTimer()
        }
    }

    private fun startFocusTimer() {
        _isFocusRunning.value = true
        _isFocusCompleted.value = false
        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (_focusTimeRemainingSeconds.value > 0) {
                delay(1000)
                _focusTimeRemainingSeconds.value -= 1
            }
            _isFocusRunning.value = false
            _isFocusCompleted.value = true
            val minutes = _currentFocusPreset.value.minutes
            repository.completeFocusSession(
                durationMinutes = minutes,
                mode = _currentFocusPreset.value.label,
                goalTitle = _selectedFocusGoal.value
            )
            _toastEvent.emit("Focus Complete! +${minutes * 2} XP")
        }
    }

    fun pauseFocusTimer() {
        _isFocusRunning.value = false
        focusTimerJob?.cancel()
    }

    fun resetFocusTimer() {
        pauseFocusTimer()
        _focusTimeRemainingSeconds.value = _currentFocusPreset.value.minutes * 60
        _isFocusCompleted.value = false
    }

    fun toggleAmbientSound() {
        _isAmbientSoundEnabled.value = !_isAmbientSoundEnabled.value
    }

    fun toggleDistractionFree() {
        _isDistractionFree.value = !_isDistractionFree.value
    }

    // --- Habit Logic ---
    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabit(habitId)
        }
    }

    fun addHabit(name: String, category: String, frequency: String) {
        viewModelScope.launch {
            repository.addHabit(name, category, frequency)
            _toastEvent.emit("Habit created: $name")
        }
    }

    // --- Mission Logic ---
    fun toggleMissionTask(taskIndex: Int) {
        viewModelScope.launch {
            repository.toggleMissionTask(taskIndex)
        }
    }

    // --- Goals Logic ---
    fun addGoal(title: String, category: String, days: Int, description: String, milestones: List<String>) {
        viewModelScope.launch {
            repository.addGoal(title, category, days, description, milestones)
            _toastEvent.emit("Goal established: $title")
        }
    }

    fun toggleMilestone(goalId: Long, milestoneIndex: Int) {
        viewModelScope.launch {
            repository.toggleMilestone(goalId, milestoneIndex)
        }
    }

    // --- Journal Logic ---
    fun saveJournalEntry(content: String, mood: String, energyLevel: Int, prompt: String, tags: String) {
        viewModelScope.launch {
            repository.saveJournalEntry(content, mood, energyLevel, prompt, tags)
            _toastEvent.emit("Reflection saved. +20 XP")
        }
    }

    // --- Schedule Logic ---
    fun toggleScheduleItem(item: ScheduleEntity) {
        viewModelScope.launch {
            repository.toggleScheduleItem(item)
        }
    }

    fun addScheduleItem(time: String, title: String, subtitle: String, type: String, date: String = "") {
        viewModelScope.launch {
            repository.addScheduleItem(time, title, subtitle, type, date)
            _toastEvent.emit("Scheduled: $title")
        }
    }

    fun bulkAddSchedule(items: List<ScheduleEntity>) {
        viewModelScope.launch {
            repository.bulkAddSchedule(items)
            _toastEvent.emit("Bulk added ${items.size} schedule items")
        }
    }

    fun deleteScheduleItem(item: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteScheduleItem(item)
            _toastEvent.emit("Removed from schedule")
        }
    }

    fun applyInsight(insight: AIInsightEntity) {
        viewModelScope.launch {
            repository.applyInsight(insight)
            _toastEvent.emit("Applied: ${insight.title}")
        }
    }

    // --- Cloud Sync ---
    fun syncNow() {
        viewModelScope.launch {
            val count = repository.syncNow()
            _toastEvent.emit("Cloud Sync complete ($count items synced)")
        }
    }

    // --- Onboarding ---
    fun completeOnboarding(
        name: String,
        priorities: List<String>,
        firstGoal: String,
        category: String,
        wakeTime: String,
        sleepTime: String,
        focusHours: String
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(
                userName = name,
                priorities = priorities,
                firstGoalTitle = firstGoal,
                goalCategory = category,
                wakeTime = wakeTime,
                sleepTime = sleepTime,
                focusHours = focusHours
            )
        }
    }

    // --- AI Coach Chat ---
    fun sendCoachMessage(promptText: String) {
        val userMsg = CoachMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = promptText
        )
        _coachMessages.value = _coachMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val user = userState.value
            val context = "User: ${user?.name}, Level: ${user?.level}, XP: ${user?.currentXp}, Streak: ${user?.streakDays}"
            val responseText = aiCoachService.getStrategicAdvice(promptText, context)
            
            _isAiThinking.value = false
            _coachMessages.value = _coachMessages.value + CoachMessage(
                id = UUID.randomUUID().toString(),
                isUser = false,
                text = responseText
            )
        }
    }

    fun requestMorningBriefing() {
        sendCoachMessage("Provide my Morning Evolution Briefing for today.")
    }

    fun requestEveningReview() {
        sendCoachMessage("Conduct my Evening Reflection Review and assess today's consistency.")
    }

    companion object {
        fun provideFactory(repository: ArohaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ArohaViewModel(repository) as T
                }
            }
    }
}
