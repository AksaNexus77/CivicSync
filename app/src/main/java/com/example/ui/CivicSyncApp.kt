package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.ActionPlanSkeleton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.screens.ActionPlanScreen
import com.example.ui.screens.ActiveCasesScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DocumentVaultScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.IntakeScreen
import com.example.ui.screens.OfflineChecklistScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.screens.SettingsScreen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo950
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AppLanguage
import com.example.util.layoutDirection

/**
 * Top-level navigation items descriptor.
 */
data class NavItem(
    val dest: NavigationDest,
    val titleResId: Int,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

val navigationItems = listOf(
    NavItem(NavigationDest.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(NavigationDest.ACTIVE_CASES, R.string.nav_active_cases, Icons.Filled.Folder, Icons.Outlined.Folder),
    NavItem(NavigationDest.DOCUMENT_VAULT, R.string.nav_vault, Icons.Filled.Lock, Icons.Outlined.Lock),
    NavItem(NavigationDest.OFFLINE_GUIDES, R.string.nav_offline, Icons.Filled.OfflinePin, Icons.Outlined.OfflinePin),
    NavItem(NavigationDest.RESOURCES, R.string.nav_resources, Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)
)

/**
 * Root Composable orchestrating state management and responsive layouts for CivicSync Global.
 * Observes state via [collectAsStateWithLifecycle] to prevent lifecycle leaks and handle
 * configuration changes (rotations) smoothly.
 */
@Composable
fun CivicSyncApp(
    viewModel: CivicSyncViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val language by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentNav by viewModel.currentNav.collectAsStateWithLifecycle()
    val savedCases by viewModel.savedCases.collectAsStateWithLifecycle()
    val vaultDocs by viewModel.vaultDocuments.collectAsStateWithLifecycle()
    val offlineChecklists by viewModel.offlineChecklists.collectAsStateWithLifecycle()
    val isPlayingTts by viewModel.isPlayingTts.collectAsStateWithLifecycle()
    val userToast by viewModel.userToast.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides language.layoutDirection()) {
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
            val isFullscreen = currentNav == NavigationDest.ONBOARDING || currentNav == NavigationDest.AUTH
            val isWideScreen = maxWidth >= 760.dp

            if (isFullscreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    MainScreenRouter(
                        uiState = uiState,
                        currentNav = currentNav,
                        language = language,
                        savedCases = savedCases,
                        vaultDocs = vaultDocs,
                        offlineChecklists = offlineChecklists,
                        isPlayingTts = isPlayingTts,
                        viewModel = viewModel
                    )
                }
            } else if (isWideScreen) {
                // Adaptive Tablet / Desktop layout: Side Rail + Content
                Row(modifier = Modifier.fillMaxSize()) {
                    Sidebar(
                        currentNav = currentNav,
                        language = language,
                        hasPendingCases = savedCases.any { it.status == com.example.data.local.CaseStatus.PENDING.name },
                        vaultCount = vaultDocs.size,
                        onSelectNav = { viewModel.navigateTo(it) },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    )

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
                                currentNav = currentNav,
                                language = language,
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
                // Mobile layout with top app bar, smooth screen transitions, and bottom navigation
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    topBar = {
                        MobileTopBar(
                            language = language,
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onOpenSettings = { viewModel.navigateTo(NavigationDest.SETTINGS) },
                            modifier = Modifier.statusBarsPadding()
                        )
                    },
                    bottomBar = {
                        MobileBottomBar(
                            currentNav = currentNav,
                            language = language,
                            hasPendingCases = savedCases.any { it.status == com.example.data.local.CaseStatus.PENDING.name },
                            vaultCount = vaultDocs.size,
                            onSelectNav = { viewModel.navigateTo(it) },
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
                            currentNav = currentNav,
                            language = language,
                            savedCases = savedCases,
                            vaultDocs = vaultDocs,
                            offlineChecklists = offlineChecklists,
                            isPlayingTts = isPlayingTts,
                            viewModel = viewModel
                        )

                        // Ephemeral Floating Toast
                        AnimatedVisibility(
                            visible = userToast != null,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        ) {
                            userToast?.let { toastMsg ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                                        .border(1.dp, Emerald400.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = toastMsg,
                                        color = Emerald400,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Main content router switching between top-level destinations and the 3-step Intake/Processing/ActionPlan wizard.
 */
@Composable
fun MainScreenRouter(
    uiState: CivicSyncUiState,
    currentNav: NavigationDest,
    language: AppLanguage,
    savedCases: List<com.example.data.local.SavedCaseEntity>,
    vaultDocs: List<com.example.data.local.VaultDocumentEntity>,
    offlineChecklists: List<com.example.data.local.OfflineChecklistEntity>,
    isPlayingTts: Boolean,
    viewModel: CivicSyncViewModel
) {
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentNav,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "tab_transition"
    ) { navState ->
        when (navState) {
            NavigationDest.ONBOARDING -> {
                OnboardingScreen(
                    onFinishOnboarding = { viewModel.completeOnboarding() }
                )
            }
            NavigationDest.AUTH -> {
                AuthScreen(
                    isLoading = authLoading,
                    errorMessage = authError,
                    onSignIn = { email, pass -> viewModel.signIn(email, pass) },
                    onSignUp = { email, pass -> viewModel.signUp(email, pass) },
                    onGoogleSignIn = { viewModel.continueAsGuest() },
                    onContinueGuest = { viewModel.continueAsGuest() }
                )
            }
            NavigationDest.HOME -> {
                HomeDashboardScreen(
                    userName = viewModel.getUserFirstName(),
                    cases = savedCases,
                    vaultCount = vaultDocs.size,
                    offlineGuidesCount = offlineChecklists.size,
                    language = language,
                    onStartNewCase = { viewModel.startNewCase() },
                    onViewCase = { caseEntity -> viewModel.viewSavedCase(caseEntity) },
                    onUpdateStatus = { caseId, status -> viewModel.updateCaseStatus(caseId, status) },
                    onDeleteCase = { caseId -> viewModel.deleteCase(caseId) },
                    onSyncNow = { viewModel.syncCasework() },
                    onNavigateToVault = { viewModel.navigateTo(NavigationDest.DOCUMENT_VAULT) },
                    onNavigateToResources = { viewModel.navigateTo(NavigationDest.RESOURCES) }
                )
            }
            NavigationDest.INTAKE_WIZARD -> {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "home_wizard_transition"
                ) { state ->
                    when (state) {
                    is CivicSyncUiState.Idle -> {
                        IntakeScreen(
                            situationText = state.situationText,
                            urgencyLevel = state.urgencyLevel,
                            country = state.country,
                            region = state.region,
                            errorMessage = state.validationError,
                            language = language,
                            vaultDocCount = vaultDocs.size,
                            onSituationChanged = { viewModel.onSituationChanged(it) },
                            onSpeechRecognized = { viewModel.onSpeechRecognized(it) },
                            onUrgencyChanged = { viewModel.onUrgencyChanged(it) },
                            onCountryChanged = { viewModel.onCountryChanged(it) },
                            onRegionChanged = { viewModel.onRegionChanged(it) },
                            onPresetSelected = { sit, urg, cnt, reg -> viewModel.selectPreset(sit, urg, cnt, reg) },
                            onNavigateToVault = { viewModel.navigateTo(NavigationDest.DOCUMENT_VAULT) },
                            onGenerateClicked = { viewModel.generateActionPlan() }
                        )
                    }
                    is CivicSyncUiState.Loading -> {
                        ActionPlanSkeleton(
                            loadingStatus = state.message
                        )
                    }
                    is CivicSyncUiState.Success -> {
                        ActionPlanScreen(
                            plan = state.plan,
                            activeTab = state.activeTab,
                            urgencyLevel = "Immediate Crisis",
                            country = state.country,
                            region = state.region,
                            language = language,
                            isOfflineFallback = state.isOfflineFallback,
                            isPlayingTts = isPlayingTts,
                            onTabSelected = { viewModel.selectTab(it) },
                            onToggleChecklist = { viewModel.toggleChecklistItem(it) },
                            onSaveCase = { title, notes ->
                                viewModel.saveCurrentPlanAsCase(title, notes)
                                viewModel.navigateTo(NavigationDest.ACTIVE_CASES)
                            },
                            onSpeakScript = { script -> viewModel.speakAdvocacyScript(script) },
                            onStopSpeech = { viewModel.stopSpeech() },
                            onTriggerHaptic = { viewModel.performHapticFeedback() },
                            onNewIntake = {
                                viewModel.resetToIntake()
                                viewModel.navigateTo(NavigationDest.HOME)
                            }
                        )
                    }
                    is CivicSyncUiState.Error -> {
                        ErrorRecoveryScreen(
                            errorState = state,
                            language = language,
                            onRetry = { viewModel.retryGeneration() },
                            onUseOfflineFallback = { viewModel.useOfflineContingencyPlan() },
                            onBackToIntake = { viewModel.resetToIntake() }
                        )
                    }
                }
            }
        }
        NavigationDest.ACTIVE_CASES -> {
            ActiveCasesScreen(
                cases = savedCases,
                language = language,
                onViewCase = { caseEntity -> viewModel.viewSavedCase(caseEntity) },
                onUpdateStatus = { caseId, status -> viewModel.updateCaseStatus(caseId, status) },
                onDeleteCase = { caseId -> viewModel.deleteCase(caseId) },
                onNewCase = {
                    viewModel.startNewCase()
                }
            )
        }
        NavigationDest.DOCUMENT_VAULT -> {
            DocumentVaultScreen(
                documents = vaultDocs,
                language = language,
                onAddDocument = { title, type, uri, desc ->
                    viewModel.addVaultDocument(title, type, uri, desc)
                },
                onDeleteDocument = { id -> viewModel.deleteVaultDocument(id) }
            )
        }
        NavigationDest.OFFLINE_GUIDES -> {
            OfflineChecklistScreen(
                checklists = offlineChecklists,
                language = language,
                onToggleChecklist = { id, completed ->
                    viewModel.toggleOfflineChecklist(id, completed)
                }
            )
        }
        NavigationDest.RESOURCES -> {
            ResourcesScreen(
                resources = viewModel.resources,
                language = language
            )
        }
        NavigationDest.SETTINGS -> {
            SettingsScreen(
                onOpenPrivacyPolicy = { viewModel.navigateTo(NavigationDest.PRIVACY_POLICY) },
                onClearAllData = { viewModel.deleteAccountAndAllData() },
                isUserLoggedIn = isUserLoggedIn,
                onSignInClick = { viewModel.navigateTo(NavigationDest.AUTH) },
                onSignOutClick = { viewModel.signOut() }
            )
        }
        NavigationDest.PRIVACY_POLICY -> {
            PrivacyPolicyScreen(
                onBack = { viewModel.navigateTo(NavigationDest.SETTINGS) },
                onDeleteAllData = { viewModel.deleteAccountAndAllData() }
            )
        }
    }
    }
}


/**
 * Resilient Error recovery view presenting user with localized error notice,
 * immediate retry action, and zero-connectivity offline contingency plan fallback.
 */
@Composable
fun ErrorRecoveryScreen(
    errorState: CivicSyncUiState.Error,
    language: AppLanguage,
    onRetry: () -> Unit,
    onUseOfflineFallback: () -> Unit,
    onBackToIntake: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Rose400.copy(alpha = 0.15f))
                .border(1.dp, Rose400.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = Rose400,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.error_general_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Slate100,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.error_general_message),
            fontSize = 16.sp,
            color = Slate400,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Retry Button
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("retry_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Slate900,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.retry_btn),
                color = Slate900,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Offline Contingency Plan Button
        OutlinedButton(
            onClick = onUseOfflineFallback,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber400),
            border = androidx.compose.foundation.BorderStroke(1.dp, Amber400.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("offline_contingency_button")
        ) {
            Icon(
                imageVector = Icons.Default.OfflinePin,
                contentDescription = null,
                tint = Amber400,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.offline_contingency_btn),
                color = Amber400,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackToIntake,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate400),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = stringResource(R.string.back_to_intake),
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Top App Bar with multilingual switch and brand identity.
 */
@Composable
fun MobileTopBar(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Emerald500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Slate900,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    color = Slate100,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.app_subtitle),
                    color = Emerald400,
                    fontSize = 12.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Language Switcher Badge Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Emerald400.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onToggleLanguage() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("language_toggle_button")
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.URDU -> "اردو"
                        AppLanguage.ENGLISH -> "EN"
                        AppLanguage.ARABIC -> "العربية"
                    },
                    color = Emerald400,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("mobile_top_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Slate400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Mobile Bottom Navigation Bar.
 */
@Composable
fun MobileBottomBar(
    currentNav: NavigationDest,
    language: AppLanguage,
    hasPendingCases: Boolean,
    vaultCount: Int,
    onSelectNav: (NavigationDest) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color(0xFF0B1329),
        contentColor = Slate300,
        tonalElevation = 8.dp
    ) {
        navigationItems.forEach { item ->
            val isSelected = currentNav == item.dest
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectNav(item.dest) },
                icon = {
                    if (item.dest == NavigationDest.ACTIVE_CASES && hasPendingCases) {
                        BadgedBox(badge = { Badge() }) {
                            Icon(
                                imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                                contentDescription = stringResource(item.titleResId),
                                tint = if (isSelected) Emerald400 else Slate400
                            )
                        }
                    } else if (item.dest == NavigationDest.DOCUMENT_VAULT && vaultCount > 0) {
                        BadgedBox(badge = { Badge { Text("$vaultCount") } }) {
                            Icon(
                                imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                                contentDescription = stringResource(item.titleResId),
                                tint = if (isSelected) Emerald400 else Slate400
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                            contentDescription = stringResource(item.titleResId),
                            tint = if (isSelected) Emerald400 else Slate400
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(item.titleResId),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Emerald400 else Slate400
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Emerald400,
                    unselectedIconColor = Slate400,
                    indicatorColor = Color(0xFF1E293B)
                )
            )
        }
    }
}

/**
 * Tablet / Wide-screen Side Rail Navigation.
 */
@Composable
fun Sidebar(
    currentNav: NavigationDest,
    language: AppLanguage,
    hasPendingCases: Boolean,
    vaultCount: Int,
    onSelectNav: (NavigationDest) -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF090D1A))
            .border(width = 1.dp, color = Slate800)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Emerald500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Slate900,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    color = Slate100,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.app_subtitle),
                    color = Emerald400,
                    fontSize = 12.sp
                )
            }
        }

        navigationItems.forEach { item ->
            val isSelected = currentNav == item.dest
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Emerald400.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onSelectNav(item.dest) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                    contentDescription = stringResource(item.titleResId),
                    tint = if (isSelected) Emerald400 else Slate400,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(item.titleResId),
                    color = if (isSelected) Emerald400 else Slate300,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Language toggle pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .clickable { onToggleLanguage() }
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Language: ${language.labelNative}",
                color = Emerald400,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
