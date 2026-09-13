package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ActionPlanScreen
import com.example.ui.screens.ActiveCasesScreen
import com.example.ui.screens.IntakeScreen
import com.example.ui.screens.ProcessingScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo900
import com.example.ui.theme.Indigo950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

data class NavItem(
    val dest: NavigationDest,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

val navigationItems = listOf(
    NavItem(NavigationDest.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(NavigationDest.ACTIVE_CASES, "Active Cases", Icons.Filled.Folder, Icons.Outlined.Folder),
    NavItem(NavigationDest.RESOURCES, "Resources", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    NavItem(NavigationDest.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun CivicSyncApp(
    viewModel: CivicSyncViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Background: Deep Slate (slate-900) and Indigo (indigo-950) gradient
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Slate900,
            Indigo950,
            Color(0xFF070B14)
        )
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        val isWideScreen = maxWidth >= 700.dp

        if (isWideScreen) {
            // Tablet / Desktop layout: Fixed Left Sidebar (w-64 equivalent: 240dp) + Main Container
            Row(modifier = Modifier.fillMaxSize()) {
                Sidebar(
                    currentNav = uiState.currentNav,
                    onSelectNav = { viewModel.selectNav(it) },
                    casesCount = uiState.activeCases.size,
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                )

                // Main Content Area with max-width container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 840.dp)
                    ) {
                        MainScreenRouter(uiState = uiState, viewModel = viewModel)
                    }
                }
            }
        } else {
            // Mobile Compact layout: Top App Bar + Content + Bottom Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    MobileTopBar(
                        currentNav = uiState.currentNav,
                        modifier = Modifier.statusBarsPadding()
                    )
                },
                bottomBar = {
                    MobileBottomBar(
                        currentNav = uiState.currentNav,
                        onSelectNav = { viewModel.selectNav(it) },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    MainScreenRouter(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreenRouter(
    uiState: CivicSyncUiState,
    viewModel: CivicSyncViewModel
) {
    when (uiState.currentNav) {
        NavigationDest.HOME -> {
            when {
                uiState.isLoading || uiState.currentStep == WizardStep.PROCESSING -> {
                    ProcessingScreen(
                        currentMessageIndex = uiState.loadingMessageIndex,
                        messages = viewModel.loadingMessages
                    )
                }
                uiState.currentStep == WizardStep.ACTION_PLAN && uiState.currentPlan != null -> {
                    ActionPlanScreen(
                        plan = uiState.currentPlan,
                        activeTab = uiState.activeTab,
                        urgencyLevel = uiState.urgencyLevel,
                        locationText = uiState.locationText,
                        onTabSelected = { viewModel.selectTab(it) },
                        onToggleChecklist = { viewModel.toggleChecklistItem(it) },
                        onNewIntake = { viewModel.resetToIntake() }
                    )
                }
                else -> {
                    IntakeScreen(
                        situationText = uiState.situationText,
                        urgencyLevel = uiState.urgencyLevel,
                        locationText = uiState.locationText,
                        errorMessage = uiState.errorMessage,
                        onSituationChanged = { viewModel.onSituationChanged(it) },
                        onUrgencyChanged = { viewModel.onUrgencyChanged(it) },
                        onLocationChanged = { viewModel.onLocationChanged(it) },
                        onPresetSelected = { sit, urg, loc -> viewModel.populatePreset(sit, urg, loc) },
                        onGenerateClicked = { viewModel.generateActionPlan() }
                    )
                }
            }
        }
        NavigationDest.ACTIVE_CASES -> {
            ActiveCasesScreen(
                cases = uiState.activeCases,
                onSelectCase = { viewModel.loadCase(it) },
                onDeleteCase = { viewModel.deleteCase(it) },
                onNewCase = {
                    viewModel.resetToIntake()
                    viewModel.selectNav(NavigationDest.HOME)
                }
            )
        }
        NavigationDest.RESOURCES -> {
            ResourcesScreen(resources = uiState.resources)
        }
        NavigationDest.SETTINGS -> {
            SettingsScreen()
        }
    }
}

@Composable
fun Sidebar(
    currentNav: NavigationDest,
    onSelectNav: (NavigationDest) -> Unit,
    casesCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Slate900.copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                color = Slate800,
                shape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // App Logo & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Emerald400, Indigo400)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "CivicSync Logo",
                    tint = Slate900,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "CivicSync AI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate100,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Legal Aid Navigator",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }

        Text(
            text = "NAVIGATION",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Slate500,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Navigation Items
        navigationItems.forEach { item ->
            val isSelected = currentNav == item.dest
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) Emerald400.copy(alpha = 0.12f) else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Emerald400.copy(alpha = 0.35f) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectNav(item.dest) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("nav_item_${item.dest.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                        contentDescription = item.title,
                        tint = if (isSelected) Emerald400 else Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Slate100 else Slate400
                    )
                }

                if (item.dest == NavigationDest.ACTIVE_CASES && casesCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Emerald400),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = casesCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Caseworker Online Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Slate800.copy(alpha = 0.6f))
                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Emerald400)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Caseworker Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Empathetic advocacy and rights defense 24/7.",
                    fontSize = 11.sp,
                    color = Slate400,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun MobileTopBar(
    currentNav: NavigationDest,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate900.copy(alpha = 0.95f))
            .border(1.dp, Slate800)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Emerald400, Indigo400)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "CivicSync Logo",
                        tint = Slate900,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CivicSync AI",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate100,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Empathetic Legal Aid",
                        fontSize = 10.sp,
                        color = Slate400
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Emerald400)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Online",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Emerald400
                )
            }
        }
    }
}

@Composable
fun MobileBottomBar(
    currentNav: NavigationDest,
    onSelectNav: (NavigationDest) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .background(Slate900)
            .border(1.dp, Slate800),
        containerColor = Slate900,
        contentColor = Slate100,
        tonalElevation = 8.dp
    ) {
        navigationItems.forEach { item ->
            val isSelected = currentNav == item.dest
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectNav(item.dest) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Slate900,
                    selectedTextColor = Emerald400,
                    indicatorColor = Emerald400,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                ),
                modifier = Modifier.testTag("bottom_nav_${item.dest.name.lowercase()}")
            )
        }
    }
}
