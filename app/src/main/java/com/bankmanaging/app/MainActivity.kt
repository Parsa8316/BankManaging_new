package com.bankmanaging.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bankmanaging.app.ui.screens.DashboardScreen
import com.bankmanaging.app.ui.screens.FilterScreen
import com.bankmanaging.app.ui.screens.PatternEditorScreen
import com.bankmanaging.app.ui.screens.SettingsScreen
import com.bankmanaging.app.ui.theme.BankManagingTheme
import com.bankmanaging.app.ui.util.Formatters
import com.bankmanaging.app.viewmodel.BankViewModel
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

@Serializable
data object FilterRoute

@Serializable
data object SettingsRoute

@Serializable
data class PatternEditorRoute(val patternId: String? = null)

private data class TopLevelDestination(
    val route: Any,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BankManagingApp()
        }
    }
}

@Composable
fun BankManagingApp(
    viewModel: BankViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeSnackbar()
        }
    }

    val destinations = remember {
        listOf(
            TopLevelDestination(
                route = DashboardRoute,
                labelRes = R.string.nav_transactions,
                selectedIcon = Icons.Rounded.ReceiptLong,
                unselectedIcon = Icons.Outlined.ReceiptLong,
                tag = "nav_dashboard"
            ),
            TopLevelDestination(
                route = FilterRoute,
                labelRes = R.string.nav_filter,
                selectedIcon = Icons.Rounded.FilterList,
                unselectedIcon = Icons.Outlined.FilterList,
                tag = "nav_filter"
            ),
            TopLevelDestination(
                route = SettingsRoute,
                labelRes = R.string.nav_settings,
                selectedIcon = Icons.Rounded.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                tag = "nav_settings"
            )
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isPatternEditor = currentDestination?.hasRoute<PatternEditorRoute>() == true

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BankManagingTheme(themeMode = uiState.themeMode) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        if (!useNavigationRail && !isPatternEditor) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                destinations.forEach { dest ->
                                    val selected = when (dest.route) {
                                        is DashboardRoute -> currentDestination?.hasRoute<DashboardRoute>() == true
                                        is FilterRoute -> currentDestination?.hasRoute<FilterRoute>() == true
                                        is SettingsRoute -> currentDestination?.hasRoute<SettingsRoute>() == true
                                        else -> false
                                    }
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTopLevel(dest.route) },
                                        icon = {
                                            DestinationIcon(
                                                dest = dest,
                                                selected = selected,
                                                activeFilterCount = uiState.filter.activeCount,
                                                persianDigits = uiState.usePersianDigits
                                            )
                                        },
                                        label = { Text(stringResource(dest.labelRes)) },
                                        modifier = Modifier.testTag(dest.tag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavigationRail && !isPatternEditor) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                destinations.forEach { dest ->
                                    val selected = when (dest.route) {
                                        is DashboardRoute -> currentDestination?.hasRoute<DashboardRoute>() == true
                                        is FilterRoute -> currentDestination?.hasRoute<FilterRoute>() == true
                                        is SettingsRoute -> currentDestination?.hasRoute<SettingsRoute>() == true
                                        else -> false
                                    }
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateTopLevel(dest.route) },
                                        icon = {
                                            DestinationIcon(
                                                dest = dest,
                                                selected = selected,
                                                activeFilterCount = uiState.filter.activeCount,
                                                persianDigits = uiState.usePersianDigits
                                            )
                                        },
                                        label = { Text(stringResource(dest.labelRes)) },
                                        modifier = Modifier.testTag(dest.tag)
                                    )
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = DashboardRoute,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable<DashboardRoute> {
                                DashboardScreen(
                                    uiState = uiState,
                                    onRequestSmsPermission = {
                                        smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                                    },
                                    onRefresh = { viewModel.refreshTransactions() },
                                    onSearchChange = { viewModel.updateSearchQuery(it) },
                                    onClearFilters = { viewModel.resetFilter() },
                                    onOpenFilter = { navigateTopLevel(FilterRoute) },
                                    onOpenSettings = { navigateTopLevel(SettingsRoute) },
                                    onAnalyzeSingleSms = { sms, sender ->
                                        viewModel.analyzeMessageWithActivePatterns(sms, sender)
                                    },
                                    onShowMessage = { viewModel.showSnackbar(it) }
                                )
                            }

                            composable<FilterRoute> {
                                FilterScreen(
                                    uiState = uiState,
                                    onApplyFilter = { newFilter ->
                                        viewModel.updateFilter(newFilter)
                                        navigateTopLevel(DashboardRoute)
                                    },
                                    onResetFilter = { viewModel.resetFilter() },
                                    onShowError = { viewModel.showSnackbar(it) }
                                )
                            }

                            composable<SettingsRoute> {
                                SettingsScreen(
                                    uiState = uiState,
                                    onAddSender = { viewModel.addBankSender(it) },
                                    onDeleteSender = { viewModel.removeBankSender(it) },
                                    onTogglePattern = { id, enabled -> viewModel.togglePatternEnabled(id, enabled) },
                                    onAddNewPattern = {
                                        navController.navigate(PatternEditorRoute(patternId = null))
                                    },
                                    onEditPattern = { id ->
                                        navController.navigate(PatternEditorRoute(patternId = id))
                                    },
                                    onDuplicatePattern = { pattern ->
                                        val copy = viewModel.duplicatePattern(pattern)
                                        navController.navigate(PatternEditorRoute(patternId = copy.id))
                                    },
                                    onDeletePattern = { viewModel.deletePattern(it) },
                                    onResetDefaultPatterns = { viewModel.resetDefaultPatterns() },
                                    onThemeModeChange = { viewModel.setThemeMode(it) },
                                    onPersianDigitsChange = { viewModel.setUsePersianDigits(it) }
                                )
                            }

                            composable<PatternEditorRoute> { entry ->
                                val route = entry.toRoute<PatternEditorRoute>()
                                val existing = remember(route.patternId, uiState.smsPatterns) {
                                    viewModel.getPatternById(route.patternId)
                                }
                                PatternEditorScreen(
                                    existingPattern = existing,
                                    persianDigits = uiState.usePersianDigits,
                                    onTestPattern = { pattern, sample ->
                                        viewModel.testPattern(pattern, sample)
                                    },
                                    onSave = { pattern ->
                                        viewModel.saveSmsPattern(pattern)
                                        navController.popBackStack()
                                    },
                                    onBack = { navController.popBackStack() }
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
private fun DestinationIcon(
    dest: TopLevelDestination,
    selected: Boolean,
    activeFilterCount: Int,
    persianDigits: Boolean
) {
    val iconVector = if (selected) dest.selectedIcon else dest.unselectedIcon
    val label = stringResource(dest.labelRes)
    if (dest.route is FilterRoute && activeFilterCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(Formatters.formatDigits(activeFilterCount.toString(), persianDigits))
                }
            }
        ) {
            Icon(imageVector = iconVector, contentDescription = label)
        }
    } else {
        Icon(imageVector = iconVector, contentDescription = label)
    }
}
