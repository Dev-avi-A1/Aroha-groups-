package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArohaDao {

    // User Profile
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    // Goals
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, id ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    // Habits
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    // Habit Completions
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCompletion(completion: HabitCompletionEntity)

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    // Journal
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntry(id: Long)

    // Missions
    @Query("SELECT * FROM missions ORDER BY id DESC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE isCompleted = 0 ORDER BY id DESC LIMIT 1")
    fun getTodayMission(): Flow<MissionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Update
    suspend fun updateMission(mission: MissionEntity)

    // Attributes
    @Query("SELECT * FROM attributes")
    fun getAllAttributes(): Flow<List<AttributeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttributes(attributes: List<AttributeEntity>)

    @Update
    suspend fun updateAttribute(attribute: AttributeEntity)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // Schedule
    @Query("SELECT * FROM schedule ORDER BY time ASC")
    fun getTodaySchedule(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule WHERE date = :date OR (date = '' AND :isToday = 1) ORDER BY time ASC")
    fun getScheduleForDate(date: String, isToday: Boolean): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule ORDER BY date ASC, time ASC")
    fun getAllSchedule(): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItems(items: List<ScheduleEntity>)

    @Update
    suspend fun updateScheduleItem(item: ScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleEntity): Long

    @Delete
    suspend fun deleteScheduleItem(item: ScheduleEntity)

    // AI Insights
    @Query("SELECT * FROM ai_insights WHERE isApplied = 0 ORDER BY timestamp DESC")
    fun getActiveInsights(): Flow<List<AIInsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIInsight(insight: AIInsightEntity): Long

    @Update
    suspend fun updateAIInsight(insight: AIInsightEntity)

    // XP Transactions
    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentXpTransactions(): Flow<List<XPTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXpTransaction(transaction: XPTransactionEntity)

    // Sync Queue
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingSyncQueue(): Flow<List<SyncQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueSync(item: SyncQueueEntity): Long

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun removeSyncQueueItem(id: Long)

    @Query("DELETE FROM sync_queue")
    suspend fun clearSyncQueue()
}
