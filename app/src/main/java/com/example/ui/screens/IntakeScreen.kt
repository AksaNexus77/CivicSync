package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AppLanguage
import com.example.util.Strings
import com.example.util.layoutDirection

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Senior-level Intake Screen implementation incorporating:
 * - Dynamic Right-to-Left (RTL) layout switching via [LocalLayoutDirection].
 * - Comprehensive localized string extraction matching [R.string].
 * - Low-literacy accessibility formatting with body typography scaled to >= 16sp.
 * - Haptic feedback integration via [LocalHapticFeedback].
 * - Speech-to-Text dictation in Urdu, Arabic, or English.
 * - Multi-jurisdiction support (Pakistan, USA, UK, Global).
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
@Composable
fun IntakeScreen(
    situationText: String,
    urgencyLevel: String,
    country: String = "Pakistan",
    region: String = "Punjab",
    errorMessage: String?,
    language: AppLanguage,
    vaultDocCount: Int = 0,
    onSituationChanged: (String) -> Unit,
    onSpeechRecognized: (String) -> Unit,
    onUrgencyChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit = {},
    onRegionChanged: (String) -> Unit = {},
    onPresetSelected: (String, String, String, String) -> Unit,
    onNavigateToVault: () -> Unit,
    onGenerateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    var isUrgencyDropdownOpen by remember { mutableStateOf(false) }
    var isCountryDropdownOpen by remember { mutableStateOf(false) }
    var isListeningByVoice by remember { mutableStateOf(false) }

    // Audio Permission State
    val audioPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )

    // Speech-to-text Activity Result Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningByVoice = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                onSpeechRecognized(spokenText)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    fun launchSpeechInput() {
        if (!audioPermissionState.status.isGranted) {
            audioPermissionState.launchPermissionRequest()
            return
        }

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val localeCode = when (language) {
                AppLanguage.URDU -> "ur-PK"
                AppLanguage.ARABIC -> "ar-SA"
                AppLanguage.ENGLISH -> "en-US"
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeCode)
            putExtra(RecognizerIntent.EXTRA_PROMPT, Strings.get("speaking_indicator", language))
        }
        isListeningByVoice = true
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            isListeningByVoice = false
        }
    }


    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // RTL provider for Urdu and Arabic languages
    CompositionLocalProvider(LocalLayoutDirection provides language.layoutDirection()) {
        Column(
            modifier = modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Hero Section
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Indigo400.copy(alpha = 0.15f))
                            .border(1.dp, Indigo400.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.hero_badge),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = Emerald400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hero Title (High contrast, display typography)
                    Text(
                        text = stringResource(R.string.hero_title),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate100,
                        lineHeight = 34.sp,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hero Description (Scalable >= 16sp for senior & low-literacy accessibility)
                    Text(
                        text = stringResource(R.string.hero_desc),
                        fontSize = 16.sp,
                        color = Slate300,
                        lineHeight = 24.sp
                    )

                    if (vaultDocCount > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Emerald500.copy(alpha = 0.15f))
                                .border(1.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateToVault()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FolderShared,
                                    contentDescription = stringResource(R.string.nav_vault),
                                    tint = Emerald400,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$vaultDocCount documents indexed in vault",
                                    fontSize = 16.sp,
                                    color = Emerald400,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Presets Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.preset_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate300
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip(
                        label = stringResource(R.string.preset_bisp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPresetSelected(
                                "My biometric fingerprint verification failed repeatedly at the franchise office, and my quarterly assistance has been halted without official written notice.",
                                "Immediate Crisis",
                                "Pakistan",
                                "Punjab"
                            )
                        }
                    )
                    PresetChip(
                        label = stringResource(R.string.preset_sehat),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPresetSelected(
                                "The empaneled hospital denied cashless emergency surgical admission on my universal health card citing quota exhaustion.",
                                "Immediate Crisis",
                                "Pakistan",
                                "KPK"
                            )
                        }
                    )
                    PresetChip(
                        label = stringResource(R.string.preset_snap),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPresetSelected(
                                "Emergency food assistance and SNAP benefits were terminated abruptly due to procedural re-certification mail not delivered.",
                                "Immediate Crisis",
                                "United States",
                                "California"
                            )
                        }
                    )
                    PresetChip(
                        label = stringResource(R.string.preset_eviction),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPresetSelected(
                                "Received an unlawful emergency eviction notice without statutory tribunal hearing or mediation notice.",
                                "This Week",
                                "United Kingdom",
                                "London"
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Grievance narrative
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Slate900.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.step1_title),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.step1_subtitle),
                                fontSize = 16.sp,
                                color = Slate400,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    if (isListeningByVoice) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.listening),
                            color = Rose400,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = situationText,
                        onValueChange = onSituationChanged,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.textarea_placeholder),
                                color = Slate500,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { launchSpeechInput() },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isListeningByVoice) Rose400.copy(alpha = 0.25f) else Indigo400.copy(alpha = 0.15f))
                                    .border(1.dp, if (isListeningByVoice) Rose400 else Indigo400.copy(alpha = 0.4f), CircleShape)
                                    .testTag("speech_to_text_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = stringResource(R.string.cd_mic_record),
                                    tint = if (isListeningByVoice) Rose400 else Indigo400,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("grievance_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            cursorColor = Emerald400
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Slate100
                        )
                    )

                    // Error message
                    AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Rose400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = Rose400,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 2: Jurisdiction & Urgency
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Slate900.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(R.string.step2_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.step2_subtitle),
                        fontSize = 16.sp,
                        color = Slate400,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Country Selection Dropdown
                    Text(
                        text = stringResource(R.string.country_label),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0B1329))
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .clickable { isCountryDropdownOpen = true }
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = Emerald400,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = country,
                                    color = Slate100,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Country",
                                tint = Slate400
                            )
                        }

                        DropdownMenu(
                            expanded = isCountryDropdownOpen,
                            onDismissRequest = { isCountryDropdownOpen = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            listOf("Pakistan", "United States", "United Kingdom", "Global / Other").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(text = option, color = Slate100, fontSize = 16.sp) },
                                    onClick = {
                                        onCountryChanged(option)
                                        isCountryDropdownOpen = false
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Region / Province Input
                    Text(
                        text = stringResource(R.string.region_label),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = region,
                        onValueChange = onRegionChanged,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Urgency Level Dropdown
                    Text(
                        text = stringResource(R.string.urgency_label),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0B1329))
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .clickable { isUrgencyDropdownOpen = true }
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = when {
                                        urgencyLevel.contains("Immediate") -> Rose400
                                        urgencyLevel.contains("Week") -> Amber400
                                        else -> Emerald400
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = urgencyLevel,
                                    color = Slate100,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Urgency",
                                tint = Slate400
                            )
                        }

                        DropdownMenu(
                            expanded = isUrgencyDropdownOpen,
                            onDismissRequest = { isUrgencyDropdownOpen = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            listOf(
                                stringResource(R.string.urgency_immediate),
                                stringResource(R.string.urgency_week),
                                stringResource(R.string.urgency_planning)
                            ).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(text = option, color = Slate100, fontSize = 16.sp) },
                                    onClick = {
                                        onUrgencyChanged(option)
                                        isUrgencyDropdownOpen = false
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Primary Action Button (Minimum 48dp touch target, scalable, animated pulse)
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onGenerateClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(pulseScale)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Emerald500)
                    .testTag("generate_action_plan_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Emerald600, Emerald500, Color(0xFF059669))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.generate_btn),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Slate800)
            .border(1.dp, Slate700, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Slate300,
            fontWeight = FontWeight.Medium
        )
    }
}
