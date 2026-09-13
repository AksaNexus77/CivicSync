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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.SavedCaseEntity
import com.example.ui.screens.ActionPlanScreen
import com.example.ui.screens.ActiveCasesScreen
import com.example.ui.screens.DocumentVaultScreen
import com.example.ui.screens.IntakeScreen
import com.example.ui.screens.OfflineChecklistScreen
import com.example.ui.screens.ProcessingScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AppLanguage
import com.example.util.Strings

data class NavItem(
    val dest: NavigationDest,
    val titleEn: String,
    val titleUr: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

val navigationItems = listOf(
    NavItem(NavigationDest.HOME, "Home", "ہوم", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(NavigationDest.ACTIVE_CASES, "Cases", "کیسز", Icons.Filled.Folder, Icons.Outlined.Folder),
    NavItem(NavigationDest.DOCUMENT_VAULT, "Vault", "والٹ", Icons.Filled.Lock, Icons.Outlined.Lock),
    NavItem(NavigationDest.OFFLINE_GUIDES, "Guides", "گائیڈز", Icons.Filled.OfflinePin, Icons.Outlined.OfflinePin),
    NavItem(NavigationDest.RESOURCES, "Helplines", "ہیلپ لائن", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)
)

@Composable
fun CivicSyncApp(
    viewModel: CivicSyncViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedCases by viewModel.savedCases.collectAsState()
    val vaultDocs by viewModel.vaultDocuments.collectAsState()
    val offlineChecklists by viewModel.offlineChecklists.collectAsState()
    val isPlayingTts by viewModel.isPlayingTts.collectAsState()

    // Right-To-Left layout support for Urdu (PHASE 1 Mandate)
    val layoutDirection = if (uiState.language == AppLanguage.URDU) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
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
            val isWideScreen = maxWidth >= 760.dp

            if (isWideScreen) {
                // Tablet / Desktop layout: Fixed Left Sidebar + Main Container
                Row(modifier = Modifier.fillMaxSize()) {
                    Sidebar(
                        currentNav = uiState.currentNav,
                        language = uiState.language,
                        casesCount = savedCases.size,
                        vaultCount = vaultDocs.size,
                        onSelectNav = { viewModel.selectNav(it) },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        modifier = Modifier
                            .width(260.dp)
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
                                .widthIn(max = 900.dp)
                        ) {
                            MainScreenRouter(
                                uiState = uiState,
                                savedCases = savedCases,
                                vaultDocs = vaultDocs,
                                offlineChecklists = offlineChecklists,
                                isPlayingTts = isPlayingTts,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            } else {
                // Mobile Compact layout: Top App Bar with Bilingual Toggle + Content + Bottom Nav
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    topBar = {
                        MobileTopBar(
                            language = uiState.language,
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            modifier = Modifier.statusBarsPadding()
                        )
                    },
                    bottomBar = {
                        MobileBottomBar(
                            currentNav = uiState.currentNav,
                            language = uiState.language,
                            casesCount = savedCases.size,
                            vaultCount = vaultDocs.size,
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
                        MainScreenRouter(
                            uiState = uiState,
                            savedCases = savedCases,
                            vaultDocs = vaultDocs,
                            offlineChecklists = offlineChecklists,
                            isPlayingTts = isPlayingTts,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenRouter(
    uiState: CivicSyncUiState,
    savedCases: List<SavedCaseEntity>,
    vaultDocs: List<com.example.data.local.VaultDocumentEntity>,
    offlineChecklists: List<com.example.data.local.OfflineChecklistEntity>,
    isPlayingTts: Boolean,
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
                        language = uiState.language,
                        isPlayingTts = isPlayingTts,
                        onTabSelected = { viewModel.selectTab(it) },
                        onToggleChecklist = { viewModel.toggleChecklistItem(it) },
                        onSaveCase = { title, notes ->
                            viewModel.saveCurrentPlanAsCase(title, notes)
                            viewModel.selectNav(NavigationDest.ACTIVE_CASES)
                        },
                        onSpeakScript = { script -> viewModel.speakAdvocacyScript(script) },
                        onStopSpeech = { viewModel.stopSpeech() },
                        onTriggerHaptic = { viewModel.performHapticFeedback() },
                        onNewIntake = { viewModel.resetToIntake() }
                    )
                }
                else -> {
                    IntakeScreen(
                        situationText = uiState.situationText,
                        urgencyLevel = uiState.urgencyLevel,
                        locationText = uiState.locationText,
                        errorMessage = uiState.errorMessage,
                        language = uiState.language,
                        vaultDocCount = vaultDocs.size,
                        onSituationChanged = { viewModel.onSituationChanged(it) },
                        onSpeechRecognized = { viewModel.onSpeechRecognized(it) },
                        onUrgencyChanged = { viewModel.onUrgencyChanged(it) },
                        onLocationChanged = { viewModel.onLocationChanged(it) },
                        onPresetSelected = { sit, urg, loc -> viewModel.populatePreset(sit, urg, loc) },
                        onNavigateToVault = { viewModel.selectNav(NavigationDest.DOCUMENT_VAULT) },
                        onGenerateClicked = { viewModel.generateActionPlan() }
                    )
                }
            }
        }
        NavigationDest.ACTIVE_CASES -> {
            ActiveCasesScreen(
                cases = savedCases,
                language = uiState.language,
                onViewCase = { caseEntity -> viewModel.loadSavedCase(caseEntity) },
                onUpdateStatus = { caseId, status -> viewModel.updateCaseStatus(caseId, status) },
                onDeleteCase = { caseId -> viewModel.deleteCase(caseId) },
                onNewCase = {
                    viewModel.resetToIntake()
                    viewModel.selectNav(NavigationDest.HOME)
                }
            )
        }
        NavigationDest.DOCUMENT_VAULT -> {
            DocumentVaultScreen(
                documents = vaultDocs,
                language = uiState.language,
                onAddDocument = { title, type, uri, desc ->
                    viewModel.addVaultDocument(title, type, uri, desc)
                },
                onDeleteDocument = { id -> viewModel.deleteVaultDocument(id) }
            )
        }
        NavigationDest.OFFLINE_GUIDES -> {
            OfflineChecklistScreen(
                checklists = offlineChecklists,
                language = uiState.language,
                onToggleChecklist = { id, completed ->
                    viewModel.toggleOfflineChecklist(id, completed)
                }
            )
        }
        NavigationDest.RESOURCES -> {
            ResourcesScreen(
                resources = uiState.resources,
                language = uiState.language
            )
        }
        NavigationDest.SETTINGS -> {
            SettingsScreen()
        }
    }
}

@Composable
fun Sidebar(
    currentNav: NavigationDest,
    language: AppLanguage,
    casesCount: Int,
    vaultCount: Int,
    onSelectNav: (NavigationDest) -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Slate900.copy(alpha = 0.9f))
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
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = Strings.get("app_title", language),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate100,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = Strings.get("app_subtitle", language),
                    fontSize = 11.sp,
                    color = Emerald400
                )
            }
        }

        // Bilingual Toggle in Sidebar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Slate800)
                .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                .clickable { onToggleLanguage() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("bilingual_toggle_sidebar")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.URDU) "زبان: اردو (اردو)" else "Language: English",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Emerald400.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.URDU) "تبدیل کریں" else "Switch",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald400
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (language == AppLanguage.URDU) "رہنمائی و مینیو" else "NAVIGATION",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = Slate500,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )

        // Navigation Items
        navigationItems.forEach { item ->
            val isSelected = currentNav == item.dest
            val title = if (language == AppLanguage.URDU) item.titleUr else item.titleEn
            val badgeCount = when (item.dest) {
                NavigationDest.ACTIVE_CASES -> casesCount
                NavigationDest.DOCUMENT_VAULT -> vaultCount
                else -> 0
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) Emerald400.copy(alpha = 0.14f) else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Emerald400.copy(alpha = 0.4f) else Color.Transparent,
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
                        contentDescription = title,
                        tint = if (isSelected) Emerald400 else Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Slate100 else Slate400
                    )
                }

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Emerald400),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Citizen Protection Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Slate800.copy(alpha = 0.7f))
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
                        text = if (language == AppLanguage.URDU) "کیس ورکر فعال ہے" else "AI Caseworker Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.URDU)
                        "شہری حقوق، سرکاری محکموں سے داد رسی اور قانونی رہنمائی 24/7۔"
                    else
                        "Pakistani administrative grievance and welfare defense 24/7.",
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
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate900.copy(alpha = 0.95f))
            .border(1.dp, Slate800)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Title and Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = Strings.get("app_title", language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate100,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = Strings.get("app_subtitle", language),
                        fontSize = 10.sp,
                        color = Emerald400
                    )
                }
            }

            // Bilingual Toggle Capsule (Phase 1 Prominent Top Bar Requirement)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate800)
                    .border(1.dp, Emerald400.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onToggleLanguage() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("bilingual_toggle_topbar")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (language == AppLanguage.URDU) Emerald400 else Color.Transparent)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "اردو",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (language == AppLanguage.URDU) Slate900 else Slate400
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (language == AppLanguage.ENGLISH) Emerald400 else Color.Transparent)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "EN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (language == AppLanguage.ENGLISH) Slate900 else Slate400
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MobileBottomBar(
    currentNav: NavigationDest,
    language: AppLanguage,
    casesCount: Int,
    vaultCount: Int,
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
            val title = if (language == AppLanguage.URDU) item.titleUr else item.titleEn
            val badgeCount = when (item.dest) {
                NavigationDest.ACTIVE_CASES -> casesCount
                NavigationDest.DOCUMENT_VAULT -> vaultCount
                else -> 0
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectNav(item.dest) },
                icon = {
                    if (badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Emerald400,
                                    contentColor = Slate900
                                ) {
                                    Text(badgeCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                                contentDescription = title,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                            contentDescription = title,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = title,
                        fontSize = 10.sp,
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
