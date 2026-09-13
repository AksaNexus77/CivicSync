package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChecklistItem
import com.example.data.model.CivicActionPlan
import com.example.data.model.EligibilityItem
import com.example.ui.PlanTab
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AppLanguage
import com.example.util.Strings

@Composable
fun ActionPlanScreen(
    plan: CivicActionPlan,
    activeTab: PlanTab,
    urgencyLevel: String,
    locationText: String,
    language: AppLanguage,
    isPlayingTts: Boolean = false,
    onTabSelected: (PlanTab) -> Unit,
    onToggleChecklist: (String) -> Unit,
    onSaveCase: (title: String, notes: String) -> Unit,
    onSpeakScript: (String) -> Unit,
    onStopSpeech: () -> Unit,
    onTriggerHaptic: () -> Unit,
    onNewIntake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSaveModal by remember { mutableStateOf(false) }
    var caseSaveTitle by remember { mutableStateOf("") }
    var caseSaveNotes by remember { mutableStateOf("") }

    val tabs = listOf(
        PlanTab.ELIGIBILITY to Strings.get("tab_eligibility", language),
        PlanTab.CHECKLIST to Strings.get("tab_checklist", language),
        PlanTab.DRAFT_LETTER to Strings.get("tab_draft_letter", language),
        PlanTab.ADVOCACY_SCRIPT to Strings.get("tab_advocacy_script", language)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation and Save Case Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onNewIntake,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Slate300
                    ),
                    modifier = Modifier.testTag("back_to_intake_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "New Case",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.URDU) "نیا مسئلہ" else "New Hardship",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Save Case Button
                Button(
                    onClick = { showSaveModal = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Slate900
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_case_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Strings.get("save_case_btn", language),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Hero Summary Card
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.05f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Emerald400.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Emerald400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.URDU) "قانونی و انتظامی لائحۂ عمل تیار ہے" else "Civic Rights & Welfare Strategy",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Text(
                                    text = if (language == AppLanguage.URDU) "وفاقی و صوبائی قوانین برائے پاکستان" else "Tailored for Pakistani Public Grievance Architecture",
                                    fontSize = 12.sp,
                                    color = Emerald400
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        urgencyLevel.contains("Crisis", ignoreCase = true) -> Rose400.copy(alpha = 0.2f)
                                        urgencyLevel.contains("Week", ignoreCase = true) -> Amber400.copy(alpha = 0.2f)
                                        else -> Emerald400.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = urgencyLevel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    urgencyLevel.contains("Crisis", ignoreCase = true) -> Rose400
                                    urgencyLevel.contains("Week", ignoreCase = true) -> Amber400
                                    else -> Emerald400
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Indigo400.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = locationText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Indigo400
                            )
                        }
                    }
                }
            }
        }

        // Tabbed Dashboard Navigation Bar
        item {
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0),
                containerColor = Slate900,
                contentColor = Slate100,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    val tabIdx = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0)
                    if (tabIdx < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                            color = Emerald400,
                            height = 3.dp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800.copy(alpha = 0.6f))
                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    .testTag("action_plan_tabs")
            ) {
                tabs.forEach { (tab, title) ->
                    val isSelected = activeTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = {
                            onTriggerHaptic()
                            onTabSelected(tab)
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Emerald400 else Slate400
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    PlanTab.ELIGIBILITY -> Icons.Default.Verified
                                    PlanTab.CHECKLIST -> Icons.Default.AssignmentTurnedIn
                                    PlanTab.DRAFT_LETTER -> Icons.Default.Description
                                    PlanTab.ADVOCACY_SCRIPT -> Icons.Default.RecordVoiceOver
                                },
                                contentDescription = null,
                                tint = if (isSelected) Emerald400 else Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }

        // Animated Tab Content Container (Phase 1 Micro-interaction)
        item {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    (fadeIn() + slideInVertically(initialOffsetY = { 20 })) togetherWith fadeOut()
                },
                label = "tabTransition"
            ) { currentTab ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    when (currentTab) {
                        PlanTab.ELIGIBILITY -> {
                            Text(
                                text = Strings.get("tab_eligibility", language),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            if (plan.eligibilitySummary.isEmpty()) {
                                EmptyStateCard("No direct welfare or statutory benefits were returned.")
                            } else {
                                plan.eligibilitySummary.forEach { item ->
                                    EligibilityCard(item = item, language = language)
                                }
                            }
                        }

                        PlanTab.CHECKLIST -> {
                            val completedCount = plan.actionChecklist.count { it.isCompleted }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Strings.get("tab_checklist", language),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Text(
                                    text = "$completedCount / ${plan.actionChecklist.size} ${if (language == AppLanguage.URDU) "مکمل" else "Completed"}",
                                    fontSize = 12.sp,
                                    color = Emerald400,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (plan.actionChecklist.isEmpty()) {
                                EmptyStateCard("No localized action steps were found.")
                            } else {
                                plan.actionChecklist.forEach { item ->
                                    ChecklistRow(
                                        item = item,
                                        language = language,
                                        onToggle = {
                                            onTriggerHaptic()
                                            onToggleChecklist(item.id)
                                        }
                                    )
                                }
                            }
                        }

                        PlanTab.DRAFT_LETTER -> {
                            DraftLetterTab(
                                draftLetterText = plan.draftLetter,
                                language = language,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("CivicSync Formal Appeal", plan.draftLetter)
                                    clipboard.setPrimaryClip(clip)
                                    onTriggerHaptic()
                                    Toast.makeText(context, Strings.get("copied_toast", language), Toast.LENGTH_SHORT).show()
                                },
                                onShare = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, plan.draftLetter)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Representation"))
                                }
                            )
                        }

                        PlanTab.ADVOCACY_SCRIPT -> {
                            AdvocacyScriptTab(
                                scriptText = plan.advocacyScript,
                                language = language,
                                isPlayingTts = isPlayingTts,
                                onToggleTts = {
                                    if (isPlayingTts) {
                                        onStopSpeech()
                                    } else {
                                        onSpeakScript(plan.advocacyScript)
                                    }
                                },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("CivicSync Helpline Script", plan.advocacyScript)
                                    clipboard.setPrimaryClip(clip)
                                    onTriggerHaptic()
                                    Toast.makeText(context, Strings.get("copied_toast", language), Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Disclaimer Card
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("action_plan_disclaimer"),
                backgroundColor = Color.White.copy(alpha = 0.02f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Notice",
                        tint = Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = plan.disclaimer,
                        fontSize = 13.sp,
                        color = Slate400,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Save Case Dialog
    if (showSaveModal) {
        AlertDialog(
            onDismissRequest = { showSaveModal = false },
            containerColor = Slate900,
            title = {
                Text(
                    text = Strings.get("save_case_btn", language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (language == AppLanguage.URDU)
                            "اس کیس کو اپنے ڈیش بورڈ میں محفوظ کریں تاکہ آپ بعد میں پیش رفت اور سرکاری درخواستوں کی نگرانی کر سکیں۔"
                        else
                            "Save this action plan to your Active Cases dashboard to track appeals, hearings, and institutional status offline.",
                        fontSize = 13.sp,
                        color = Slate300
                    )
                    OutlinedTextField(
                        value = caseSaveTitle,
                        onValueChange = { caseSaveTitle = it },
                        label = { Text(if (language == AppLanguage.URDU) "کیس کا عنوان" else "Case Title", color = Slate400) },
                        placeholder = { Text("e.g., BISP Biometric Grievance", color = Slate500) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate700
                        )
                    )
                    OutlinedTextField(
                        value = caseSaveNotes,
                        onValueChange = { caseSaveNotes = it },
                        label = { Text(if (language == AppLanguage.URDU) "اضافی نوٹس یا ڈائری نمبر" else "Citizen Notes / Diary No.", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate700
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveCase(caseSaveTitle, caseSaveNotes)
                        showSaveModal = false
                        caseSaveTitle = ""
                        caseSaveNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Slate900
                    )
                ) {
                    Text(if (language == AppLanguage.URDU) "محفوظ کریں" else "Confirm Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveModal = false }) {
                    Text(if (language == AppLanguage.URDU) "منسوخ" else "Cancel", color = Slate400)
                }
            }
        )
    }
}

@Composable
fun EligibilityCard(item: EligibilityItem, language: AppLanguage) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("eligibility_item_${item.benefit.replace(" ", "_")}"),
        backgroundColor = Color.White.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.benefit,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100,
                    modifier = Modifier.weight(1f)
                )

                val badgeColor = when {
                    item.urgency.contains("Crisis", ignoreCase = true) -> Rose400
                    item.urgency.contains("Week", ignoreCase = true) -> Amber400
                    else -> Emerald400
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.urgency,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body text >= 16sp
            Text(
                text = item.reason,
                fontSize = 15.sp,
                color = Slate300,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun ChecklistRow(item: ChecklistItem, language: AppLanguage, onToggle: () -> Unit) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("checklist_item_${item.id}"),
        backgroundColor = if (item.isCompleted) Emerald500.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.04f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Emerald400,
                    uncheckedColor = Slate500,
                    checkmarkColor = Slate900
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.task,
                    fontSize = 15.sp,
                    fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (item.isCompleted) Slate400 else Slate100,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Slate800)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 10.sp,
                            color = Emerald400,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Slate800)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.timeline,
                            fontSize = 10.sp,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DraftLetterTab(
    draftLetterText: String,
    language: AppLanguage,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Strings.get("tab_draft_letter", language),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Slate200),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == AppLanguage.URDU) "شیئر" else "Share", fontSize = 12.sp)
                }

                Button(
                    onClick = onCopy,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Slate900),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.get("copy_letter_btn", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.05f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800.copy(alpha = 0.8f))
                        .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = draftLetterText,
                        fontSize = 15.sp,
                        color = Slate100,
                        lineHeight = 23.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun AdvocacyScriptTab(
    scriptText: String,
    language: AppLanguage,
    isPlayingTts: Boolean,
    onToggleTts: () -> Unit,
    onCopy: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Strings.get("tab_advocacy_script", language),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Text-to-Speech Button (Phase 1 Accessibility Requirement)
                Button(
                    onClick = onToggleTts,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlayingTts) Rose400 else Emerald500.copy(alpha = 0.2f),
                        contentColor = if (isPlayingTts) Slate100 else Emerald400
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp).testTag("listen_tts_button")
                ) {
                    Icon(
                        imageVector = if (isPlayingTts) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPlayingTts) Strings.get("stop_tts_btn", language) else Strings.get("listen_tts_btn", language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCopy,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Slate200),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.get("copy_script_btn", language), fontSize = 12.sp)
                }
            }
        }

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.05f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FormatQuote, contentDescription = null, tint = Indigo400, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.URDU)
                            "ہیلپ لائن پر بات کرنے کے لیے مؤثر رہنمائی (بی آئی ایس پی 0800-26477، نادرا 1777، وفاقی محتسب 1055):"
                        else
                            "Helpline Advocacy Script (BISP 0800-26477, NADRA 1777, Mohtasib 1055):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Indigo400,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800.copy(alpha = 0.8f))
                        .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = scriptText,
                        fontSize = 16.sp,
                        color = Slate100,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == AppLanguage.URDU) "اہم ہدایات برائے ہیلپ لائن گفتگو:" else "Helpline Advocacy Tips (BISP, NADRA, Public Offices):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate300
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.URDU)
                        "• کال ملانے سے پہلے اپنا 13 ہندسوں کا شناختی کارڈ اپنے سامنے رکھیں\n• نمائندے سے لازمی اپنی شکایت کا سرکاری ڈائری / ٹریکنگ نمبر نوٹ کریں\n• کال اٹینڈنٹ کا نام اور عہدہ اپنے پاس درج کر لیں\n• تحصیل یا زونل دفتر کی اگلی کھلی کچہری کے بارے میں دریافت کریں"
                    else
                        "• Keep your 13-digit CNIC card in hand before calling\n• Always ask for the official Complaint / Diary Tracking Number\n• Note the name and designation of the call representative\n• Inquire about the next Tehsil / Zonal office open court (Khuli Kachehri)",
                    fontSize = 13.sp,
                    color = Slate400,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.03f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Slate500,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Slate400
            )
        }
    }
}
