package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CaseStatus
import com.example.data.local.SavedCaseEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Purple400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AppLanguage
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveCasesScreen(
    cases: List<SavedCaseEntity>,
    language: AppLanguage,
    onViewCase: (SavedCaseEntity) -> Unit,
    onUpdateStatus: (caseId: String, newStatus: String) -> Unit,
    onDeleteCase: (String) -> Unit,
    onNewCase: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<String?>("ALL") }

    val filteredCases = remember(cases, selectedFilter) {
        if (selectedFilter == null || selectedFilter == "ALL") {
            cases
        } else {
            cases.filter { it.status == selectedFilter }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.get("cases_heading", language),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Strings.get("cases_subheading", language),
                        fontSize = 13.sp,
                        color = Slate400,
                        lineHeight = 18.sp
                    )
                }

                Button(
                    onClick = onNewCase,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Slate900
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("create_new_case_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.URDU) "نیا کیس" else "New Case",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to if (language == AppLanguage.URDU) "تمام (${cases.size})" else "All (${cases.size})",
                    CaseStatus.PENDING.name to (if (language == AppLanguage.URDU) "زیرِ جائزہ" else "Pending"),
                    CaseStatus.IN_PROGRESS.name to (if (language == AppLanguage.URDU) "زیرِ کارروائی" else "In Progress"),
                    CaseStatus.FILED.name to (if (language == AppLanguage.URDU) "جمع شدہ" else "Filed"),
                    CaseStatus.RESOLVED.name to (if (language == AppLanguage.URDU) "حل شدہ" else "Resolved")
                )

                filters.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Emerald400.copy(alpha = 0.2f) else Slate800.copy(alpha = 0.5f))
                            .border(1.dp, if (isSelected) Emerald400 else Slate700, RoundedCornerShape(8.dp))
                            .clickable { selectedFilter = key }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Emerald400 else Slate300
                        )
                    }
                }
            }
        }

        // Empty state
        if (filteredCases.isEmpty()) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.03f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = Strings.get("empty_cases", language),
                            fontSize = 14.sp,
                            color = Slate400,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNewCase,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald400,
                                contentColor = Slate900
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (language == AppLanguage.URDU) "نیا لائحۂ عمل بنائیں" else "Generate Action Plan")
                        }
                    }
                }
            }
        } else {
            items(filteredCases, key = { it.id }) { item ->
                CaseCard(
                    case = item,
                    language = language,
                    onView = { onViewCase(item) },
                    onUpdateStatus = { newStatus -> onUpdateStatus(item.id, newStatus) },
                    onDelete = { onDeleteCase(item.id) }
                )
            }
        }
    }
}

@Composable
fun CaseCard(
    case: SavedCaseEntity,
    language: AppLanguage,
    onView: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    var statusMenuExpanded by remember { mutableStateOf(false) }

    val formattedDate = remember(case.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(case.createdAt))
    }

    val (statusColor, statusIcon, statusLabel) = when (case.status) {
        CaseStatus.IN_PROGRESS.name -> Triple(
            Indigo400,
            Icons.Default.Sync,
            if (language == AppLanguage.URDU) CaseStatus.IN_PROGRESS.labelUr else CaseStatus.IN_PROGRESS.labelEn
        )
        CaseStatus.FILED.name -> Triple(
            Amber400,
            Icons.AutoMirrored.Filled.Send,
            if (language == AppLanguage.URDU) CaseStatus.FILED.labelUr else CaseStatus.FILED.labelEn
        )
        CaseStatus.RESOLVED.name -> Triple(
            Emerald400,
            Icons.Default.CheckCircle,
            if (language == AppLanguage.URDU) CaseStatus.RESOLVED.labelUr else CaseStatus.RESOLVED.labelEn
        )
        else -> Triple(
            Slate400,
            Icons.Default.HourglassTop,
            if (language == AppLanguage.URDU) CaseStatus.PENDING.labelUr else CaseStatus.PENDING.labelEn
        )
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_case_card_${case.id}"),
        backgroundColor = Color.White.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title and Status Badge Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = case.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                // Interactive Status Pill
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { statusMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change Status",
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false },
                        modifier = Modifier.background(Slate800)
                    ) {
                        CaseStatus.values().forEach { st ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (language == AppLanguage.URDU) st.labelUr else st.labelEn,
                                        fontSize = 13.sp,
                                        color = Slate100
                                    )
                                },
                                onClick = {
                                    statusMenuExpanded = false
                                    onUpdateStatus(st.name)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Situation preview
            Text(
                text = case.situation,
                fontSize = 13.sp,
                color = Slate300,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate800)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = case.province, fontSize = 11.sp, color = Slate300)
                }

                // Urgency badge
                val urgColor = if (case.urgency.contains("Crisis", ignoreCase = true) || case.urgency.contains("24", ignoreCase = true)) {
                    Rose400
                } else {
                    Amber400
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(urgColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = case.urgency,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = urgColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // View Plan Button
                Button(
                    onClick = onView,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400.copy(alpha = 0.18f),
                        contentColor = Emerald400
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (language == AppLanguage.URDU) "پلان دیکھیں" else "View Plan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Case",
                        tint = Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
