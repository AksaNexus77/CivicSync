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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntakeScreen(
    situationText: String,
    urgencyLevel: String,
    locationText: String,
    errorMessage: String?,
    language: AppLanguage,
    vaultDocCount: Int = 0,
    onSituationChanged: (String) -> Unit,
    onSpeechRecognized: (String) -> Unit,
    onUrgencyChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onPresetSelected: (String, String, String) -> Unit,
    onNavigateToVault: () -> Unit,
    onGenerateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isUrgencyDropdownOpen by remember { mutableStateOf(false) }
    var isListeningByVoice by remember { mutableStateOf(false) }

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
            }
        }
    }

    // Launch speech recognition
    fun launchSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val localeCode = if (language == AppLanguage.URDU) "ur-PK" else "en-PK"
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

    // Pulsing animation for glowing emerald action button
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
                            text = Strings.get("hero_badge", language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = Emerald400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Title
                Text(
                    text = Strings.get("hero_title", language),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate100,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Description (Body text >= 16sp for senior & low-literacy accessibility)
                Text(
                    text = Strings.get("hero_desc", language),
                    fontSize = 16.sp,
                    color = Slate300,
                    lineHeight = 24.sp
                )

                // Vault status chip
                if (vaultDocCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald500.copy(alpha = 0.15f))
                            .border(1.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onNavigateToVault() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FolderShared, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.URDU) "$vaultDocCount دستاویزات والٹ میں محفوظ ہیں (اے آئی میں شامل)" else "$vaultDocCount Verified Documents in Vault (Active)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preset Scenarios
        Text(
            text = Strings.get("preset_title", language),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(
                Triple(
                    Strings.get("preset_bisp", language),
                    "Meri walida ki BISP Benazir Kafalat qist biometric fingerprint fail honay ki waja se ruk gayi hai. Retailer pay order nahi de raha aur hamaray paas ghar chalane k paise nahi hain.",
                    "Punjab"
                ),
                Triple(
                    Strings.get("preset_sehat", language),
                    "Empaneled private hospital ne Sehat Sahulat Card par indoor surgery aur emergency admission dene se inkar kar diya hai. Kehtay hain panel block hai.",
                    "KPK"
                ),
                Triple(
                    Strings.get("preset_nadra", language),
                    "NADRA center ne mera CNIC renewal block kar diya hai aur Family Registration Certificate (FRC) mein ajeeb objection laga diya hai. Main daily wager hoon.",
                    "Sindh"
                ),
                Triple(
                    Strings.get("preset_eobi", language),
                    "Marhoom walid ki EOBI pension pichlay 8 maah se delay hai aur regional office koi tracking number nahi deta. Meri walida bewa hain.",
                    "Punjab"
                )
            )

            presets.forEach { (label, sit, loc) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800.copy(alpha = 0.7f))
                        .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                        .clickable { onPresetSelected(sit, "Immediate Crisis", loc) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate300
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Intake Form Card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.05f)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                // Section 1 Header with Speech-to-text action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Strings.get("step1_title", language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Strings.get("step1_subtitle", language),
                            fontSize = 14.sp,
                            color = Slate400,
                            lineHeight = 20.sp
                        )
                    }

                    // Speech-to-Text Button
                    Button(
                        onClick = { launchSpeechInput() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListeningByVoice) Rose400 else Emerald500.copy(alpha = 0.2f),
                            contentColor = if (isListeningByVoice) Slate100 else Emerald400
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("speech_to_text_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = Strings.get("speak_to_type", language),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isListeningByVoice) "..." else Strings.get("speak_to_type", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Situation Input Field (min 16sp font size for accessibility)
                OutlinedTextField(
                    value = situationText,
                    onValueChange = onSituationChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .testTag("situation_input"),
                    placeholder = {
                        Text(
                            text = Strings.get("textarea_placeholder", language),
                            fontSize = 15.sp,
                            color = Slate500,
                            lineHeight = 22.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate800.copy(alpha = 0.5f),
                        unfocusedContainerColor = Slate800.copy(alpha = 0.35f),
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

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Urgency & Province
                Text(
                    text = Strings.get("step2_title", language),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Strings.get("step2_subtitle", language),
                    fontSize = 13.sp,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Urgency & Location Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Urgency Dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Strings.get("urgency_label", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate300,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Slate800.copy(alpha = 0.5f))
                                    .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                                    .clickable { isUrgencyDropdownOpen = true }
                                    .padding(horizontal = 12.dp)
                                    .testTag("urgency_selector"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val dotColor = if (urgencyLevel.contains("Crisis")) Rose400 else Amber400
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = urgencyLevel,
                                        fontSize = 13.sp,
                                        color = Slate100,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Slate400
                                )
                            }

                            DropdownMenu(
                                expanded = isUrgencyDropdownOpen,
                                onDismissRequest = { isUrgencyDropdownOpen = false },
                                modifier = Modifier.background(Slate800)
                            ) {
                                listOf("Immediate Crisis", "This Week", "Planning Ahead").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option, color = Slate100, fontSize = 14.sp) },
                                        onClick = {
                                            onUrgencyChanged(option)
                                            isUrgencyDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Province Input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Strings.get("province_label", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate300,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = locationText,
                            onValueChange = onLocationChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("state_region_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate800.copy(alpha = 0.5f),
                                unfocusedContainerColor = Slate800.copy(alpha = 0.4f),
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100,
                                cursorColor = Emerald400
                            )
                        )
                    }
                }

                // Quick Province Chips
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val provinces = listOf(
                        "Punjab" to if (language == AppLanguage.URDU) "پنجاب" else "Punjab",
                        "Sindh" to if (language == AppLanguage.URDU) "سندھ" else "Sindh",
                        "KPK" to if (language == AppLanguage.URDU) "خیبر پختونخوا" else "KPK",
                        "Balochistan" to if (language == AppLanguage.URDU) "بلوچستان" else "Balochistan",
                        "Islamabad" to if (language == AppLanguage.URDU) "اسلام آباد (وفاق)" else "Islamabad (ICT)"
                    )

                    provinces.forEach { (provKey, label) ->
                        val isSelected = locationText.equals(provKey, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Emerald500.copy(alpha = 0.2f) else Slate800.copy(alpha = 0.6f))
                                .border(1.dp, if (isSelected) Emerald400 else Slate700, RoundedCornerShape(8.dp))
                                .clickable { onLocationChanged(provKey) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Emerald400 else Slate300
                            )
                        }
                    }
                }

                // Error Banner
                AnimatedVisibility(visible = errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Rose400.copy(alpha = 0.1f))
                            .border(1.dp, Rose400.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Rose400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 13.sp,
                                color = Rose400,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Large Glowing Emerald Action Button with subtle pulse
                Button(
                    onClick = onGenerateClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(pulseScale)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = Emerald400,
                            ambientColor = Emerald600
                        )
                        .testTag("generate_plan_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Slate900
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Slate900,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Strings.get("generate_btn", language),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}
