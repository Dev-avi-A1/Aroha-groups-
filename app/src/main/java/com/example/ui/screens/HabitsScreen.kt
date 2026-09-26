package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.local.entity.HabitEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel

@Composable
fun HabitsScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    val habits by viewModel.habitsState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val completedCount = habits.count { it.completedToday }
    val totalCount = habits.size
    val completionPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    Scaffold(
        topBar = {
            ArohaTopBar(
                title = "HABIT MATRIX",
                subtitle = "Small disciplined actions compound into identity",
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ArohaEmerald,
                contentColor = ArohaDeepBackground,
                shape = RoundedCornerShape(ArohaDimens.cornerMedium),
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        containerColor = ArohaDeepBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ArohaDeepBackground)
        ) {
            // Consistency summary card
            ArohaCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                backgroundColor = ArohaDarkSurface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S CONSISTENCY",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArohaTextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$completedCount of $totalCount completed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ArohaTextPrimary
                        )
                    }
                    Text(
                        text = "$completionPercent%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (completionPercent >= 80) ArohaEmerald else ArohaGold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                ArohaProgressBar(
                    progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f,
                    progressColor = ArohaEmerald,
                    height = 6.dp
                )
            }

            if (habits.isEmpty()) {
                ArohaEmptyState(
                    title = "No Habits Tracked",
                    subtitle = "Small actions become identity. Create your first daily ritual.",
                    buttonText = "Create Habit",
                    onButtonClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onToggle = {
                                performHaptic(context)
                                viewModel.toggleHabit(habit.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, freq ->
                viewModel.addHabit(name, category, freq)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HabitCard(
    habit: HabitEntity,
    onToggle: () -> Unit
) {
    val checkBgColor by animateColorAsState(
        targetValue = if (habit.completedToday) ArohaEmerald else ArohaSurfaceElevated,
        label = "habitCheckBg"
    )

    ArohaCard(
        backgroundColor = ArohaDarkSurface,
        borderColor = if (habit.completedToday) ArohaEmerald.copy(alpha = 0.4f) else ArohaCardBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Check button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(checkBgColor)
                    .clickable(onClick = onToggle)
                    .testTag("habit_toggle_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (habit.completedToday) Icons.Filled.Check else Icons.Outlined.Check,
                    contentDescription = "Toggle",
                    tint = if (habit.completedToday) Color(0xFF022117) else ArohaTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (habit.completedToday) ArohaTextMuted else ArohaTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔥 ${habit.currentStreak} DAYS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArohaGoldLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${habit.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArohaTextSecondary
                    )
                }
            }

            ArohaXPBadge(xp = habit.xpReward)
        }
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Discipline") }
    var frequency by remember { mutableStateOf("Daily") }

    val categories = listOf("Discipline", "Focus", "Knowledge", "Fitness", "Mind")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArohaDarkSurface,
        title = {
            Text(
                text = "New Daily Habit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ArohaTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name", color = ArohaTextSecondary) },
                    placeholder = { Text("e.g. Read 20 minutes") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ArohaTextPrimary,
                        unfocusedTextColor = ArohaTextPrimary,
                        focusedBorderColor = ArohaTeal,
                        unfocusedBorderColor = ArohaCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArohaTextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        ArohaChip(
                            text = cat,
                            isSelected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }
            }
        },
        confirmButton = {
            ArohaButton(
                text = "Create Habit",
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name, category, frequency) }
            )
        },
        dismissButton = {
            ArohaOutlinedButton(
                text = "Cancel",
                onClick = { onDismiss() }
            )
        }
    )
}

private fun performHaptic(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(40)
            }
        }
    } catch (e: Exception) {
        // Safe haptic fallback
    }
}
