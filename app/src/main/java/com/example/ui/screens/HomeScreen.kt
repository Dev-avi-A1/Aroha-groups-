package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.local.entity.*
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.DayOption
import com.example.util.TimeUtils
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel,
    onStartFocus: (String) -> Unit = {}
) {
    val uiState by dashboardViewModel.uiState.collectAsState()

    HomeScreenContent(
        navController = navController,
        user = uiState.user,
        mission = uiState.todayMission,
        schedule = uiState.scheduleItems,
        attributes = uiState.attributes,
        insights = uiState.insights,
        greeting = uiState.greeting,
        systemTimeZone = uiState.timeZone,
        selectedDate = uiState.selectedDate,
        calendarDays = uiState.calendarDays,
        allScheduleItems = uiState.allScheduleItems,
        onSelectDate = { dashboardViewModel.setSelectedDate(it) },
        onAddSchedule = { time, title, subtitle, type, date ->
            dashboardViewModel.addScheduleItem(time, title, subtitle, type, date)
        },
        onBulkAddSchedule = { items ->
            dashboardViewModel.bulkAddSchedule(items)
        },
        onApplySchedulePreset = { preset, targetDate ->
            dashboardViewModel.applyPresetSchedule(preset, targetDate)
        },
        onParseAndBulkAdd = { rawText, targetDate ->
            dashboardViewModel.parseAndBulkAdd(rawText, targetDate)
        },
        onDeleteScheduleItem = { dashboardViewModel.deleteScheduleItem(it) },
        onToggleMissionTask = { dashboardViewModel.toggleMissionTask(it) },
        onToggleScheduleItem = { dashboardViewModel.toggleScheduleItem(it) },
        onApplyInsight = { dashboardViewModel.applyInsight(it) },
        onStartFocus = onStartFocus
    )
}

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    val user by viewModel.userState.collectAsState()
    val mission by viewModel.todayMissionState.collectAsState()
    val schedule by viewModel.scheduleState.collectAsState()
    val attributes by viewModel.attributesState.collectAsState()
    val insights by viewModel.insightsState.collectAsState()
    var selectedDate by remember { mutableStateOf(TimeUtils.getTodayDateString()) }

    HomeScreenContent(
        navController = navController,
        user = user,
        mission = mission,
        schedule = schedule,
        attributes = attributes,
        insights = insights,
        greeting = TimeUtils.getGreeting(),
        selectedDate = selectedDate,
        onSelectDate = { selectedDate = it },
        onAddSchedule = { time, title, subtitle, type, date ->
            viewModel.addScheduleItem(time, title, subtitle, type, date)
        },
        onBulkAddSchedule = { viewModel.bulkAddSchedule(it) },
        onDeleteScheduleItem = { viewModel.deleteScheduleItem(it) },
        onToggleMissionTask = { viewModel.toggleMissionTask(it) },
        onToggleScheduleItem = { viewModel.toggleScheduleItem(it) },
        onApplyInsight = { viewModel.applyInsight(it) },
        onStartFocus = { title ->
            viewModel.setSelectedFocusGoal(title)
            navController.navigate(Screen.Focus.route)
        }
    )
}

@Composable
fun HomeScreenContent(
    navController: NavController,
    user: UserEntity?,
    mission: MissionEntity?,
    schedule: List<ScheduleEntity>,
    attributes: List<AttributeEntity>,
    insights: List<AIInsightEntity>,
    greeting: String = TimeUtils.getGreeting(),
    systemTimeZone: TimeZone = remember { TimeZone.getDefault() },
    selectedDate: String = TimeUtils.getTodayDateString(),
    calendarDays: List<DayOption> = remember { TimeUtils.getCalendarDays() },
    allScheduleItems: List<ScheduleEntity> = emptyList(),
    onSelectDate: (String) -> Unit = {},
    onAddSchedule: (time: String, title: String, subtitle: String, type: String, date: String) -> Unit = { _, _, _, _, _ -> },
    onBulkAddSchedule: (List<ScheduleEntity>) -> Unit = {},
    onApplySchedulePreset: (presetName: String, targetDate: String) -> Unit = { _, _ -> },
    onParseAndBulkAdd: (rawText: String, targetDate: String) -> Unit = { _, _ -> },
    onDeleteScheduleItem: (ScheduleEntity) -> Unit = {},
    onToggleMissionTask: (Int) -> Unit,
    onToggleScheduleItem: (ScheduleEntity) -> Unit,
    onApplyInsight: (AIInsightEntity) -> Unit,
    onStartFocus: (String) -> Unit
) {
    val currentDateStr = remember(systemTimeZone) {
        TimeUtils.getFormattedCurrentDate(systemTimeZone)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showBulkDialog by remember { mutableStateOf(false) }
    var showCalendarJumpDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddScheduleDialog(
            initialDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onConfirm = { time, title, subtitle, type, date ->
                onAddSchedule(time, title, subtitle, type, date)
                showAddDialog = false
            }
        )
    }

    if (showBulkDialog) {
        BulkAddScheduleDialog(
            targetDate = selectedDate,
            onDismiss = { showBulkDialog = false },
            onApplyPreset = { preset, date ->
                onApplySchedulePreset(preset, date)
                showBulkDialog = false
            },
            onParseAndAdd = { rawText, date ->
                onParseAndBulkAdd(rawText, date)
                showBulkDialog = false
            }
        )
    }

    if (showCalendarJumpDialog) {
        CalendarJumpDialog(
            currentDateStr = selectedDate,
            onDismiss = { showCalendarJumpDialog = false },
            onDateSelected = { date ->
                onSelectDate(date)
                showCalendarJumpDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ArohaDeepBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Header: Greeting + Date + Streak + Notification
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$greeting, ${user?.name ?: "Seeker"}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ArohaTextPrimary,
                            modifier = Modifier.testTag("dashboard_greeting")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentDateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = ArohaTextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Streak Pill
                        Surface(
                            shape = RoundedCornerShape(ArohaDimens.cornerPill),
                            color = ArohaGold.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArohaGold.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { navController.navigate(Screen.Habits.route) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🔥", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${user?.streakDays ?: 12}d",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArohaGoldLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Avatar / Profile icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ArohaSurfaceElevated)
                                .clickable { navController.navigate(Screen.Profile.route) }
                                .testTag("profile_avatar_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = ArohaTeal
                            )
                        }
                    }
                }
            }
        }

        // 2. Level & XP Progress Card
        item {
            val lvl = user?.level ?: 17
            val curXp = user?.currentXp ?: 2450
            val reqXp = user?.requiredXp ?: 3000
            val xpProgress = (curXp.toFloat() / reqXp.toFloat()).coerceIn(0f, 1f)

            ArohaCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("level_card"),
                onClick = { navController.navigate(Screen.Evolution.route) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ArohaLevelBadge(level = lvl)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LEVEL $lvl ARCHITECT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArohaGoldLight,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${reqXp - curXp} XP to Level ${lvl + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ArohaTextSecondary
                            )
                        }
                    }

                    Text(
                        text = "$curXp / $reqXp XP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ArohaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                ArohaProgressBar(
                    progress = xpProgress,
                    progressColor = ArohaGold,
                    height = 7.dp
                )
            }
        }

        // 3. Today's Evolution Attributes Horizontal Scroll
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S EVOLUTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArohaTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "View Map →",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArohaTeal,
                        modifier = Modifier.clickable { navController.navigate(Screen.Evolution.route) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(attributes) { attr ->
                        Surface(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(ArohaDimens.cornerMedium))
                                .clickable { navController.navigate(Screen.Evolution.route) },
                            shape = RoundedCornerShape(ArohaDimens.cornerMedium),
                            color = ArohaDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArohaCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = attr.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArohaTextSecondary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Lvl ${attr.level}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ArohaTextPrimary
                                    )
                                    Text(
                                        text = "${attr.progressPercent}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ArohaEmerald
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                ArohaProgressBar(
                                    progress = attr.progressPercent / 100f,
                                    progressColor = ArohaTeal,
                                    height = 4.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Daily Mission Card
        item {
            mission?.let { m ->
                val taskList = remember(m.tasksJson) {
                    m.tasksJson.split(",").filter { it.isNotBlank() }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "DAILY MISSION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArohaTextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ArohaCard(
                        backgroundColor = ArohaSurfaceElevated,
                        borderColor = ArohaTeal.copy(alpha = 0.35f),
                        modifier = Modifier.testTag("mission_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = m.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArohaTextPrimary
                                )
                                Text(
                                    text = m.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArohaTextSecondary
                                )
                            }
                            ArohaXPBadge(xp = m.xpReward)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Task List Checkbox Items
                        taskList.forEachIndexed { index, taskItem ->
                            val parts = taskItem.split(":")
                            val title = parts.getOrNull(0) ?: ""
                            val isDone = parts.getOrNull(1)?.toBoolean() ?: false

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleMissionTask(index) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    tint = if (isDone) ArohaEmerald else ArohaTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDone) ArohaTextMuted else ArohaTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${m.progressCount} / ${taskList.size} completed",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArohaTextSecondary
                            )
                            if (m.isCompleted) {
                                Text(
                                    text = "MISSION COMPLETE ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ArohaEmerald
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Quick Actions Row
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "QUICK COMMANDS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArohaTextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Timer,
                        label = "Focus",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Focus.route) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.CheckCircle,
                        label = "Habits",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Habits.route) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Flag,
                        label = "Goals",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Goals.route) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.AutoAwesome,
                        label = "AI Coach",
                        modifier = Modifier.weight(1f),
                        accentColor = ArohaGold,
                        onClick = { navController.navigate(Screen.AICoach.route) }
                    )
                }
            }
        }

        // 6. Calendar Date Selector & Schedule Timeline
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Calendar Strip Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIMELINE CALENDAR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArohaTextMuted,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = { showCalendarJumpDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pick Date",
                            tint = ArohaTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Calendar Day Strip
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(calendarDays) { day ->
                        val isSelected = (day.dateStr == selectedDate)
                        val hasEvents = allScheduleItems.any { it.date == day.dateStr || (it.date.isBlank() && day.isToday) }
                        CalendarDayPill(
                            day = day,
                            isSelected = isSelected,
                            hasEvents = hasEvents,
                            onClick = { onSelectDate(day.dateStr) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Schedule Header with Action Buttons (Bulk Add & + Add)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedDate == TimeUtils.getTodayDateString(systemTimeZone)) "TODAY'S SCHEDULE" else "SCHEDULE FOR ${TimeUtils.formatDateForDisplay(selectedDate, systemTimeZone).uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArohaTextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${schedule.count { it.isCompleted }}/${schedule.size} completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArohaTextSecondary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bulk Add button
                        Surface(
                            shape = RoundedCornerShape(ArohaDimens.cornerPill),
                            color = ArohaDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArohaGold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(ArohaDimens.cornerPill))
                                .clickable { showBulkDialog = true }
                                .testTag("btn_bulk_add_schedule")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = ArohaGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Bulk Add",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ArohaGold
                                )
                            }
                        }

                        // Add Single Item Button
                        Surface(
                            shape = RoundedCornerShape(ArohaDimens.cornerPill),
                            color = ArohaTeal.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArohaTeal),
                            modifier = Modifier
                                .clip(RoundedCornerShape(ArohaDimens.cornerPill))
                                .clickable { showAddDialog = true }
                                .testTag("btn_add_schedule")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = ArohaTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ArohaTeal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Schedule Item List or Empty State
                if (schedule.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ArohaDimens.cornerSmall)),
                        shape = RoundedCornerShape(ArohaDimens.cornerSmall),
                        color = ArohaDarkSurface.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArohaCardBorder.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EventNote,
                                contentDescription = null,
                                tint = ArohaTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No schedule planned for this day",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ArohaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use Smart Presets to plan an elite day or add specific tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = ArohaTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ArohaButton(
                                    text = "Apply Smart Preset",
                                    containerColor = ArohaGold,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showBulkDialog = true }
                                )
                                ArohaButton(
                                    text = "+ Add Task",
                                    containerColor = ArohaTeal,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showAddDialog = true }
                                )
                            }
                        }
                    }
                } else {
                    schedule.forEach { item ->
                        ScheduleRowItem(
                            item = item,
                            onToggle = { onToggleScheduleItem(item) },
                            onDelete = { onDeleteScheduleItem(item) },
                            onStartFocus = {
                                onStartFocus(item.title)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 7. AI Insight Action Card
        item {
            val activeInsight = insights.firstOrNull()
            if (activeInsight != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "STRATEGIC AI INSIGHT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArohaTextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ArohaCard(
                        backgroundColor = ArohaDarkSurface,
                        borderColor = ArohaGold.copy(alpha = 0.35f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = ArohaGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeInsight.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArohaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = activeInsight.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArohaTextSecondary
                        )

                        activeInsight.suggestedAction?.let { actionText ->
                            Spacer(modifier = Modifier.height(12.dp))
                            ArohaButton(
                                text = actionText,
                                containerColor = ArohaGold,
                                onClick = {
                                    onApplyInsight(activeInsight)
                                    if (activeInsight.actionRoute == "focus") {
                                        navController.navigate(Screen.Focus.route)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayPill(
    day: DayOption,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) ArohaTeal else ArohaDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) ArohaTeal else if (day.isToday) ArohaGold else ArohaCardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.dayOfWeek,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.Black else if (day.isToday) ArohaGold else ArohaTextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = day.dayOfMonth,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else ArohaTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasEvents) {
                            if (isSelected) Color.Black else ArohaEmerald
                        } else {
                            Color.Transparent
                        }
                    )
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = ArohaTeal,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(ArohaDimens.cornerSmall))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ArohaDimens.cornerSmall),
        color = ArohaDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ArohaCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ArohaTextPrimary
            )
        }
    }
}

@Composable
private fun ScheduleRowItem(
    item: ScheduleEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit = {},
    onStartFocus: () -> Unit
) {
    val typeColor = when (item.type) {
        "Deep Work" -> ArohaTeal
        "Routine" -> ArohaGold
        "Study" -> ArohaEmerald
        "Workout" -> ArohaError
        "Reflection" -> AttrKnowledge
        else -> ArohaTeal
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ArohaDimens.cornerSmall)),
        shape = RoundedCornerShape(ArohaDimens.cornerSmall),
        color = if (item.isCompleted) ArohaDarkSurface.copy(alpha = 0.6f) else ArohaDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isCompleted) ArohaCardBorder.copy(alpha = 0.5f) else ArohaCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Toggle schedule",
                    tint = if (item.isCompleted) ArohaEmerald else ArohaTextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.time,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = typeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.type,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.isCompleted) ArohaTextMuted else ArohaTextPrimary
                    )
                }
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArohaTextMuted
                )
            }

            if (!item.isCompleted && item.type == "Deep Work") {
                IconButton(
                    onClick = onStartFocus,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Focus",
                        tint = ArohaEmerald
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete item",
                    tint = ArohaTextMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ArohaInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        placeholder = if (placeholder.isNotBlank()) { { Text(placeholder, color = ArohaTextMuted) } } else null,
        modifier = modifier,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ArohaTextPrimary,
            unfocusedTextColor = ArohaTextPrimary,
            focusedBorderColor = ArohaTeal,
            unfocusedBorderColor = ArohaCardBorder,
            focusedLabelColor = ArohaTeal,
            unfocusedLabelColor = ArohaTextMuted,
            focusedContainerColor = ArohaDeepBackground,
            unfocusedContainerColor = ArohaDeepBackground
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

// -------------------------------------------------------------------------
// DIALOGS: Add Schedule, Bulk Add Presets/Multi-line, and Calendar Jump
// -------------------------------------------------------------------------

@Composable
fun AddScheduleDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (time: String, title: String, subtitle: String, type: String, date: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("09:00") }
    var date by remember { mutableStateOf(initialDate) }
    var selectedType by remember { mutableStateOf("Deep Work") }

    val types = listOf("Deep Work", "Routine", "Study", "Workout", "Reflection")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArohaDarkSurface,
        title = {
            Text(
                text = "Schedule New Event",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArohaTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ArohaInputField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title (e.g. System Architecture)",
                    modifier = Modifier.fillMaxWidth()
                )

                ArohaInputField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = "Details / Focus Notes",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArohaInputField(
                        value = time,
                        onValueChange = { time = it },
                        label = "Time (HH:mm)",
                        modifier = Modifier.weight(1f)
                    )
                    ArohaInputField(
                        value = date,
                        onValueChange = { date = it },
                        label = "Date (YYYY-MM-DD)",
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Category Type",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArohaTextMuted
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(types) { t ->
                        val isSel = (t == selectedType)
                        Surface(
                            shape = RoundedCornerShape(ArohaDimens.cornerPill),
                            color = if (isSel) ArohaTeal else ArohaDeepBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) ArohaTeal else ArohaCardBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(ArohaDimens.cornerPill))
                                .clickable { selectedType = t }
                        ) {
                            Text(
                                text = t,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.Black else ArohaTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(time, title, subtitle.ifBlank { "Scheduled session" }, selectedType, date)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArohaTeal)
            ) {
                Text("Schedule", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ArohaTextMuted)
            }
        }
    )
}

@Composable
fun BulkAddScheduleDialog(
    targetDate: String,
    onDismiss: () -> Unit,
    onApplyPreset: (preset: String, date: String) -> Unit,
    onParseAndAdd: (rawText: String, date: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Presets, 1 = Multi-Line Paste
    var selectedDate by remember { mutableStateOf(targetDate) }
    var multiLineInput by remember { mutableStateOf("08:00 Morning Mobility - Dynamic stretches\n09:30 Deep Architecture - Core engine\n13:30 Execution Sprint - Unit tests\n17:00 Strength Session - Compound lifts\n21:00 Day Reflection - Sanctuary log") }

    val presets = listOf(
        "Elite Deep Work Day" to "6-part high performance workflow: Morning protocol, 2 deep work sprints, fitness & evening reflection.",
        "Intense Study & Mastery" to "5-part academic & coding mastery: Theory, sandbox implementation, research paper review & active recall.",
        "Sprint Execution Day" to "5-part rapid delivery: Daily calibration, UI build, code review, integrations & daily retro.",
        "Weekend Wellness & Recharge" to "4-part restorative cycle: Nature walk, creative writing, high-leverage learning & yoga."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArohaDarkSurface,
        title = {
            Text(
                text = "Bulk Schedule Planner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArohaTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Target Date Input
                ArohaInputField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = "Target Date (YYYY-MM-DD)",
                    modifier = Modifier.fillMaxWidth()
                )

                // Tabs: Presets vs Multi-Line Paste
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ArohaDeepBackground,
                    contentColor = ArohaTeal
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Smart Presets", style = MaterialTheme.typography.labelMedium) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Multi-Line Paste", style = MaterialTheme.typography.labelMedium) }
                    )
                }

                if (selectedTab == 0) {
                    // Presets List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presets.forEach { (name, desc) ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onApplyPreset(name, selectedDate) },
                                shape = RoundedCornerShape(8.dp),
                                color = ArohaDeepBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ArohaCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ArohaGold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ArohaTextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = "Apply",
                                        tint = ArohaGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Multi-Line Paste
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Enter one task per line (e.g. HH:mm Title - Notes):",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArohaTextMuted
                        )
                        OutlinedTextField(
                            value = multiLineInput,
                            onValueChange = { multiLineInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ArohaTextPrimary,
                                unfocusedTextColor = ArohaTextPrimary,
                                focusedBorderColor = ArohaTeal,
                                unfocusedBorderColor = ArohaCardBorder,
                                focusedContainerColor = ArohaDeepBackground,
                                unfocusedContainerColor = ArohaDeepBackground
                            ),
                            placeholder = {
                                Text("09:00 Deep Work\n11:00 System Review\n14:00 Build Sprint", color = ArohaTextMuted)
                            }
                        )

                        Button(
                            onClick = {
                                if (multiLineInput.isNotBlank()) {
                                    onParseAndAdd(multiLineInput, selectedDate)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArohaTeal),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Parse & Add All Items", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ArohaTextMuted)
            }
        }
    )
}

@Composable
fun CalendarJumpDialog(
    currentDateStr: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var customDate by remember { mutableStateOf(currentDateStr) }
    val timeZone = remember { TimeZone.getDefault() }

    val quickOptions = remember {
        val cal = Calendar.getInstance(timeZone)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { this.timeZone = timeZone }
        val d1 = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val d2 = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val d3 = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 5)
        val d4 = sdf.format(cal.time)

        listOf(
            "Today" to d1,
            "Tomorrow" to d2,
            "In 2 Days" to d3,
            "Next Week" to d4
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArohaDarkSurface,
        title = {
            Text(
                text = "Jump to Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArohaTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Quick Select:",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArohaTextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickOptions.forEach { (label, date) ->
                        Surface(
                            shape = RoundedCornerShape(ArohaDimens.cornerPill),
                            color = if (customDate == date) ArohaTeal else ArohaDeepBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (customDate == date) ArohaTeal else ArohaCardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(ArohaDimens.cornerPill))
                                .clickable {
                                    customDate = date
                                    onDateSelected(date)
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = if (customDate == date) Color.Black else ArohaTextPrimary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                ArohaInputField(
                    value = customDate,
                    onValueChange = { customDate = it },
                    label = "Custom Date (YYYY-MM-DD)",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customDate.isNotBlank()) {
                        onDateSelected(customDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArohaTeal)
            ) {
                Text("Select", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ArohaTextMuted)
            }
        }
    )
}

