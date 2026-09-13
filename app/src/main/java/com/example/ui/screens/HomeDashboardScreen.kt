package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.data.local.CaseStatus
import com.example.data.local.SavedCaseEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home Dashboard screen displaying citizen caseload, quick welfare statistics,
 * color-coded status badges, and a prominent Floating Action Button to initiate new casework.
 */
@Composable
fun HomeDashboardScreen(
    userName: String? = null,
    cases: List<SavedCaseEntity>,
    vaultCount: Int,
    offlineGuidesCount: Int,
    language: AppLanguage,
    onStartNewCase: () -> Unit,
    onViewCase: (SavedCaseEntity) -> Unit,
    onUpdateStatus: (caseId: String, newStatus: String) -> Unit,
    onDeleteCase: (caseId: String) -> Unit,
    onSyncNow: () -> Unit = {},
    onNavigateToVault: () -> Unit,
    onNavigateToResources: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<String?>("ALL") }

    val filteredCases = remember(cases, selectedFilter) {
        if (selectedFilter == "ALL" || selectedFilter == null) {
            cases
        } else {
            cases.filter { it.status.equals(selectedFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartNewCase,
                containerColor = Emerald400,
                contentColor = Slate900,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("dashboard_start_case_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start New Case",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "greeting_banner") {
                GreetingBanner(userName = userName)
            }

            // Header & Sync Banner
            item(key = "header_banner") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Citizen Casework",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = "Encrypted Local Storage • Supabase Synchronized",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        }

                        IconButton(
                            onClick = onSyncNow,
                            modifier = Modifier
                                .testTag("dashboard_sync_button")
                                .clip(CircleShape)
                                .background(Slate800.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Casework",
                                tint = Emerald400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics Stat Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "Active Cases",
                            count = cases.size.toString(),
                            icon = Icons.Default.Folder,
                            tint = Indigo400,
                            onClick = { selectedFilter = "ALL" },
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Vault Docs",
                            count = vaultCount.toString(),
                            icon = Icons.Default.Lock,
                            tint = Emerald400,
                            onClick = onNavigateToVault,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Offline Guides",
                            count = offlineGuidesCount.toString(),
                            icon = Icons.Default.OfflinePin,
                            tint = Amber400,
                            onClick = onNavigateToResources,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Filter Chips Row
            item(key = "filter_chips") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filterOptions = listOf(
                        "ALL" to "All Cases (${cases.size})",
                        CaseStatus.PENDING.name to "Pending",
                        CaseStatus.IN_PROGRESS.name to "In Progress",
                        CaseStatus.FILED.name to "Filed",
                        CaseStatus.RESOLVED.name to "Resolved"
                    )
                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald400.copy(alpha = 0.2f),
                                selectedLabelColor = Emerald400,
                                containerColor = Slate800.copy(alpha = 0.4f),
                                labelColor = Slate300
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Emerald400 else Slate700
                            )
                        )
                    }
                }
            }

            // Case List or Empty State
            if (filteredCases.isEmpty()) {
                item(key = "empty_state") {
                    DashboardEmptyState(onStartNewCase = onStartNewCase)
                }
            } else {
                items(filteredCases, key = { it.id }) { caseItem ->
                    CaseItemCard(
                        caseItem = caseItem,
                        onViewCase = { onViewCase(caseItem) },
                        onUpdateStatus = { status -> onUpdateStatus(caseItem.id, status) },
                        onDeleteCase = { onDeleteCase(caseItem.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

    GlassmorphicCard(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        backgroundColor = Slate800.copy(alpha = 0.4f),
        borderColor = Slate700.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = count, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate100)
            Text(text = label, fontSize = 11.sp, color = Slate400)
        }
    }
}

@Composable
private fun CaseItemCard(
    caseItem: SavedCaseEntity,
    onViewCase: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onDeleteCase: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStatusDropdown by remember { mutableStateOf(false) }

    val statusBadgeColor = when (caseItem.status) {
        CaseStatus.PENDING.name -> Amber400
        CaseStatus.IN_PROGRESS.name -> Indigo400
        CaseStatus.FILED.name -> Color(0xFFC084FC) // Purple
        CaseStatus.RESOLVED.name -> Emerald400
        else -> Slate400
    }

    val formattedDate = remember(caseItem.createdAt) {
        try {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(caseItem.createdAt))
        } catch (e: Throwable) {
            "Recent"
        }
    }

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewCase() }
            .testTag("dashboard_case_item_${caseItem.id}"),
        backgroundColor = Slate800.copy(alpha = 0.45f),
        borderColor = Slate700.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = caseItem.title.ifBlank { "Citizen Grievance Record" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${caseItem.province} • $formattedDate",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Pill (Clickable to change status)
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBadgeColor.copy(alpha = 0.15f))
                            .border(1.dp, statusBadgeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { showStatusDropdown = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = caseItem.status.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusBadgeColor
                        )
                    }

                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false },
                        modifier = Modifier.background(Slate800)
                    ) {
                        CaseStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = status.labelEn,
                                        color = if (status.name == caseItem.status) Emerald400 else Slate100,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    showStatusDropdown = false
                                    onUpdateStatus(status.name)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Situation narrative excerpt
            Text(
                text = caseItem.situation,
                fontSize = 12.sp,
                color = Slate300,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (caseItem.isSynced) Icons.Default.CloudDone else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (caseItem.isSynced) Emerald400 else Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (caseItem.isSynced) "Synced to Cloud" else "Saved Locally",
                        fontSize = 11.sp,
                        color = if (caseItem.isSynced) Emerald400 else Slate400
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDeleteCase,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Case",
                            tint = Slate500,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    TextButton(onClick = onViewCase) {
                        Text(
                            text = "View Plan →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardEmptyState(
    onStartNewCase: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExamples by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(Emerald400.copy(alpha = 0.12f))
                .border(1.5.dp, Emerald400.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Emerald400,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No cases yet. Start by describing your situation — we'll build your action plan together.",
            fontSize = 14.sp,
            color = Slate300,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { showExamples = !showExamples },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(if (showExamples) "Hide Examples" else "See Example Cases")
        }

        AnimatedVisibility(visible = showExamples) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExampleCaseCard(
                    title = "Healthcare Denial",
                    description = "My Sehat Insaf card was rejected for an emergency procedure."
                )
                ExampleCaseCard(
                    title = "Eviction Notice",
                    description = "Received a 3-day notice without prior warning or due process."
                )
                ExampleCaseCard(
                    title = "Identity Verification Block",
                    description = "BISP payments stopped due to biometric mismatch at the center."
                )
            }
        }
    }
}

@Composable
private fun ExampleCaseCard(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Slate800.copy(alpha = 0.6f))
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate100)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 12.sp, color = Slate400, lineHeight = 16.sp)
        }
    }
}

@Composable
fun GreetingBanner(userName: String?, modifier: Modifier = Modifier) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greetingPrefix = if (hour in 5..17) "Assalam-o-Alaikum" else "Welcome back"
    val displayName = userName?.split(" ")?.firstOrNull() ?: "Citizen"
    val fullGreeting = if (userName.isNullOrBlank()) "Welcome, Citizen" else "$greetingPrefix, $displayName"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Slate800.copy(alpha = 0.6f))
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Emerald400.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.firstOrNull()?.toString() ?: "C",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald400
                )
            }
            Column {
                Text(
                    text = fullGreeting,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
                Text(
                    text = "Ready to manage your cases?",
                    fontSize = 13.sp,
                    color = Slate400
                )
            }
        }
    }
}
