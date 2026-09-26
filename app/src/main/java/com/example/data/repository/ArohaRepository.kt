package com.example.data.repository

import com.example.data.local.dao.ArohaDao
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class ArohaRepository(private val dao: ArohaDao) {

    // User Profile
    val userFlow: Flow<UserEntity?> = dao.getUserFlow()

    // Goals & Tasks
    val goalsFlow: Flow<List<GoalEntity>> = dao.getAllGoals()
    val tasksFlow: Flow<List<TaskEntity>> = dao.getAllTasks()

    // Habits
    val habitsFlow: Flow<List<HabitEntity>> = dao.getAllHabits()

    // Focus Sessions
    val focusSessionsFlow: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()

    // Journal
    val journalEntriesFlow: Flow<List<JournalEntryEntity>> = dao.getAllJournalEntries()

    // Missions
    val todayMissionFlow: Flow<MissionEntity?> = dao.getTodayMission()
    val allMissionsFlow: Flow<List<MissionEntity>> = dao.getAllMissions()

    // Attributes & Achievements
    val attributesFlow: Flow<List<AttributeEntity>> = dao.getAllAttributes()
    val achievementsFlow: Flow<List<AchievementEntity>> = dao.getAllAchievements()

    // Schedule & Insights
    val scheduleFlow: Flow<List<ScheduleEntity>> = dao.getTodaySchedule()
    val insightsFlow: Flow<List<AIInsightEntity>> = dao.getActiveInsights()
    val syncQueueFlow: Flow<List<SyncQueueEntity>> = dao.getPendingSyncQueue()
    val xpTransactionsFlow: Flow<List<XPTransactionEntity>> = dao.getRecentXpTransactions()

    // --- XP & Level Calculation ---
    suspend fun awardXp(amount: Int, source: String, description: String) {
        val user = dao.getUser() ?: return
        val newTotalXp = user.currentXp + amount
        
        // Calculate dynamic level: each level requires 150 * level XP
        var lvl = user.level
        var req = user.requiredXp
        var remainingXp = newTotalXp

        while (remainingXp >= req && lvl < 100) {
            remainingXp -= req
            lvl++
            req = 1000 + (lvl * 150)
        }

        dao.insertOrUpdateUser(
            user.copy(
                level = lvl,
                currentXp = remainingXp,
                requiredXp = req
            )
        )

        dao.insertXpTransaction(
            XPTransactionEntity(
                amount = amount,
                source = source,
                description = description
            )
        )

        enqueueSync("XP_TRANSACTION", System.currentTimeMillis(), "INSERT", "{\"amount\":$amount}")
    }

    // --- Attribute Growth Helper ---
    suspend fun boostAttribute(attributeName: String, percentBoost: Int = 3) {
        val list = dao.getAllAttributes().firstOrNull() ?: emptyList()
        val attr = list.find { it.name.equals(attributeName, ignoreCase = true) } ?: return
        val newPercent = (attr.progressPercent + percentBoost).coerceAtMost(100)
        val newLevel = if (newPercent >= 100) attr.level + 1 else attr.level
        val updatedPercent = if (newPercent >= 100) newPercent - 100 else newPercent

        dao.updateAttribute(
            attr.copy(
                level = newLevel,
                progressPercent = updatedPercent,
                recentGrowth = "+$percentBoost% recently"
            )
        )
    }

    // --- Habit Actions ---
    suspend fun toggleHabit(habitId: Long) {
        val habit = dao.getHabitById(habitId) ?: return
        val isNowCompleted = !habit.completedToday
        val newStreak = if (isNowCompleted) habit.currentStreak + 1 else (habit.currentStreak - 1).coerceAtLeast(0)
        val newBest = maxOf(habit.bestStreak, newStreak)

        dao.updateHabit(
            habit.copy(
                completedToday = isNowCompleted,
                currentStreak = newStreak,
                bestStreak = newBest
            )
        )

        if (isNowCompleted) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dao.insertHabitCompletion(
                HabitCompletionEntity(habitId = habitId, completedDate = todayStr)
            )
            awardXp(habit.xpReward, "Habit", "Completed: ${habit.name}")
            boostAttribute("Consistency", 2)
            boostAttribute("Discipline", 2)
            enqueueSync("HABIT_COMPLETION", habitId, "COMPLETE", "{\"habitId\":$habitId}")
        }
    }

    suspend fun addHabit(name: String, category: String, frequency: String = "Daily", xpReward: Int = 20) {
        val id = dao.insertHabit(
            HabitEntity(
                name = name,
                category = category,
                frequency = frequency,
                xpReward = xpReward
            )
        )
        enqueueSync("HABIT", id, "INSERT", "{\"name\":\"$name\"}")
    }

    // --- Focus Session Actions ---
    suspend fun completeFocusSession(durationMinutes: Int, mode: String, goalTitle: String) {
        val xp = (durationMinutes * 2).coerceAtLeast(20)
        val focusBoost = (durationMinutes / 10).coerceAtLeast(1)

        val id = dao.insertFocusSession(
            FocusSessionEntity(
                durationMinutes = durationMinutes,
                mode = mode,
                linkedGoalTitle = goalTitle,
                xpEarned = xp,
                focusAttributeGained = focusBoost
            )
        )

        awardXp(xp, "Focus", "Completed $durationMinutes min $mode")
        boostAttribute("Focus", focusBoost * 2)
        boostAttribute("Discipline", focusBoost)

        enqueueSync("FOCUS_SESSION", id, "INSERT", "{\"minutes\":$durationMinutes}")
    }

    // --- Mission Actions ---
    suspend fun toggleMissionTask(taskIndex: Int) {
        val mission = dao.getTodayMission().firstOrNull() ?: return
        val taskItems = mission.tasksJson.split(",").filter { it.isNotBlank() }.toMutableList()
        if (taskIndex !in taskItems.indices) return

        val itemParts = taskItems[taskIndex].split(":")
        val name = itemParts[0]
        val currentDone = itemParts.getOrNull(1)?.toBoolean() ?: false
        val newDone = !currentDone
        taskItems[taskIndex] = "$name:$newDone"

        val completedCount = taskItems.count { it.endsWith(":true") }
        val allDone = completedCount == taskItems.size

        dao.updateMission(
            mission.copy(
                progressCount = completedCount,
                tasksJson = taskItems.joinToString(","),
                isCompleted = allDone
            )
        )

        if (allDone && !mission.isCompleted) {
            awardXp(mission.xpReward, "Mission", "Completed Daily Mission: ${mission.title}")
            boostAttribute("Productivity", 5)
            boostAttribute("Discipline", 4)
        }
    }

    // --- Goal Actions ---
    suspend fun addGoal(
        title: String,
        category: String,
        targetDays: Int,
        description: String = "",
        milestones: List<String> = emptyList()
    ) {
        val milestonesStr = milestones.joinToString(",") { "$it:false" }
        val id = dao.insertGoal(
            GoalEntity(
                title = title,
                category = category,
                targetDays = targetDays,
                remainingDays = targetDays,
                progress = 0,
                description = description,
                milestonesJson = milestonesStr,
                xpReward = 350
            )
        )
        enqueueSync("GOAL", id, "INSERT", "{\"title\":\"$title\"}")
    }

    suspend fun toggleMilestone(goalId: Long, milestoneIndex: Int) {
        val goal = dao.getGoalById(goalId) ?: return
        val list = goal.milestonesJson.split(",").filter { it.isNotBlank() }.toMutableList()
        if (milestoneIndex !in list.indices) return

        val parts = list[milestoneIndex].split(":")
        val name = parts[0]
        val done = parts.getOrNull(1)?.toBoolean() ?: false
        val newDone = !done
        list[milestoneIndex] = "$name:$newDone"

        val doneCount = list.count { it.endsWith(":true") }
        val newProgress = if (list.isNotEmpty()) (doneCount * 100) / list.size else 0

        dao.updateGoal(
            goal.copy(
                milestonesJson = list.joinToString(","),
                progress = newProgress,
                isCompleted = newProgress == 100
            )
        )

        if (newDone) {
            awardXp(75, "Goal Milestone", "Milestone reached: $name")
            boostAttribute(goal.category, 4)
        }
    }

    // --- Journal Actions ---
    suspend fun saveJournalEntry(content: String, mood: String, energyLevel: Int, prompt: String, tags: String) {
        val todayStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
        val id = dao.insertJournalEntry(
            JournalEntryEntity(
                date = todayStr,
                promptQuestion = prompt,
                content = content,
                mood = mood,
                energyLevel = energyLevel,
                tags = tags,
                aiSummary = "Reflected on daily growth and progress."
            )
        )
        awardXp(20, "Journal", "Daily Reflection logged")
        boostAttribute("Mental Clarity", 4)
        enqueueSync("JOURNAL", id, "INSERT", "{\"date\":\"$todayStr\"}")
    }

    // --- Schedule Actions ---
    val allScheduleFlow: Flow<List<ScheduleEntity>> = dao.getAllSchedule()

    fun getScheduleForDate(date: String): Flow<List<ScheduleEntity>> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return dao.getScheduleForDate(date, isToday = (date.isBlank() || date == todayStr))
    }

    suspend fun addScheduleItem(
        time: String,
        title: String,
        subtitle: String,
        type: String,
        date: String = ""
    ): Long {
        val id = dao.insertScheduleItem(
            ScheduleEntity(
                time = time,
                title = title,
                subtitle = subtitle,
                type = type,
                date = date
            )
        )
        awardXp(10, "Schedule", "Scheduled: $title")
        enqueueSync("SCHEDULE", id, "INSERT", "{\"title\":\"$title\",\"time\":\"$time\",\"date\":\"$date\"}")
        return id
    }

    suspend fun bulkAddSchedule(items: List<ScheduleEntity>) {
        if (items.isEmpty()) return
        dao.insertScheduleItems(items)
        awardXp(15 * items.size.coerceAtMost(5), "Schedule", "Bulk added ${items.size} schedule items")
        boostAttribute("Discipline", items.size.coerceAtMost(3))
        for (item in items) {
            enqueueSync("SCHEDULE", item.id, "INSERT", "{\"title\":\"${item.title}\",\"time\":\"${item.time}\",\"date\":\"${item.date}\"}")
        }
    }

    suspend fun deleteScheduleItem(item: ScheduleEntity) {
        dao.deleteScheduleItem(item)
        enqueueSync("SCHEDULE", item.id, "DELETE", "{\"id\":${item.id}}")
    }

    suspend fun toggleScheduleItem(item: ScheduleEntity) {
        dao.updateScheduleItem(item.copy(isCompleted = !item.isCompleted))
        if (!item.isCompleted) {
            awardXp(15, "Schedule", "Executed: ${item.title}")
            boostAttribute("Discipline", 1)
        }
    }

    suspend fun rescheduleItem(item: ScheduleEntity, newTime: String) {
        dao.updateScheduleItem(item.copy(time = newTime))
    }

    // --- AI Insights Actions ---
    suspend fun applyInsight(insight: AIInsightEntity) {
        dao.updateAIInsight(insight.copy(isApplied = true))
        if (insight.type == "Habit") {
            // e.g. move workout
            val schedule = dao.getTodaySchedule().firstOrNull() ?: emptyList()
            val workout = schedule.find { it.title.contains("Workout", ignoreCase = true) }
            if (workout != null) {
                dao.updateScheduleItem(workout.copy(time = "17:30"))
            }
        }
    }

    // --- Sync Queue Actions ---
    private suspend fun enqueueSync(entityType: String, entityId: Long, op: String, payload: String) {
        dao.enqueueSync(
            SyncQueueEntity(
                entityType = entityType,
                entityId = entityId,
                operation = op,
                payloadJson = payload
            )
        )
    }

    suspend fun syncNow(): Int {
        val pending = dao.getPendingSyncQueue().firstOrNull() ?: emptyList()
        val count = pending.size
        dao.clearSyncQueue()
        return count
    }

    // --- Onboarding Completion ---
    suspend fun completeOnboarding(
        userName: String,
        priorities: List<String>,
        firstGoalTitle: String,
        goalCategory: String,
        wakeTime: String,
        sleepTime: String,
        focusHours: String
    ) {
        val user = dao.getUser() ?: UserEntity()
        dao.insertOrUpdateUser(
            user.copy(
                name = userName.ifBlank { "Seeker" },
                priorities = priorities.joinToString(", "),
                wakeTime = wakeTime,
                sleepTime = sleepTime,
                availableFocusHours = focusHours,
                isOnboarded = true
            )
        )

        if (firstGoalTitle.isNotBlank()) {
            addGoal(
                title = firstGoalTitle,
                category = goalCategory.ifBlank { "Discipline" },
                targetDays = 30,
                milestones = listOf("Foundations", "Consistent execution", "Reflection & mastery")
            )
        }
    }
}
