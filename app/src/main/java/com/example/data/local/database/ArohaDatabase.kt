package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ArohaDao
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        GoalEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        FocusSessionEntity::class,
        JournalEntryEntity::class,
        MissionEntity::class,
        XPTransactionEntity::class,
        AttributeEntity::class,
        AchievementEntity::class,
        ScheduleEntity::class,
        AIInsightEntity::class,
        SyncQueueEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ArohaDatabase : RoomDatabase() {

    abstract fun arohaDao(): ArohaDao

    companion object {
        @Volatile
        private var INSTANCE: ArohaDatabase? = null

        fun getInstance(context: Context): ArohaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArohaDatabase::class.java,
                    "aroha_evolution.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate with initial starter data
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateInitialData(database.arohaDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: ArohaDao) {
            // User
            dao.insertOrUpdateUser(
                UserEntity(
                    id = 1,
                    name = "Seeker",
                    level = 17,
                    currentXp = 2450,
                    requiredXp = 3000,
                    streakDays = 12,
                    lastActiveDate = "2026-09-25",
                    isOnboarded = true,
                    priorities = "Discipline, Focus, Knowledge, Fitness",
                    wakeTime = "06:30",
                    sleepTime = "22:30",
                    availableFocusHours = "4 hours"
                )
            )

            // Initial Goals
            dao.insertGoal(
                GoalEntity(
                    id = 1,
                    title = "Master Modern Android Architecture",
                    description = "Achieve mastery in Jetpack Compose, Clean Architecture, and Coroutines.",
                    category = "Knowledge",
                    progress = 72,
                    targetDays = 30,
                    remainingDays = 18,
                    priority = "HIGH",
                    milestonesJson = "Kotlin Basics:true,Coroutines & Flow:true,Jetpack Compose:true,Clean Architecture:false,Offline-First Room:false",
                    linkedHabitsJson = "Deep Study,Code Review",
                    xpReward = 500,
                    isCompleted = false
                )
            )
            dao.insertGoal(
                GoalEntity(
                    id = 2,
                    title = "Peak Physical Conditioning",
                    description = "Build a consistent athletic routine with strength and cardiovascular endurance.",
                    category = "Fitness",
                    progress = 60,
                    targetDays = 60,
                    remainingDays = 34,
                    priority = "MEDIUM",
                    milestonesJson = "Morning Mobility:true,5km Endurance Run:true,Strength Progression:false,Recovery Optimization:false",
                    linkedHabitsJson = "Morning Workout,Hydration 3L",
                    xpReward = 400,
                    isCompleted = false
                )
            )
            dao.insertGoal(
                GoalEntity(
                    id = 3,
                    title = "Daily Deep Work Discipline",
                    description = "Establish uninterrupted 90-minute blocks of high-leverage focus sessions.",
                    category = "Focus",
                    progress = 85,
                    targetDays = 21,
                    remainingDays = 5,
                    priority = "HIGH",
                    milestonesJson = "Distraction-free environment:true,Morning session start:true,Zero social media during work:true,Evening debrief:false",
                    linkedHabitsJson = "Morning Focus Block,No Phone Before 10AM",
                    xpReward = 350,
                    isCompleted = false
                )
            )

            // Initial Habits
            dao.insertHabit(
                HabitEntity(
                    id = 1,
                    name = "Read 20 min",
                    category = "Knowledge",
                    frequency = "Daily",
                    currentStreak = 12,
                    bestStreak = 24,
                    completedToday = true,
                    xpReward = 20,
                    iconName = "book"
                )
            )
            dao.insertHabit(
                HabitEntity(
                    id = 2,
                    name = "Morning Workout",
                    category = "Fitness",
                    frequency = "Daily",
                    currentStreak = 8,
                    bestStreak = 15,
                    completedToday = true,
                    xpReward = 30,
                    iconName = "fitness"
                )
            )
            dao.insertHabit(
                HabitEntity(
                    id = 3,
                    name = "Deep Focus 45m",
                    category = "Focus",
                    frequency = "Daily",
                    currentStreak = 14,
                    bestStreak = 14,
                    completedToday = true,
                    xpReward = 40,
                    iconName = "timer"
                )
            )
            dao.insertHabit(
                HabitEntity(
                    id = 4,
                    name = "Evening Reflection",
                    category = "Mind",
                    frequency = "Daily",
                    currentStreak = 9,
                    bestStreak = 18,
                    completedToday = false,
                    xpReward = 20,
                    iconName = "journal"
                )
            )
            dao.insertHabit(
                HabitEntity(
                    id = 5,
                    name = "Cold Shower & Breathing",
                    category = "Discipline",
                    frequency = "Daily",
                    currentStreak = 5,
                    bestStreak = 10,
                    completedToday = false,
                    xpReward = 20,
                    iconName = "water"
                )
            )

            // Initial Mission
            dao.insertMission(
                MissionEntity(
                    id = 1,
                    date = "Today",
                    title = "Build Unstoppable Momentum",
                    description = "Execute core daily rituals to reinforce discipline and cognitive clarity.",
                    progressCount = 3,
                    totalCount = 5,
                    xpReward = 250,
                    isCompleted = false,
                    tasksJson = "30 min focus session:true,Morning routine workout:true,Review goal milestones:true,Evening journal reflection:false,Hydrate 3 Liters:false"
                )
            )

            // Today's Schedule Timeline
            dao.insertScheduleItems(
                listOf(
                    ScheduleEntity(time = "07:00", title = "Morning Routine", subtitle = "Hydration, stretching, cold exposure", type = "Routine", isCompleted = true),
                    ScheduleEntity(time = "09:00", title = "Deep Work Block", subtitle = "Core project architecture & development", type = "Deep Work", isCompleted = true),
                    ScheduleEntity(time = "11:30", title = "Knowledge & Study", subtitle = "Architecture patterns & Kotlin Flow", type = "Study", isCompleted = true),
                    ScheduleEntity(time = "14:30", title = "Strength & Mobility Workout", subtitle = "High intensity functional training", type = "Workout", isCompleted = false),
                    ScheduleEntity(time = "17:00", title = "Execution & Review", subtitle = "Refactor code & update milestones", type = "Deep Work", isCompleted = false),
                    ScheduleEntity(time = "21:00", title = "Evening Reflection & Journal", subtitle = "Evaluate wins and plan tomorrow", type = "Reflection", isCompleted = false)
                )
            )

            // Attributes
            dao.insertAttributes(
                listOf(
                    AttributeEntity("Discipline", 14, 78, "+8% this month", "Habit consistency, Morning routine, Focus sessions", "Your discipline peaked during weeks where morning routine was completed before 08:00."),
                    AttributeEntity("Focus", 16, 88, "+18% this week", "Deep work blocks, Pomodoro sessions, Zero tab switching", "Strongest focus recorded between 09:00 - 11:30 AM."),
                    AttributeEntity("Knowledge", 15, 65, "+5% this week", "Reading, Technical exploration, Documentation", "Daily 20m reading habit has compounded knowledge retention."),
                    AttributeEntity("Fitness", 12, 54, "+4% this month", "Strength workouts, Mobility routines, Recovery", "Moving workout to 18:00 stabilized weekly consistency."),
                    AttributeEntity("Mental Clarity", 13, 72, "+12% this week", "Journaling, Breathwork, Screen-free evenings", "Evening reflections correlated directly with calmer sleep scores."),
                    AttributeEntity("Consistency", 15, 82, "+15% this month", "12-day streak across core rituals", "Consistency multiplier is currently giving 1.25x XP bonus."),
                    AttributeEntity("Productivity", 14, 75, "+9% this week", "Mission execution, Task completion rate", "Task completion rate is at 84% over last 14 days.")
                )
            )

            // Achievements
            dao.insertAchievements(
                listOf(
                    AchievementEntity("first_step", "First Step", "Created and launched your first evolution goal.", true, "2026-09-10", 100, "Milestones", "star"),
                    AchievementEntity("warrior_7d", "7-Day Warrior", "Maintained a continuous 7-day habit streak.", true, "2026-09-20", 250, "Streaks", "fire"),
                    AchievementEntity("deep_worker", "Deep Worker", "Logged 10 dedicated deep focus sessions.", true, "2026-09-23", 200, "Focus", "timer"),
                    AchievementEntity("disciplined_100", "Disciplined", "Completed 100 total habit executions.", false, null, 500, "Discipline", "shield"),
                    AchievementEntity("focus_master", "Master of Focus", "Reach Focus Attribute Level 20.", false, null, 750, "Mastery", "crown")
                )
            )

            // Initial AI Insight
            dao.insertAIInsight(
                AIInsightEntity(
                    id = 1,
                    type = "Schedule",
                    title = "Energy Pattern Optimization",
                    message = "Your deep work output is 34% higher when your first focus session begins before 10:00 AM. Today's 09:00 Deep Work block is primed for maximum leverage.",
                    suggestedAction = "Start 45m Focus Block",
                    actionRoute = "focus"
                )
            )
            dao.insertAIInsight(
                AIInsightEntity(
                    id = 2,
                    type = "Habit",
                    title = "Workout Consistency Signal",
                    message = "You completed your morning workout 2 of the last 7 days. Most missed sessions occurred after late sleep. Consider shifting workout to 17:30.",
                    suggestedAction = "Shift to 17:30",
                    actionRoute = "schedule"
                )
            )

            // Starter Journal Entries
            dao.insertJournalEntry(
                JournalEntryEntity(
                    id = 1,
                    date = "Yesterday",
                    promptQuestion = "What mattered most today?",
                    content = "Completed the core architectural layout. Keeping distractions at zero during the 9am block was a huge game changer. Mind feels sharp and composed.",
                    mood = "Focused",
                    energyLevel = 5,
                    tags = "Win, Deep Work, Momentum",
                    aiSummary = "High focus session achieved without distraction; reinforced productive morning ritual.",
                    createdAt = System.currentTimeMillis() - 86400000
                )
            )
        }
    }
}
