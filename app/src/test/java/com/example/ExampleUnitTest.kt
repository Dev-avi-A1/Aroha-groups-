package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLevelProgressionCalculation() {
        val currentLvl = 17
        val reqXp = 1000 + (currentLvl * 150)
        assertEquals(3550, reqXp)
    }

    @Test
    fun testFocusXpRewardFormula() {
        val durationMinutes = 45
        val xp = (durationMinutes * 2).coerceAtLeast(20)
        assertEquals(90, xp)
    }

    @Test
    fun testHabitStreakIncrement() {
        val initialStreak = 12
        val newStreak = initialStreak + 1
        assertEquals(13, newStreak)
    }

    @Test
    fun testMissionCompletionEvaluation() {
        val tasks = listOf("Focus 30m:true", "Workout:true", "Journal:true")
        val completedCount = tasks.count { it.endsWith(":true") }
        assertEquals(3, completedCount)
        assertTrue(completedCount == tasks.size)
    }

    @Test
    fun testBottomNavItemsInitialization() {
        val items = com.example.ui.navigation.Screen.bottomNavItems
        assertEquals(5, items.size)
        items.forEach { screen ->
            org.junit.Assert.assertNotNull(screen)
            org.junit.Assert.assertNotNull(screen.route)
            assertTrue(screen.route.isNotBlank())
            org.junit.Assert.assertNotNull(screen.selectedIcon)
            org.junit.Assert.assertNotNull(screen.unselectedIcon)
        }

        // Verify Dashboard, Journal, and Coaching are all present
        assertTrue(items.any { it.route == com.example.ui.navigation.Screen.Dashboard.route })
        assertTrue(items.any { it.route == com.example.ui.navigation.Screen.Journal.route })
        assertTrue(items.any { it.route == com.example.ui.navigation.Screen.Coaching.route })
    }

    @Test
    fun testDashboardJournalCoachingRoutes() {
        assertEquals("dashboard", com.example.ui.navigation.Screen.Dashboard.route)
        assertEquals("journal", com.example.ui.navigation.Screen.Journal.route)
        assertEquals("coaching", com.example.ui.navigation.Screen.Coaching.route)
    }

    @Test
    fun testDashboardUiStateCalculation() {
        val schedule = listOf(
            com.example.data.local.entity.ScheduleEntity(1, "08:00", "Morning", "Routine", "Habit", true),
            com.example.data.local.entity.ScheduleEntity(2, "09:00", "Deep Work", "Kotlin", "Deep Work", false)
        )
        val state = com.example.ui.viewmodel.DashboardUiState(
            scheduleItems = schedule
        )
        assertEquals(1, state.completedScheduleCount)
        assertEquals(2, state.totalScheduleCount)
    }

    @Test
    fun testGreetingByHour() {
        // Morning: 5..11
        org.junit.Assert.assertEquals("Good morning", com.example.util.TimeUtils.getGreetingForHour(5))
        org.junit.Assert.assertEquals("Good morning", com.example.util.TimeUtils.getGreetingForHour(9))
        org.junit.Assert.assertEquals("Good morning", com.example.util.TimeUtils.getGreetingForHour(11))

        // Afternoon: 12..16
        org.junit.Assert.assertEquals("Good afternoon", com.example.util.TimeUtils.getGreetingForHour(12))
        org.junit.Assert.assertEquals("Good afternoon", com.example.util.TimeUtils.getGreetingForHour(14))
        org.junit.Assert.assertEquals("Good afternoon", com.example.util.TimeUtils.getGreetingForHour(16))

        // Evening: 17..21
        org.junit.Assert.assertEquals("Good evening", com.example.util.TimeUtils.getGreetingForHour(17))
        org.junit.Assert.assertEquals("Good evening", com.example.util.TimeUtils.getGreetingForHour(19))
        org.junit.Assert.assertEquals("Good evening", com.example.util.TimeUtils.getGreetingForHour(21))

        // Night: 22..23, 0..4
        org.junit.Assert.assertEquals("Good night", com.example.util.TimeUtils.getGreetingForHour(22))
        org.junit.Assert.assertEquals("Good night", com.example.util.TimeUtils.getGreetingForHour(23))
        org.junit.Assert.assertEquals("Good night", com.example.util.TimeUtils.getGreetingForHour(0))
        org.junit.Assert.assertEquals("Good night", com.example.util.TimeUtils.getGreetingForHour(3))
    }

    @Test
    fun testGreetingWithDifferentTimeZones() {
        // Create a fixed reference timestamp (e.g., 2026-09-25 14:00 UTC)
        val calUtc = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(java.util.Calendar.YEAR, 2026)
            set(java.util.Calendar.MONTH, java.util.Calendar.SEPTEMBER)
            set(java.util.Calendar.DAY_OF_MONTH, 25)
            set(java.util.Calendar.HOUR_OF_DAY, 14) // 14:00 UTC
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }
        val timestamp = calUtc.timeInMillis

        // UTC: 14:00 -> Afternoon
        val utcGreeting = com.example.util.TimeUtils.getGreeting(
            timeZone = java.util.TimeZone.getTimeZone("UTC"),
            currentTimeMillis = timestamp
        )
        org.junit.Assert.assertEquals("Good afternoon", utcGreeting)

        // Tokyo (UTC+9): 23:00 -> Night
        val tokyoGreeting = com.example.util.TimeUtils.getGreeting(
            timeZone = java.util.TimeZone.getTimeZone("Asia/Tokyo"),
            currentTimeMillis = timestamp
        )
        org.junit.Assert.assertEquals("Good night", tokyoGreeting)

        // New York (UTC-4 in Daylight Saving): 10:00 -> Morning
        val nyGreeting = com.example.util.TimeUtils.getGreeting(
            timeZone = java.util.TimeZone.getTimeZone("America/New_York"),
            currentTimeMillis = timestamp
        )
        org.junit.Assert.assertEquals("Good morning", nyGreeting)

        // Honolulu (UTC-10): 04:00 -> Night
        val honoluluGreeting = com.example.util.TimeUtils.getGreeting(
            timeZone = java.util.TimeZone.getTimeZone("Pacific/Honolulu"),
            currentTimeMillis = timestamp
        )
        org.junit.Assert.assertEquals("Good night", honoluluGreeting)
    }

    @Test
    fun testFullGreetingAndDashboardState() {
        val fullGreeting = com.example.util.TimeUtils.getFullGreeting(
            userName = "Alex",
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        )
        assertTrue(fullGreeting.endsWith(", Alex"))

        val fallbackGreeting = com.example.util.TimeUtils.getFullGreeting(
            userName = null,
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        )
        assertTrue(fallbackGreeting.endsWith(", Seeker"))

        val state = com.example.ui.viewmodel.DashboardUiState(
            user = com.example.data.local.entity.UserEntity(
                id = 1,
                name = "Kai",
                level = 2,
                streakDays = 5,
                isOnboarded = true
            ),
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        )
        org.junit.Assert.assertNotNull(state.greeting)
        assertTrue(state.personalizedGreeting.contains("Kai"))
    }
}
