package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber900
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntakeScreen(
    situationText: String,
    urgencyLevel: String,
    locationText: String,
    errorMessage: String?,
    onSituationChanged: (String) -> Unit,
    onUrgencyChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onPresetSelected: (String, String, String) -> Unit,
    onGenerateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isUrgencyDropdownOpen by remember { mutableStateOf(false) }

    val urgencyOptions = listOf("Immediate Crisis", "This Week", "Planning Ahead")

    // Subtle pulsing animation for glowing emerald button
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Section
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            backgroundColor = Color.White.copy(alpha = 0.03f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Emerald500.copy(alpha = 0.15f))
                            .border(1.dp, Emerald400.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Civic Protection Shield",
                            tint = Emerald400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "PAKISTAN LEGAL AID & SOCIAL WELFARE AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Emerald400
                    )
                }

                Text(
                    text = "You don't have to face the system alone.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate100,
                    lineHeight = 30.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Empathetic guidance for BISP Kafalat, NADRA CNIC, Sehat Sahulat Card, EOBI Pension, and Wafaqi Mohtasib administrative appeals across Pakistan.",
                    fontSize = 14.sp,
                    color = Slate300,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Scenario Preset Chips
                Text(
                    text = "Select a sample Pakistani citizen case:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PresetChip(
                        label = "BISP Biometric Error",
                        onClick = {
                            onPresetSelected(
                                "Meri walida ki BISP Kafalat payment biometric verification na honay ki wajah se rok di gayi hai. Woh boorhi hain aur retailer machine par un k fingerprints nahi aa rahay. Hamaray paas ration k paisay nahi hain.",
                                "Immediate Crisis",
                                "Punjab"
                            )
                        }
                    )
                    PresetChip(
                        label = "Sehat Card Denial at Hospital",
                        onClick = {
                            onPresetSelected(
                                "My father was brought to an empaneled hospital for emergency cardiac treatment, but the admission counter refused his Sehat Card Plus and demanded Rs. 150,000 cash advance.",
                                "Immediate Crisis",
                                "KPK"
                            )
                        }
                    )
                    PresetChip(
                        label = "NADRA Blocked CNIC",
                        onClick = {
                            onPresetSelected(
                                "NADRA has put my CNIC in digitally held / blocked status citing family tree (FRC) mismatch. Due to this, my mobile SIM and bank account are frozen.",
                                "This Week",
                                "Sindh"
                            )
                        }
                    )
                    PresetChip(
                        label = "EOBI Survivor Pension Delay",
                        onClick = {
                            onPresetSelected(
                                "My late husband was an insured worker with EOBI for 16 years, but EOBI regional office has withheld survivor pension for over 12 months citing employer documentation issues.",
                                "Planning Ahead",
                                "Balochistan"
                            )
                        }
                    )
                }
            }
        }

        // Error Banner if present
        if (!errorMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Rose400.copy(alpha = 0.12f))
                    .border(1.dp, Rose400.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error icon",
                        tint = Rose400,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 13.sp,
                        color = Slate100,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Intake Form Card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.04f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "1. Detail Your Situation",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Include what happened, notices received, dependents, and any deadlines.",
                    fontSize = 12.sp,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Large beautifully styled textarea
                OutlinedTextField(
                    value = situationText,
                    onValueChange = onSituationChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .testTag("situation_input_textarea"),
                    placeholder = {
                        Text(
                            text = "e.g., Meri walida ka BISP biometric verify nahi ho raha aur hospital mein Sehat Card reject kar diya hai. Hamaray paas ration k paise nahi hain. Ya NADRA CNIC block hai.",
                            fontSize = 13.sp,
                            color = Slate500,
                            lineHeight = 20.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate800.copy(alpha = 0.6f),
                        unfocusedContainerColor = Slate800.copy(alpha = 0.4f),
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100,
                        cursorColor = Emerald400
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Input Row: Urgency Dropdown & State/Region input
                Text(
                    text = "2. Urgency & Jurisdiction",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Social protection programs, Sehat Card rules, and administrative offices vary by Pakistani province.",
                    fontSize = 12.sp,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Urgency Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            Text(
                                text = "Urgency Level",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate400,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Slate800.copy(alpha = 0.5f))
                                    .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                                    .clickable { isUrgencyDropdownOpen = true }
                                    .padding(horizontal = 12.dp)
                                    .testTag("urgency_dropdown_button"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (urgencyLevel) {
                                                        "Immediate Crisis" -> Rose400
                                                        "This Week" -> Amber400
                                                        else -> Emerald400
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = urgencyLevel,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Slate100
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown indicator",
                                        tint = Slate400
                                    )
                                }

                                DropdownMenu(
                                    expanded = isUrgencyDropdownOpen,
                                    onDismissRequest = { isUrgencyDropdownOpen = false },
                                    modifier = Modifier
                                        .background(Slate800)
                                        .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                                ) {
                                    urgencyOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                when (option) {
                                                                    "Immediate Crisis" -> Rose400
                                                                    "This Week" -> Amber400
                                                                    else -> Emerald400
                                                                }
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = option,
                                                        color = Slate100,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            },
                                            onClick = {
                                                onUrgencyChanged(option)
                                                isUrgencyDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Province / Region Input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Province / Region",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate400,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = locationText,
                            onValueChange = onLocationChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
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
                            placeholder = {
                                Text("e.g., Punjab, Sindh, KPK", fontSize = 12.sp, color = Slate500)
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
                    listOf("Punjab", "Sindh", "KPK", "Balochistan", "Islamabad").forEach { prov ->
                        val isSelected = locationText.equals(prov, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Emerald500.copy(alpha = 0.2f) else Slate800.copy(alpha = 0.6f))
                                .border(1.dp, if (isSelected) Emerald400 else Slate700, RoundedCornerShape(8.dp))
                                .clickable { onLocationChanged(prov) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = prov,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Emerald400 else Slate300
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Large Glowing Emerald Action Button with subtle pulse
                Button(
                    onClick = onGenerateClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(pulseScale)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = Emerald500.copy(alpha = 0.5f),
                            spotColor = Emerald400
                        )
                        .testTag("generate_action_plan_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Slate900
                    )
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
                            text = "Generate Action Plan ✨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔒 Confidential & Secure: Formulations are processed strictly for civic assistance preparation.",
                    fontSize = 11.sp,
                    color = Slate500,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Slate800.copy(alpha = 0.7f))
            .border(1.dp, Slate700, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Indigo400,
            fontWeight = FontWeight.Medium
        )
    }
}
