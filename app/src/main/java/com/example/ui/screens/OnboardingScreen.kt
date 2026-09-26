package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: ArohaViewModel
) {
    var step by remember { mutableIntStateOf(1) }

    var name by remember { mutableStateOf("Seeker") }
    val allPriorities = remember {
        listOf("Discipline", "Fitness", "Learning", "Productivity", "Focus", "Mental Clarity", "Career", "Personal Growth")
    }
    val selectedPriorities = remember { mutableStateListOf("Discipline", "Focus", "Learning") }

    var firstGoal by remember { mutableStateOf("Master Modern Android Architecture") }
    var goalCategory by remember { mutableStateOf("Knowledge") }

    var wakeTime by remember { mutableStateOf("06:30") }
    var sleepTime by remember { mutableStateOf("22:30") }
    var focusHours by remember { mutableStateOf("4 hours") }

    var isSynthesizingPlan by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArohaDeepBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step indicator dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            (1..4).forEach { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (i == step) 20.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (i == step) ArohaEmerald else ArohaSurfaceElevated)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (step) {
            1 -> {
                // Step 1: Welcome & Hero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(ArohaDimens.cornerLarge))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.aroha_hero_art),
                        contentDescription = "Aroha Ascent",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "AROHA",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArohaGoldLight,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Your Personal Evolution System",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArohaTeal
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aroha unifies your goals, daily missions, focus blocks, habits, and reflections into an empirical command center.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArohaTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What shall we call you?", color = ArohaTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ArohaTextPrimary,
                        unfocusedTextColor = ArohaTextPrimary,
                        focusedBorderColor = ArohaTeal,
                        unfocusedBorderColor = ArohaCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                ArohaButton(
                    text = "Begin Evolution →",
                    onClick = { step = 2 },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            2 -> {
                // Step 2: Choose Priorities
                Text(
                    text = "SELECT YOUR CORE PILLARS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArohaGoldLight,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Where do you wish to concentrate your momentum?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ArohaTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allPriorities.forEach { priority ->
                        val isSelected = selectedPriorities.contains(priority)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(ArohaDimens.cornerSmall))
                                .clickable {
                                    if (isSelected) selectedPriorities.remove(priority)
                                    else selectedPriorities.add(priority)
                                },
                            shape = RoundedCornerShape(ArohaDimens.cornerSmall),
                            color = if (isSelected) ArohaTeal.copy(alpha = 0.2f) else ArohaDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ArohaTeal else ArohaCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = priority,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ArohaTextPrimary
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = ArohaEmerald
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                ArohaButton(
                    text = "Continue to First Goal →",
                    enabled = selectedPriorities.isNotEmpty(),
                    onClick = { step = 3 },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            3 -> {
                // Step 3: Establish First Goal & Daily Schedule
                Text(
                    text = "ANCHOR YOUR EVOLUTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArohaGoldLight,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your Primary Objective",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ArohaTextPrimary
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = firstGoal,
                    onValueChange = { firstGoal = it },
                    label = { Text("Primary Goal", color = ArohaTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ArohaTextPrimary,
                        unfocusedTextColor = ArohaTextPrimary,
                        focusedBorderColor = ArohaTeal,
                        unfocusedBorderColor = ArohaCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = { wakeTime = it },
                        label = { Text("Wake Time", color = ArohaTextSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArohaTextPrimary,
                            unfocusedTextColor = ArohaTextPrimary,
                            focusedBorderColor = ArohaTeal,
                            unfocusedBorderColor = ArohaCardBorder
                        )
                    )
                    OutlinedTextField(
                        value = sleepTime,
                        onValueChange = { sleepTime = it },
                        label = { Text("Sleep Time", color = ArohaTextSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArohaTextPrimary,
                            unfocusedTextColor = ArohaTextPrimary,
                            focusedBorderColor = ArohaTeal,
                            unfocusedBorderColor = ArohaCardBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = focusHours,
                    onValueChange = { focusHours = it },
                    label = { Text("Available Daily Focus Hours", color = ArohaTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ArohaTextPrimary,
                        unfocusedTextColor = ArohaTextPrimary,
                        focusedBorderColor = ArohaTeal,
                        unfocusedBorderColor = ArohaCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                ArohaButton(
                    text = "Synthesize Initial Plan →",
                    enabled = firstGoal.isNotBlank(),
                    onClick = {
                        step = 4
                        isSynthesizingPlan = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            4 -> {
                // Step 4: AI Synthesis Animation & Launch
                LaunchedEffect(Unit) {
                    delay(1800)
                    isSynthesizingPlan = false
                }

                if (isSynthesizingPlan) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 48.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ArohaTeal,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Aroha AI is assembling your evolution trajectory...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ArohaGoldLight,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Balancing $wakeTime wake ritual, $focusHours deep work, and $firstGoal milestones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArohaTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ArohaGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "YOUR TRAJECTORY IS SET",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ArohaTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Welcome to Aroha, $name. Your personal command center is fully calibrated for daily compounding.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArohaTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        ArohaButton(
                            text = "Enter Command Center 🚀",
                            onClick = {
                                viewModel.completeOnboarding(
                                    name = name,
                                    priorities = selectedPriorities,
                                    firstGoal = firstGoal,
                                    category = goalCategory,
                                    wakeTime = wakeTime,
                                    sleepTime = sleepTime,
                                    focusHours = focusHours
                                )
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
