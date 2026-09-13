package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun ActionPlanScreen(
    plan: CivicActionPlan,
    activeTab: PlanTab,
    urgencyLevel: String,
    locationText: String,
    onTabSelected: (PlanTab) -> Unit,
    onToggleChecklist: (String) -> Unit,
    onNewIntake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tabs = listOf(
        PlanTab.ELIGIBILITY to "Eligibility Summary",
        PlanTab.CHECKLIST to "Action Checklist",
        PlanTab.DRAFT_LETTER to "Draft Letter",
        PlanTab.ADVOCACY_SCRIPT to "Advocacy Script"
    )

    fun copyToClipboard(label: String, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareOrPrintDocument(title: String, content: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share or Print Document"))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header & Context Pill
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.03f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Emerald400)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACTION PLAN COMPILED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = Emerald400
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your Legal Defense & Relief Strategy",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                        }

                        OutlinedButton(
                            onClick = onNewIntake,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate600),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Slate300
                            ),
                            modifier = Modifier.testTag("new_intake_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Intake", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (urgencyLevel) {
                                        "Immediate Crisis" -> Rose400.copy(alpha = 0.2f)
                                        "This Week" -> Amber400.copy(alpha = 0.2f)
                                        else -> Emerald400.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Urgency: $urgencyLevel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (urgencyLevel) {
                                    "Immediate Crisis" -> Rose400
                                    "This Week" -> Amber400
                                    else -> Emerald400
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Indigo400.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Jurisdiction: $locationText",
                                fontSize = 11.sp,
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
                        onClick = { onTabSelected(tab) },
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

        // Active Tab Content
        when (activeTab) {
            PlanTab.ELIGIBILITY -> {
                item {
                    Text(
                        text = "Identified Benefit Entitlements & Protections",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (plan.eligibilitySummary.isEmpty()) {
                    item {
                        EmptyStateCard("No direct welfare or statutory benefits were returned.")
                    }
                } else {
                    items(plan.eligibilitySummary) { item ->
                        EligibilityCard(item = item)
                    }
                }
            }

            PlanTab.CHECKLIST -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Time-Sensitive Action Timeline",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        val completedCount = plan.actionChecklist.count { it.isCompleted }
                        Text(
                            text = "$completedCount/${plan.actionChecklist.size} Completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald400
                        )
                    }
                }

                if (plan.actionChecklist.isEmpty()) {
                    item {
                        EmptyStateCard("No checklist tasks available.")
                    }
                } else {
                    items(plan.actionChecklist) { item ->
                        ChecklistTimelineItem(
                            item = item,
                            onToggle = { onToggleChecklist(item.id) }
                        )
                    }
                }
            }

            PlanTab.DRAFT_LETTER -> {
                item {
                    DraftLetterCard(
                        letterText = plan.draftLetter,
                        onCopy = { copyToClipboard("Draft Appeal Letter", plan.draftLetter) },
                        onShare = { shareOrPrintDocument("Formal Notice & Hardship Appeal", plan.draftLetter) }
                    )
                }
            }

            PlanTab.ADVOCACY_SCRIPT -> {
                item {
                    AdvocacyScriptCard(
                        scriptText = plan.advocacyScript,
                        onCopy = { copyToClipboard("Advocacy Script", plan.advocacyScript) }
                    )
                }
            }
        }

        // Footer Legal Disclaimer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800.copy(alpha = 0.5f))
                    .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .testTag("footer_disclaimer")
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Legal Disclaimer",
                        tint = Amber400,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "IMPORTANT LEGAL NOTICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Amber400
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = plan.disclaimer.ifBlank {
                                "This is AI-generated guidance, not licensed legal advice. Consult an attorney or legal aid provider for court proceedings."
                            },
                            fontSize = 12.sp,
                            color = Slate300,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EligibilityCard(item: EligibilityItem) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("eligibility_card_${item.benefit.take(10).replace(" ", "_")}"),
        backgroundColor = Color.White.copy(alpha = 0.04f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Emerald500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Eligible checkmark",
                            tint = Emerald400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.benefit,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (item.urgency.lowercase()) {
                                "immediate", "immediate crisis" -> Rose400.copy(alpha = 0.2f)
                                "urgent", "this week" -> Amber400.copy(alpha = 0.2f)
                                else -> Emerald400.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.urgency,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (item.urgency.lowercase()) {
                            "immediate", "immediate crisis" -> Rose400
                            "urgent", "this week" -> Amber400
                            else -> Emerald400
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.reason,
                fontSize = 13.sp,
                color = Slate300,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun ChecklistTimelineItem(
    item: ChecklistItem,
    onToggle: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("checklist_item_${item.id}"),
        backgroundColor = if (item.isCompleted) Color.White.copy(alpha = 0.015f) else Color.White.copy(alpha = 0.04f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual toggleable checkbox
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Emerald400,
                    checkmarkColor = Slate900,
                    uncheckedColor = Slate500
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.task,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isCompleted) Slate500 else Slate100,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Amber400.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.timeline,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Amber400
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Indigo400.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Indigo400
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DraftLetterCard(
    letterText: String,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Formal Legal Notice Document",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCopy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Slate900
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("copy_letter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy to clipboard",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onShare,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Slate100
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate600),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("print_export_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print or share",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print / Export", fontSize = 12.sp)
                }
            }
        }

        // Clean white-paper styled document: bg-white, text-slate-900, p-8, rounded-lg, shadow-xl
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(24.dp)
                .testTag("white_paper_draft_letter")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LEGAL NOTICE / FORMAL APPEAL",
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OFFICIAL DRAFT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Clean Markdown/Text representation with clear paragraph spacing
                Text(
                    text = letterText,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A), // Slate 900
                    lineHeight = 21.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE2E8F0))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tip: Replace placeholders like [Your Name], [Landlord/Agency Name], and [Date] before sending. Send via Certified Mail with Return Receipt Requested.",
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun AdvocacyScriptCard(
    scriptText: String,
    onCopy: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Phone & Hearing Advocacy Script",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100
            )

            Button(
                onClick = onCopy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald400,
                    contentColor = Slate900
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("copy_script_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy script",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Script", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("advocacy_script_container"),
            backgroundColor = Color.White.copy(alpha = 0.05f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = Indigo400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Conversational, Assertive Script for Caseworker Calls:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Indigo400
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
                        fontSize = 14.sp,
                        color = Slate100,
                        lineHeight = 22.sp,
                        fontStyle = FontStyle.Normal
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Advocate Tips when speaking to agencies:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate300
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Note the date, time, and full name of the representative\n• Ask for your confirmation or document tracking number\n• Politely insist on written confirmation of any deadline extensions",
                    fontSize = 12.sp,
                    color = Slate400,
                    lineHeight = 18.sp
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
                fontSize = 13.sp,
                color = Slate400
            )
        }
    }
}
