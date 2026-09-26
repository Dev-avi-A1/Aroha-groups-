package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.example.domain.model.CoachMessage
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel
import kotlinx.coroutines.launch

@Composable
fun AICoachScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    val messages by viewModel.coachMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ArohaTopBar(
                title = "AROHA STRATEGIC AI",
                subtitle = "Adaptive personal evolution advisor",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            // Chat Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(ArohaDimens.cornerMedium),
                color = ArohaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArohaCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = {
                            Text(
                                "Ask advice or adapt plan...",
                                color = ArohaTextMuted,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = ArohaTextPrimary,
                            unfocusedTextColor = ArohaTextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_coach_input")
                    )

                    IconButton(
                        onClick = {
                            if (inputQuery.isNotBlank()) {
                                val text = inputQuery
                                inputQuery = ""
                                viewModel.sendCoachMessage(text)
                            }
                        },
                        enabled = inputQuery.isNotBlank() && !isThinking,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputQuery.isNotBlank() && !isThinking) ArohaEmerald else ArohaTextMuted
                        )
                    }
                }
            }
        },
        containerColor = ArohaDeepBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ArohaDeepBackground)
                .testTag("ai_coach_screen")
        ) {
            // Quick Strategic Prompts Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ArohaChip(
                        text = "🌅 Morning Briefing",
                        isSelected = false,
                        onClick = { viewModel.requestMorningBriefing() }
                    )
                }
                item {
                    ArohaChip(
                        text = "🌙 Evening Review",
                        isSelected = false,
                        onClick = { viewModel.requestEveningReview() }
                    )
                }
                item {
                    ArohaChip(
                        text = "⚡ Fix Workout Rut",
                        isSelected = false,
                        onClick = { viewModel.sendCoachMessage("I keep missing my morning workouts. Analyze and suggest schedule change.") }
                    )
                }
                item {
                    ArohaChip(
                        text = "📚 Exam in 30 Days",
                        isSelected = false,
                        onClick = { viewModel.sendCoachMessage("I have an exam in 30 days and want to balance study, focus blocks, and workout.") }
                    )
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(messages) { msg ->
                    CoachMessageCard(
                        message = msg,
                        onApplyAction = { action ->
                            viewModel.sendCoachMessage("Apply: $action")
                        }
                    )
                }

                if (isThinking) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ArohaSurfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = ArohaTeal
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Aroha AI is formulating strategic optimization...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArohaTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachMessageCard(
    message: CoachMessage,
    onApplyAction: (String) -> Unit
) {
    if (message.isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                color = ArohaTealDark,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                color = ArohaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArohaCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ArohaGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AROHA STRATEGY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArohaGoldLight,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArohaTextPrimary,
                        lineHeight = 22.sp
                    )

                    message.actionableSuggestion?.let { suggestion ->
                        Spacer(modifier = Modifier.height(12.dp))
                        ArohaButton(
                            text = "Apply: $suggestion",
                            containerColor = ArohaEmerald,
                            onClick = { onApplyAction(suggestion) }
                        )
                    }
                }
            }
        }
    }
}
