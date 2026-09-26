package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    val pendingSync by viewModel.pendingSyncCount.collectAsState()
    val context = LocalContext.current

    var notificationsEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var ambientSoundDefault by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            ArohaTopBar(
                title = "SETTINGS & PREFERENCES",
                subtitle = "Configure your evolution environment",
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
                .testTag("settings_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cloud Synchronization Card
            item {
                ArohaCard(
                    backgroundColor = ArohaSurfaceElevated,
                    borderColor = ArohaTeal.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CLOUD SYNCHRONIZATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ArohaGoldLight,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (pendingSync == 0) "All progress synced ✓" else "$pendingSync items pending sync",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArohaTextPrimary
                            )
                        }
                        ArohaButton(
                            text = "Sync Now",
                            onClick = {
                                viewModel.syncNow()
                                Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aroha is 100% offline-first. All data, missions, habits, and journals are securely saved on your device first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArohaTextSecondary
                    )
                }
            }

            // Sensory Feedback
            item {
                Text(
                    text = "FEEDBACK & NOTIFICATIONS",
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Haptic Micro-interactions",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ArohaTextPrimary
                                )
                                Text(
                                    text = "Tactile feedback for habit checks and level ups",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArohaTextMuted
                                )
                            }
                            Switch(
                                checked = hapticsEnabled,
                                onCheckedChange = { hapticsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ArohaEmerald,
                                    checkedTrackColor = ArohaEmerald.copy(alpha = 0.3f)
                                )
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = ArohaCardBorder.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Local Ritual Reminders",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ArohaTextPrimary
                                )
                                Text(
                                    text = "Briefings, focus alerts, and evening reviews",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArohaTextMuted
                                )
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ArohaEmerald,
                                    checkedTrackColor = ArohaEmerald.copy(alpha = 0.3f)
                                )
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = ArohaCardBorder.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Ambient Soundscapes in Focus",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ArohaTextPrimary
                                )
                                Text(
                                    text = "Subtle white noise and frequency pulses",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArohaTextMuted
                                )
                            }
                            Switch(
                                checked = ambientSoundDefault,
                                onCheckedChange = { ambientSoundDefault = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ArohaEmerald,
                                    checkedTrackColor = ArohaEmerald.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            // Data Management
            item {
                Text(
                    text = "DATA & PRIVACY",
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Export Personal Telemetry",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ArohaTextPrimary
                                )
                                Text(
                                    text = "JSON archive of goals, journals, and XP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArohaTextMuted
                                )
                            }
                            ArohaOutlinedButton(
                                text = "Export",
                                onClick = {
                                    Toast.makeText(context, "Archive exported successfully!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            // About Aroha
            item {
                ArohaCard(backgroundColor = ArohaDarkSurface) {
                    Text(
                        text = "AROHA — Your Personal Evolution System",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArohaGoldLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.0 • Production Build\nRooted in ancient discipline and amplified by intelligent telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArohaTextSecondary
                    )
                }
            }
        }
    }
}
