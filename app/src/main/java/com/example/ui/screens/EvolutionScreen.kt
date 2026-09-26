package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
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
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.AttributeEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel

@Composable
fun EvolutionScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    val attributes by viewModel.attributesState.collectAsState()
    val achievements by viewModel.achievementsState.collectAsState()
    var selectedAttribute by remember { mutableStateOf<AttributeEntity?>(null) }

    Scaffold(
        topBar = {
            ArohaTopBar(
                title = "PERSONAL EVOLUTION",
                subtitle = "Architecting your lifelong self-mastery tree",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = ArohaDeepBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ArohaDeepBackground)
                .testTag("evolution_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progression Map Banner
            item {
                ArohaCard(
                    backgroundColor = ArohaSurfaceElevated,
                    borderColor = ArohaTeal.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "THE ASCENT PATHWAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArohaGoldLight,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mind → Discipline → Body → Focus → Mastery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArohaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Each daily mission, completed habit, and focus block compounds into your core attributes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArohaTextSecondary
                    )
                }
            }

            // Core Attributes Section
            item {
                Text(
                    text = "CORE PILLARS (${attributes.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArohaTextMuted,
                    letterSpacing = 1.sp
                )
            }

            items(attributes) { attr ->
                AttributeRowCard(
                    attribute = attr,
                    onClick = { selectedAttribute = attr }
                )
            }

            // Achievements Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "MILESTONES & ACHIEVEMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArohaTextMuted,
                    letterSpacing = 1.sp
                )
            }

            items(achievements) { ach ->
                AchievementRowCard(achievement = ach)
            }
        }
    }

    // Attribute Details Dialog
    selectedAttribute?.let { attr ->
        AlertDialog(
            onDismissRequest = { selectedAttribute = null },
            containerColor = ArohaDarkSurface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = attr.name.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ArohaTextPrimary
                    )
                    ArohaLevelBadge(level = attr.level)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recent Growth:",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArohaTextSecondary
                        )
                        Text(
                            text = attr.recentGrowth,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArohaEmerald
                        )
                    }

                    ArohaProgressBar(
                        progress = attr.progressPercent / 100f,
                        progressColor = ArohaTeal,
                        height = 8.dp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Main Contributing Rituals:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArohaTextMuted
                    )
                    Text(
                        text = attr.contributingActivities,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArohaTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aroha AI Analysis:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArohaGoldLight
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ArohaSurfaceElevated
                    ) {
                        Text(
                            text = attr.aiInsight,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = ArohaTextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                ArohaButton(
                    text = "Close",
                    onClick = { selectedAttribute = null }
                )
            }
        )
    }
}

@Composable
private fun AttributeRowCard(
    attribute: AttributeEntity,
    onClick: () -> Unit
) {
    ArohaCard(
        backgroundColor = ArohaDarkSurface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = attribute.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArohaTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lvl ${attribute.level}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ArohaTeal
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = attribute.recentGrowth,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArohaEmerald
                )
            }

            Text(
                text = "${attribute.progressPercent}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ArohaTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        ArohaProgressBar(
            progress = attribute.progressPercent / 100f,
            progressColor = ArohaTeal,
            height = 5.dp
        )
    }
}

@Composable
private fun AchievementRowCard(achievement: AchievementEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ArohaDimens.cornerSmall),
        color = ArohaDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (achievement.isUnlocked) ArohaGold.copy(alpha = 0.4f) else ArohaCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (achievement.isUnlocked) ArohaGold.copy(alpha = 0.2f) else ArohaSurfaceElevated
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (achievement.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (achievement.isUnlocked) ArohaGold else ArohaTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked) ArohaTextPrimary else ArohaTextMuted
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArohaTextSecondary
                )
            }

            ArohaXPBadge(xp = achievement.xpReward)
        }
    }
}
