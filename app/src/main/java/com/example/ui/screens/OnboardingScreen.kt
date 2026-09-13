package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badgeText: String
)

/**
 * Premium 3-slide onboarding screen presenting CivicSync Global's mission,
 * local-first encrypted architecture, and empathetic AI legal aid capabilities.
 */
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "Empowering Citizen Rights",
                subtitle = "Universal Welfare & Legal Aid Navigator",
                description = "Navigate statutory welfare programs, poverty alleviation schemes (BISP, Sehat Card Plus, EOBI), and administrative remedies across provinces with zero bureaucratic barriers.",
                icon = Icons.Default.Public,
                accentColor = Emerald400,
                badgeText = "Mission & Inclusion"
            ),
            OnboardingSlide(
                title = "Local-First & Encrypted",
                subtitle = "256-Bit SQLCipher Sovereign Vault",
                description = "Your confidential hardship narratives and evidence documents are stored locally in an encrypted Room database. Optional private Supabase sync keeps your records protected under strict Row Level Security.",
                icon = Icons.Default.Lock,
                accentColor = Indigo400,
                badgeText = "Zero-Knowledge Privacy"
            ),
            OnboardingSlide(
                title = "Empathetic AI Caseworker",
                subtitle = "Powered by Google Gemini 3.5",
                description = "Generate official representation letters for Deputy Commissioners and Ombudsmen, step-by-step verified checklists, and spoken helpline advocacy scripts within seconds.",
                icon = Icons.Default.AutoAwesome,
                accentColor = Amber400,
                badgeText = "Next-Gen Legal Tech"
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }
    val currentSlide = slides[currentSlideIndex]

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Skip Button (top right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Emerald400,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CivicSync Global",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
            }

            androidx.compose.material3.TextButton(
                onClick = onFinishOnboarding,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip",
                    color = Slate400,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Central Content Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Halo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(currentSlide.accentColor.copy(alpha = 0.15f))
                    .border(2.dp, currentSlide.accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentSlide.icon,
                    contentDescription = null,
                    tint = currentSlide.accentColor,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(currentSlide.accentColor.copy(alpha = 0.15f))
                    .border(1.dp, currentSlide.accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currentSlide.badgeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = currentSlide.accentColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slide Title
            Text(
                text = currentSlide.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Slate100,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = currentSlide.subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = currentSlide.accentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Glassmorphic description
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.04f),
                borderColor = Slate700.copy(alpha = 0.5f)
            ) {
                Text(
                    text = currentSlide.description,
                    fontSize = 13.sp,
                    color = Slate300,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }

        // Bottom Controls: Pager dots and Navigation Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Slide Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                slides.indices.forEach { index ->
                    val isSelected = index == currentSlideIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) currentSlide.accentColor else Slate700
                            )
                    )
                }
            }

            // Next / Get Started Button
            Button(
                onClick = {
                    if (currentSlideIndex < slides.size - 1) {
                        currentSlideIndex++
                    } else {
                        onFinishOnboarding()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentSlide.accentColor
                )
            ) {
                Text(
                    text = if (currentSlideIndex == slides.size - 1) "Get Started" else "Continue",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }
        }
    }
}
