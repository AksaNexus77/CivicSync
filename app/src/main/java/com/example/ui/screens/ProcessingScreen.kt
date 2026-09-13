package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProcessingScreen(
    currentMessageIndex: Int,
    messages: List<String>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "processing_spin")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val activeMessage = messages.getOrElse(currentMessageIndex % messages.size) {
        "Compiling your action plan..."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Slate900.copy(alpha = 0.95f),
                        Indigo950.copy(alpha = 0.98f)
                    )
                )
            )
            .padding(24.dp)
            .testTag("ai_processing_loading_overlay"),
        contentAlignment = Alignment.Center
    ) {
        GlassmorphicCard(
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
            backgroundColor = Color.White.copy(alpha = 0.05f)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Outer rotating ring + central pulsing icon
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Spinning gradient ring
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(100.dp)
                            .rotate(rotationAngle),
                        strokeWidth = 3.dp,
                        color = Emerald400,
                        trackColor = Slate700.copy(alpha = 0.3f)
                    )

                    // Central glowing shield
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Emerald500.copy(alpha = 0.15f))
                            .border(1.5.dp, Emerald400.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (currentMessageIndex % 3) {
                                0 -> Icons.Default.Policy
                                1 -> Icons.Default.Gavel
                                else -> Icons.Default.Shield
                            },
                            contentDescription = "Caseworker in progress",
                            tint = Emerald400,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "CIVICSYNC AI CASEWORKER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Emerald400
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Smooth micro-copy transition
                AnimatedContent(
                    targetState = activeMessage,
                    transitionSpec = {
                        fadeIn(tween(350)) togetherWith fadeOut(tween(350))
                    },
                    label = "loading_text_switch"
                ) { targetText ->
                    Text(
                        text = targetText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Evaluating statutory guidelines, welfare entitlement rules, and municipal defense precedent.",
                    fontSize = 12.sp,
                    color = Slate400,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Step progress indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    messages.indices.forEach { index ->
                        val isCurrent = (currentMessageIndex % messages.size) == index
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 20.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCurrent) Emerald400 else Slate700)
                        )
                    }
                }
            }
        }
    }
}
