package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CareLinkViewModel

@Composable
fun QuizScreen(
    viewModel: CareLinkViewModel,
    modifier: Modifier = Modifier,
    onNavigateToStore: () -> Unit
) {
    val currentIndex by viewModel.quizCurrentIndex.collectAsState()
    val answers by viewModel.quizAnswers.collectAsState()
    val isCompleted by viewModel.isQuizCompleted.collectAsState()
    val riskScore by viewModel.quizRiskScore.collectAsState()
    val riskLevel by viewModel.quizRiskLevel.collectAsState()

    val currentQuestion = viewModel.quizQuestions[currentIndex]
    val selectedOptionIndex = answers[currentQuestion.id]

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isCompleted) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- TOP PROGRESS BAR & STEPS ---
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Confidential Risk Quiz",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This assessment evaluates behaviors to give an estimated risk factor. We never ask direct orientation questions or save real personal markers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} of ${viewModel.quizQuestions.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${((currentIndex + 1) * 100) / viewModel.quizQuestions.size}% Completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = (currentIndex + 1).toFloat() / viewModel.quizQuestions.size,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // --- QUESTION DISPLAY CARD WITH ANIMATION ---
                AnimatedContent(
                    targetState = currentQuestion,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "QuestionAnimation",
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp)
                ) { q ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = q.text,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                q.options.forEachIndexed { optIndex, optionText ->
                                    val isSelected = selectedOptionIndex == optIndex
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant
                                            }
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.selectQuizAnswer(currentIndex, optIndex)
                                                }
                                            )
                                            .testTag("quiz_option_${q.id}_$optIndex")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { viewModel.selectQuizAnswer(currentIndex, optIndex) }
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = optionText,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- NAVIGATION BUTTONS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.previousQuizQuestion() },
                        enabled = currentIndex > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quiz_prev_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { viewModel.nextQuizQuestion() },
                        enabled = selectedOptionIndex != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quiz_next_button")
                    ) {
                        Text(if (currentIndex == viewModel.quizQuestions.size - 1) "Calculate Score" else "Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        } else {
            // --- QUIZ COMPLETED: REPORT & ACTIONABLE RESULTS ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success check",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(54.dp)
                )

                Text(
                    text = "Risk Factor Score Generated",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                // --- SCORE DISPLAY DONUT / VISUAL METER ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Estimated Behavior Risk Score",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(120.dp)
                        ) {
                            val scoreColor = when {
                                riskScore <= 30 -> MaterialTheme.colorScheme.secondary
                                riskScore <= 65 -> Color(0xFFD97706)
                                else -> Color(0xFFDC2626)
                            }

                            CircularProgressIndicator(
                                progress = riskScore.toFloat() / 100f,
                                modifier = Modifier.size(120.dp),
                                color = scoreColor,
                                strokeWidth = 10.dp,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$riskScore%",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor
                                )
                                Text(
                                    text = riskLevel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scoreColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when (riskLevel) {
                                "Low Risk" -> "Your behavior score is relatively low. Continue practicing healthy habits, using barrier protection (condoms) consistently, and getting a professional check once a year."
                                "Medium Risk" -> "You have some elevated behavioral risk factors. Knowing your actual HIV status is empowering. We highly encourage ordering a rapid test kit to check privately."
                                "High Risk" -> "Your score indicates multiple risk factors. Do not panic—an elevated score does NOT mean you are HIV-positive. It is simply an alert. We strongly recommend ordering a discreet at-home STI/HIV test kit or visiting a local laboratory to confirm your status safely."
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // --- PRIVACY PROTECTION CALLOUT ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Self-testing is completely private. Packages are wrapped in unmarked double boxes with zero medical labeling.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }

                // --- DIRECT CALLS TO ACTION ---
                Button(
                    onClick = onNavigateToStore,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_quiz_button")
                ) {
                    Icon(Icons.Default.LocalPharmacy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Order Confidential Home Test Kit")
                }

                OutlinedButton(
                    onClick = { viewModel.resetQuiz() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quiz_reset_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recalculate Risk Score")
                }
            }
        }
    }
}
