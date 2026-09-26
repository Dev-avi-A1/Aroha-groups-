package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.local.entity.GoalEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel

@Composable
fun GoalsScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    val goals by viewModel.goalsState.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredGoals = remember(goals, selectedFilter) {
        when (selectedFilter) {
            "Active" -> goals.filter { !it.isCompleted }
            "Completed" -> goals.filter { it.isCompleted }
            else -> goals
        }
    }

    Scaffold(
        topBar = {
            ArohaTopBar(
                title = "EVOLUTION GOALS",
                subtitle = "What are you building for the long horizon?"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ArohaEmerald,
                contentColor = ArohaDeepBackground,
                shape = RoundedCornerShape(ArohaDimens.cornerMedium),
                modifier = Modifier.testTag("add_goal_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal")
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
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active", "Completed").forEach { filter ->
                    ArohaChip(
                        text = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            if (filteredGoals.isEmpty()) {
                ArohaEmptyState(
                    title = "No Goals in this Filter",
                    subtitle = "Establish ambitious milestones to steer your daily momentum.",
                    buttonText = "Create Goal",
                    onButtonClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGoals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onToggleMilestone = { idx ->
                                viewModel.toggleMilestone(goal.id, idx)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, cat, days, desc, milestones ->
                viewModel.addGoal(title, cat, days, desc, milestones)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun GoalCard(
    goal: GoalEntity,
    onToggleMilestone: (Int) -> Unit
) {
    val milestones = remember(goal.milestonesJson) {
        goal.milestonesJson.split(",").filter { it.isNotBlank() }
    }

    ArohaCard(
        backgroundColor = ArohaDarkSurface,
        borderColor = if (goal.isCompleted) ArohaEmerald.copy(alpha = 0.5f) else ArohaCardBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(ArohaDimens.cornerPill),
                    color = ArohaTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = goal.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArohaTeal,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArohaTextPrimary
                )
                if (goal.description.isNotBlank()) {
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArohaTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            ArohaProgressRing(
                progress = goal.progress / 100f,
                size = 52.dp,
                strokeWidth = 5.dp,
                progressColor = if (goal.progress >= 100) ArohaEmerald else ArohaGold
            ) {
                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArohaTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Milestones
        if (milestones.isNotEmpty()) {
            Text(
                text = "MILESTONES",
                style = MaterialTheme.typography.labelSmall,
                color = ArohaTextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            milestones.forEachIndexed { index, mItem ->
                val parts = mItem.split(":")
                val mTitle = parts.getOrNull(0) ?: ""
                val mDone = parts.getOrNull(1)?.toBoolean() ?: false

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onToggleMilestone(index) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (mDone) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = if (mDone) ArohaEmerald else ArohaTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (mDone) ArohaTextMuted else ArohaTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (goal.isCompleted) "Completed ✓" else "${goal.remainingDays} days remaining",
                style = MaterialTheme.typography.bodySmall,
                color = if (goal.isCompleted) ArohaEmerald else ArohaTextSecondary
            )
            ArohaXPBadge(xp = goal.xpReward)
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Knowledge") }
    var targetDays by remember { mutableStateOf("30") }
    var description by remember { mutableStateOf("") }
    var milestoneText by remember { mutableStateOf("") }

    val categories = listOf("Knowledge", "Focus", "Discipline", "Fitness", "Career")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArohaDarkSurface,
        title = {
            Text(
                text = "Establish New Goal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ArohaTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title", color = ArohaTextSecondary) },
                    placeholder = { Text("e.g. Master Jetpack Compose") },
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

                OutlinedTextField(
                    value = targetDays,
                    onValueChange = { targetDays = it },
                    label = { Text("Target Days", color = ArohaTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ArohaTextPrimary,
                        unfocusedTextColor = ArohaTextPrimary,
                        focusedBorderColor = ArohaTeal,
                        unfocusedBorderColor = ArohaCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = milestoneText,
                    onValueChange = { milestoneText = it },
                    label = { Text("Milestones (comma separated)", color = ArohaTextSecondary) },
                    placeholder = { Text("Basics, Deep Work, Project Launch") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ArohaTextPrimary,
                        unfocusedTextColor = ArohaTextPrimary,
                        focusedBorderColor = ArohaTeal,
                        unfocusedBorderColor = ArohaCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ArohaButton(
                text = "Establish",
                enabled = title.isNotBlank(),
                onClick = {
                    val days = targetDays.toIntOrNull() ?: 30
                    val milestones = milestoneText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onConfirm(title, category, days, description, milestones)
                }
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
