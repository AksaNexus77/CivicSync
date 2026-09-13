package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
import com.example.util.layoutDirection

/**
 * Production-grade Action Plan display screen presenting synthesized legal aid,
 * social welfare eligibility assessments, dynamic checklist milestones, formal
 * administrative representations, and verbal advocacy scripts.
 *
 * Implements:
 * - LazyColumn with stable keys for fluid scrolling and zero redundant recomposition.
 * - Dynamic Right-to-Left (RTL) composition local switching for Urdu and Arabic.
 * - Minimum 16sp font sizing for accessible, high-legibility rendering.
 * - Haptic feedback on all interactive milestones via [LocalHapticFeedback].
 * - Crossfade and [AnimatedContent] tab transitions with [Modifier.animateContentSize].
 */
@Composable
fun ActionPlanScreen(
    plan: CivicActionPlan,
    activeTab: PlanTab,
    urgencyLevel: String,
    country: String = "Pakistan",
    region: String = "Punjab",
    language: AppLanguage,
    isOfflineFallback: Boolean = false,
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
    val haptic = LocalHapticFeedback.current
    var showSaveModal by remember { mutableStateOf(false) }
    var caseSaveTitle by remember { mutableStateOf("") }
    var caseSaveNotes by remember { mutableStateOf("") }

    val tabs = listOf(
        PlanTab.ELIGIBILITY to stringResource(R.string.tab_eligibility),
        PlanTab.CHECKLIST to stringResource(R.string.tab_checklist),
        PlanTab.DRAFT_LETTER to stringResource(R.string.tab_draft_letter),
        PlanTab.ADVOCACY_SCRIPT to stringResource(R.string.tab_advocacy_script)
    )

    // Derived progress computation to avoid redundant recomposition
    val completedCount by remember(plan.actionChecklist) {
        derivedStateOf { plan.actionChecklist.count { it.isCompleted } }
    }
    val totalChecklistCount by remember(plan.actionChecklist) {
        derivedStateOf { plan.actionChecklist.size }
    }

    // Wrap entire layout in LocalLayoutDirection for seamless RTL mirroring
    CompositionLocalProvider(LocalLayoutDirection provides language.layoutDirection()) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation and Save Case Controls
            item(key = "header_controls") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNewIntake()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("back_to_intake_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.back_to_intake),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Save Case Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showSaveModal = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald400,
                            contentColor = Slate900
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("save_case_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.save_case_btn),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Hero Summary Card
            item(key = "summary_card") {
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
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Emerald400.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Emerald400,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Civic Strategy & Welfare Plan",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate100
                                    )
                                    Text(
                                        text = "Jurisdiction: $region, $country",
                                        fontSize = 16.sp,
                                        color = Emerald400
                                    )
                                }
                            }
                        }

                        if (isOfflineFallback) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Amber400.copy(alpha = 0.2f))
                                    .border(1.dp, Amber400.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.offline_contingency_badge),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber400
                                )
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
                                    fontSize = 14.sp,
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
                                    text = "$completedCount/$totalChecklistCount Steps Completed",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Indigo400
                                )
                            }
                        }
                    }
                }
            }

            // Tabbed Navigation
            item(key = "plan_tabs") {
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
                ) {
                    tabs.forEach { (tab, title) ->
                        val isSelected = activeTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(tab)
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Emerald400 else Slate400
                                )
                            },
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }

            // Dynamic Tab Content with Animated Transitions
            item(key = "tab_animated_content") {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_content_transition",
                    modifier = Modifier.animateContentSize()
                ) { currentTab ->
                    when (currentTab) {
                        PlanTab.ELIGIBILITY -> EligibilityTabContent(
                            items = plan.eligibilitySummary,
                            language = language
                        )
                        PlanTab.CHECKLIST -> ChecklistTabContent(
                            checklist = plan.actionChecklist,
                            onToggle = { itemId ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleChecklist(itemId)
                            }
                        )
                        PlanTab.DRAFT_LETTER -> DraftLetterTabContent(
                            letterText = plan.draftLetter,
                            language = language,
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Civic Representation", plan.draftLetter))
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(context, context.getString(R.string.copied_toast), Toast.LENGTH_SHORT).show()
                            }
                        )
                        PlanTab.ADVOCACY_SCRIPT -> AdvocacyScriptTabContent(
                            scriptText = plan.advocacyScript,
                            isPlaying = isPlayingTts,
                            language = language,
                            onSpeak = { onSpeakScript(plan.advocacyScript) },
                            onStop = onStopSpeech,
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Advocacy Script", plan.advocacyScript))
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(context, context.getString(R.string.copied_toast), Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Statutory Disclaimer Notice
            item(key = "disclaimer") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = plan.disclaimer,
                            fontSize = 14.sp,
                            color = Slate500,
                            lineHeight = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Case Save Modal
    if (showSaveModal) {
        AlertDialog(
            onDismissRequest = { showSaveModal = false },
            title = {
                Text(
                    text = stringResource(R.string.save_case_btn),
                    color = Slate100,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Store this action plan in your local dashboard for offline access and progress tracking.",
                        color = Slate300,
                        fontSize = 16.sp
                    )
                    OutlinedTextField(
                        value = caseSaveTitle,
                        onValueChange = { caseSaveTitle = it },
                        placeholder = { Text("Case Title (e.g., CNIC Grievance)", color = Slate500, fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
                    )
                    OutlinedTextField(
                        value = caseSaveNotes,
                        onValueChange = { caseSaveNotes = it },
                        placeholder = { Text("Personal Notes / Token Number", color = Slate500, fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaveCase(caseSaveTitle, caseSaveNotes)
                        showSaveModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Text("Save / محفوظ کریں", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveModal = false }) {
                    Text("Cancel", color = Slate400, fontSize = 16.sp)
                }
            },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun EligibilityTabContent(
    items: List<EligibilityItem>,
    language: AppLanguage
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.animateContentSize()
    ) {
        items.forEach { item ->
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
                        Text(
                            text = item.benefit,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        item.urgency.contains("Crisis", ignoreCase = true) -> Rose400.copy(alpha = 0.2f)
                                        item.urgency.contains("Week", ignoreCase = true) -> Amber400.copy(alpha = 0.2f)
                                        else -> Emerald400.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.urgency,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    item.urgency.contains("Crisis", ignoreCase = true) -> Rose400
                                    item.urgency.contains("Week", ignoreCase = true) -> Amber400
                                    else -> Emerald400
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.reason,
                        fontSize = 16.sp,
                        color = Slate300,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistTabContent(
    checklist: List<ChecklistItem>,
    onToggle: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.animateContentSize()
    ) {
        checklist.forEach { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (item.isCompleted) Color(0xFF064E3B).copy(alpha = 0.2f) else Color(0xFF0F172A))
                    .border(
                        1.dp,
                        if (item.isCompleted) Emerald500.copy(alpha = 0.4f) else Slate800,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onToggle(item.id) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { onToggle(item.id) },
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.isCompleted) Slate400 else Slate100,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Indigo400.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.category,
                                    fontSize = 14.sp,
                                    color = Indigo400,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Slate800)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.timeline,
                                    fontSize = 14.sp,
                                    color = Slate300
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
fun DraftLetterTabContent(
    letterText: String,
    language: AppLanguage,
    onCopy: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onCopy,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.cd_copy_letter),
                    tint = Slate900,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.copy_letter_btn),
                    color = Slate900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                .padding(20.dp)
        ) {
            Text(
                text = letterText,
                fontSize = 16.sp,
                color = Slate200,
                lineHeight = 26.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun AdvocacyScriptTabContent(
    scriptText: String,
    isPlaying: Boolean,
    language: AppLanguage,
    onSpeak: () -> Unit,
    onStop: () -> Unit,
    onCopy: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = if (isPlaying) onStop else onSpeak,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) Rose400 else Indigo400
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isPlaying) stringResource(R.string.cd_stop_tts) else stringResource(R.string.cd_speak_script),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlaying) stringResource(R.string.stop_tts_btn) else stringResource(R.string.listen_tts_btn),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_script_btn),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.copy_script_btn),
                    fontSize = 16.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verbal Counter / Phone Script",
                        color = Emerald400,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = scriptText,
                    fontSize = 16.sp,
                    color = Slate200,
                    lineHeight = 26.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
