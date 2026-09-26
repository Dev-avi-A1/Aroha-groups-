package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Seeker",
    val level: Int = 17,
    val currentXp: Int = 2450,
    val requiredXp: Int = 3000,
    val streakDays: Int = 12,
    val lastActiveDate: String = "",
    val isOnboarded: Boolean = true,
    val priorities: String = "Discipline, Focus, Knowledge",
    val wakeTime: String = "06:30",
    val sleepTime: String = "22:30",
    val availableFocusHours: String = "4 hours"
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String, // Discipline, Focus, Knowledge, Fitness, Career, Mastery
    val progress: Int, // 0..100
    val targetDays: Int,
    val remainingDays: Int,
    val priority: String = "HIGH", // HIGH, MEDIUM, LOW
    val milestonesJson: String = "", // JSON list of Milestone items
    val linkedHabitsJson: String = "",
    val xpReward: Int = 250,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "General",
    val priority: String = "MEDIUM",
    val isCompleted: Boolean = false,
    val scheduledTime: String = "09:00",
    val durationMinutes: Int = 30,
    val linkedGoalId: Long? = null,
    val xpReward: Int = 30
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Discipline, Focus, Body, Mind
    val frequency: String = "Daily",
    val targetCount: Int = 1,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completedToday: Boolean = false,
    val xpReward: Int = 20,
    val iconName: String = "check",
    val linkedGoalId: Long? = null
)

@Entity(tableName = "habit_completions")
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val completedDate: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMinutes: Int,
    val mode: String, // Deep Work, Pomodoro, Study, Reflection
    val linkedGoalTitle: String = "General Evolution",
    val timestamp: Long = System.currentTimeMillis(),
    val xpEarned: Int = 100,
    val focusAttributeGained: Int = 5
)

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "Today, Sep 25" or YYYY-MM-DD
    val promptQuestion: String = "What mattered most today?",
    val content: String,
    val mood: String = "Focused", // Zen, Energetic, Focused, Tired, Grateful
    val energyLevel: Int = 4, // 1..5
    val tags: String = "Reflection, Growth",
    val aiSummary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val title: String,
    val description: String,
    val progressCount: Int,
    val totalCount: Int,
    val xpReward: Int = 250,
    val isCompleted: Boolean = false,
    val tasksJson: String = "" // List of mission item strings
)

@Entity(tableName = "xp_transactions")
data class XPTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val source: String, // Habit, Focus, Mission, Goal, Journal
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "attributes")
data class AttributeEntity(
    @PrimaryKey val name: String, // Discipline, Focus, Knowledge, Fitness, Mental Clarity, Consistency, Productivity
    val level: Int,
    val progressPercent: Int,
    val recentGrowth: String,
    val contributingActivities: String,
    val aiInsight: String
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null,
    val xpReward: Int = 100,
    val category: String = "Milestones",
    val iconName: String = "star"
)

@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: String,
    val title: String,
    val subtitle: String,
    val type: String, // Routine, Deep Work, Study, Workout, Reflection
    val isCompleted: Boolean = false,
    val date: String = "" // YYYY-MM-DD (e.g. 2026-09-25), empty string matches default/today
)

@Entity(tableName = "ai_insights")
data class AIInsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // Home, Habit, Goal, Focus, Schedule
    val title: String,
    val message: String,
    val suggestedAction: String? = null,
    val actionRoute: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isApplied: Boolean = false
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val operation: String, // INSERT, UPDATE, DELETE
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING"
)
