package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.local.entity.JournalEntryEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel

@Composable
fun JournalScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    val entries by viewModel.journalEntriesState.collectAsState()
    var isWritingMode by remember { mutableStateOf(true) }

    val reflectionPrompts = remember {
        listOf(
            "What mattered most today?",
            "What was your single biggest win or breakthrough?",
            "What resistance did you encounter, and how did you overcome it?",
            "What insight will reshape tomorrow's plan?"
        )
    }
    var currentPromptIndex by remember { mutableIntStateOf(0) }
    var journalText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("Focused") }
    var selectedEnergy by remember { mutableIntStateOf(4) }
    var selectedTags by remember { mutableStateOf("Reflection, Win") }

    val moods = listOf("Focused", "Zen", "Energetic", "Grateful", "Tired")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArohaDeepBackground)
            .padding(bottom = 96.dp)
            .testTag("journal_screen")
    ) {
        ArohaTopBar(
            title = "SANCTUARY JOURNAL",
            subtitle = "Private reflection compounds self-mastery",
            actions = {
                IconButton(onClick = { isWritingMode = !isWritingMode }) {
                    Icon(
                        imageVector = if (isWritingMode) Icons.Default.History else Icons.Default.Create,
                        contentDescription = "Toggle Mode",
                        tint = ArohaTeal
                    )
                }
            }
        )

        AnimatedVisibility(visible = isWritingMode) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                // Prompt Card
                item {
                    ArohaCard(
                        backgroundColor = ArohaDarkSurface,
                        borderColor = ArohaTeal.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "TODAY'S INQUIRY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArohaGoldLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${reflectionPrompts[currentPromptIndex]}\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ArohaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ArohaOutlinedButton(
                            text = "Shuffle Inquiry ⟳",
                            onClick = {
                                currentPromptIndex = (currentPromptIndex + 1) % reflectionPrompts.size
                            }
                        )
                    }
                }

                // Mood & Energy Selector
                item {
                    Column {
                        Text(
                            text = "STATE OF MIND",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArohaTextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(moods) { mood ->
                                ArohaChip(
                                    text = mood,
                                    isSelected = selectedMood == mood,
                                    onClick = { selectedMood = mood }
                                )
                            }
                        }
                    }
                }

                // Energy Slider
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENERGY LEVEL: $selectedEnergy/5",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArohaTextMuted,
                            letterSpacing = 1.sp
                        )
                    }
                    Slider(
                        value = selectedEnergy.toFloat(),
                        onValueChange = { selectedEnergy = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = ArohaEmerald,
                            activeTrackColor = ArohaEmerald,
                            inactiveTrackColor = ArohaSurfaceElevated
                        )
                    )
                }

                // Writing Area
                item {
                    OutlinedTextField(
                        value = journalText,
                        onValueChange = { journalText = it },
                        placeholder = {
                            Text(
                                "Put honest thoughts to page. The unexamined day slips away...",
                                color = ArohaTextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp)
                            .testTag("journal_input"),
                        shape = RoundedCornerShape(ArohaDimens.cornerMedium),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArohaTextPrimary,
                            unfocusedTextColor = ArohaTextPrimary,
                            focusedBorderColor = ArohaTeal,
                            unfocusedBorderColor = ArohaCardBorder,
                            focusedContainerColor = ArohaDarkSurface,
                            unfocusedContainerColor = ArohaDarkSurface
                        )
                    )
                }

                // Save Action
                item {
                    ArohaButton(
                        text = "Seal Reflection (+20 XP)",
                        enabled = journalText.isNotBlank(),
                        onClick = {
                            viewModel.saveJournalEntry(
                                content = journalText,
                                mood = selectedMood,
                                energyLevel = selectedEnergy,
                                prompt = reflectionPrompts[currentPromptIndex],
                                tags = selectedTags
                            )
                            journalText = ""
                            isWritingMode = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        AnimatedVisibility(visible = !isWritingMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAST REFLECTIONS (${entries.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArohaTextMuted,
                        letterSpacing = 1.sp
                    )
                    ArohaOutlinedButton(
                        text = "New Entry +",
                        onClick = { isWritingMode = true }
                    )
                }

                if (entries.isEmpty()) {
                    ArohaEmptyState(
                        title = "No Journal Entries",
                        subtitle = "Your first reflection starts today.",
                        buttonText = "Write Entry",
                        onButtonClick = { isWritingMode = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(entries, key = { it.id }) { entry ->
                            JournalEntryCard(entry = entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntryEntity) {
    ArohaCard(
        backgroundColor = ArohaDarkSurface,
        borderColor = ArohaCardBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.date,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ArohaTeal
            )
            Surface(
                shape = RoundedCornerShape(ArohaDimens.cornerPill),
                color = ArohaEmerald.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${entry.mood} • ${entry.energyLevel}/5 ⚡",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = ArohaEmeraldLight
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.promptQuestion,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = ArohaGoldLight
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.content,
            style = MaterialTheme.typography.bodyMedium,
            color = ArohaTextPrimary
        )

        if (entry.aiSummary.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ArohaSurfaceElevated
            ) {
                Text(
                    text = "AI Takeaway: ${entry.aiSummary}",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArohaTextSecondary
                )
            }
        }
    }
}
