package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.domain.model.FocusPreset
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel
import java.util.Locale

@Composable
fun FocusScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    val preset by viewModel.currentFocusPreset.collectAsState()
    val remainingSeconds by viewModel.focusTimeRemainingSeconds.collectAsState()
    val isRunning by viewModel.isFocusRunning.collectAsState()
    val isCompleted by viewModel.isFocusCompleted.collectAsState()
    val selectedGoal by viewModel.selectedFocusGoal.collectAsState()
    val isSoundEnabled by viewModel.isAmbientSoundEnabled.collectAsState()
    val isDistractionFree by viewModel.isDistractionFree.collectAsState()

    val totalSeconds = preset.minutes * 60
    val progress = if (totalSeconds > 0) (remainingSeconds.toFloat() / totalSeconds.toFloat()) else 0f

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArohaDeepBackground)
            .padding(bottom = 96.dp)
            .testTag("focus_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isDistractionFree) {
            ArohaTopBar(
                title = "DEEP FOCUS CHAMBER",
                subtitle = "Channel undivided attention into mastery",
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleAmbientSound() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Ambient sound",
                            tint = if (isSoundEnabled) ArohaTeal else ArohaTextMuted
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleDistractionFree() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Distraction free",
                            tint = ArohaTextPrimary
                        )
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { viewModel.toggleDistractionFree() }
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit distraction free",
                        tint = ArohaTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Preset selector (if not running)
        AnimatedVisibility(visible = !isRunning && !isDistractionFree) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FocusPreset.values()) { p ->
                    ArohaChip(
                        text = "${p.label} (${p.minutes}m)",
                        isSelected = preset == p,
                        onClick = { viewModel.selectFocusPreset(p) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Circular Timer
        ArohaProgressRing(
            progress = progress,
            size = 250.dp,
            strokeWidth = 12.dp,
            progressColor = if (isRunning) ArohaTeal else ArohaGold
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    color = ArohaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = preset.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArohaTeal,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${preset.minutes * 2} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArohaGoldLight
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Linked Goal card
        if (!isDistractionFree) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(ArohaDimens.cornerPill)),
                color = ArohaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArohaCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = ArohaGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedGoal,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArohaTextPrimary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            IconButton(
                onClick = { viewModel.resetFocusTimer() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ArohaDarkSurface),
                enabled = remainingSeconds < totalSeconds
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset timer",
                    tint = if (remainingSeconds < totalSeconds) ArohaTextPrimary else ArohaTextMuted
                )
            }

            // Main Play/Pause Button
            Button(
                onClick = { viewModel.toggleFocusRunning() },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .testTag("focus_start_pause_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) ArohaGold else ArohaEmerald,
                    contentColor = Color(0xFF022117)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(34.dp)
                )
            }

            // Ambient Sound Toggle
            IconButton(
                onClick = { viewModel.toggleAmbientSound() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ArohaDarkSurface)
            ) {
                Icon(
                    imageVector = if (isSoundEnabled) Icons.Default.Spa else Icons.Default.MusicOff,
                    contentDescription = "Soundscape",
                    tint = if (isSoundEnabled) ArohaEmerald else ArohaTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }

    // Focus Complete Dialog
    if (isCompleted) {
        AlertDialog(
            onDismissRequest = { viewModel.resetFocusTimer() },
            containerColor = ArohaDarkSurface,
            title = {
                Text(
                    text = "FOCUS COMPLETE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ArohaGoldLight
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Outstanding session logged. You channeled ${preset.minutes} minutes of disciplined attention.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArohaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reward:",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArohaTextSecondary
                        )
                        ArohaXPBadge(xp = preset.minutes * 2)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Focus Attribute:",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArohaTextSecondary
                        )
                        Text(
                            text = "+${(preset.minutes / 10).coerceAtLeast(1)} Focus",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArohaTeal
                        )
                    }
                }
            },
            confirmButton = {
                ArohaButton(
                    text = "Done",
                    onClick = { viewModel.resetFocusTimer() }
                )
            }
        )
    }
}
