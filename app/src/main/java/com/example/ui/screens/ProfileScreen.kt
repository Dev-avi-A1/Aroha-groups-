package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    val user by viewModel.userState.collectAsState()
    val achievements by viewModel.achievementsState.collectAsState()
    val pendingSync by viewModel.pendingSyncCount.collectAsState()

    val unlockedCount = remember(achievements) {
        achievements.count { it.isUnlocked }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ArohaDeepBackground)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar + Name + Level Hero
        item {
            ArohaCard(
                backgroundColor = ArohaDarkSurface,
                borderColor = ArohaTeal.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(ArohaSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.name?.take(1) ?: "S").uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ArohaGoldLight
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user?.name ?: "Seeker",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ArohaTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ArohaLevelBadge(level = user?.level ?: 17)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Level ${user?.level ?: 17} Architect",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArohaGoldLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🔥 ${user?.streakDays ?: 12} Day Evolution Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArohaEmerald
                        )
                    }

                    IconButton(
                        onClick = { navController.navigate(Screen.Settings.route) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = ArohaTextSecondary
                        )
                    }
                }
            }
        }

        // Evolution Highlights
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileMetricTile(
                    title = "Total XP",
                    value = "${(user?.level ?: 17) * 2200 + (user?.currentXp ?: 2450)}",
                    icon = Icons.Default.Star,
                    color = ArohaGold,
                    modifier = Modifier.weight(1f)
                )
                ProfileMetricTile(
                    title = "Milestones",
                    value = "$unlockedCount / ${achievements.size}",
                    icon = Icons.Default.EmojiEvents,
                    color = ArohaEmerald,
                    modifier = Modifier.weight(1f)
                )
                ProfileMetricTile(
                    title = "Sync Status",
                    value = if (pendingSync == 0) "Synced ✓" else "$pendingSync queue",
                    icon = Icons.Default.CloudDone,
                    color = ArohaTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Navigation Menu Hub
        item {
            Text(
                text = "SYSTEM MODULES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ArohaTextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(ArohaDimens.cornerMedium),
                color = ArohaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArohaCardBorder)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.AutoGraph,
                        title = "Progress Intelligence & Telemetry",
                        subtitle = "Empirical growth charts and AI correlation",
                        onClick = { navController.navigate(Screen.Analytics.route) }
                    )
                    HorizontalDivider(color = ArohaCardBorder.copy(alpha = 0.5f))
                    ProfileMenuItem(
                        icon = Icons.Default.Share,
                        title = "Evolution Tree & Attributes",
                        subtitle = "Inspect Mind, Discipline, Body & Mastery",
                        onClick = { navController.navigate(Screen.Evolution.route) }
                    )
                    HorizontalDivider(color = ArohaCardBorder.copy(alpha = 0.5f))
                    ProfileMenuItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Habits & Consistency Matrix",
                        subtitle = "Review weekly completions and flame streaks",
                        onClick = { navController.navigate(Screen.Habits.route) }
                    )
                    HorizontalDivider(color = ArohaCardBorder.copy(alpha = 0.5f))
                    ProfileMenuItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Aroha AI Strategic Coach",
                        subtitle = "Adaptive planning and morning briefing",
                        onClick = { navController.navigate(Screen.AICoach.route) }
                    )
                    HorizontalDivider(color = ArohaCardBorder.copy(alpha = 0.5f))
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Settings & Synchronization",
                        subtitle = "Haptics, notifications, offline backup",
                        onClick = { navController.navigate(Screen.Settings.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMetricTile(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(ArohaDimens.cornerSmall),
        color = ArohaDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ArohaCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ArohaTextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ArohaTextMuted
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ArohaTeal,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = ArohaTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ArohaTextMuted
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = ArohaTextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
