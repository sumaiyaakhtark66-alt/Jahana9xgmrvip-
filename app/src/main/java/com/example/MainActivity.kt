package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Castle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.VipGameId
import com.example.ui.components.RoyalTopHeader
import com.example.ui.components.VipAmbientBackground
import com.example.ui.screens.ArenaScreen
import com.example.ui.screens.LoungeScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.screens.VipPassScreen
import com.example.ui.screens.games.CrownReflexGameScreen
import com.example.ui.screens.games.RunicMatrixGameScreen
import com.example.ui.screens.games.VaultBreakerGameScreen
import com.example.ui.theme.JahanVipTheme
import com.example.ui.viewmodel.VipViewModel

object Routes {
    const val LOUNGE = "lounge"
    const val ARENA = "arena"
    const val VAULT = "vault"
    const val PASS = "pass"
    const val GAME_PATTERN = "game/{gameId}"
    fun gameRoute(gameId: VipGameId) = "game/${gameId.id}"
}

private data class VipNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val VipTabs = listOf(
    VipNavTab(Routes.LOUNGE, "Lounge", Icons.Filled.Castle, Icons.Outlined.Castle, "nav_lounge"),
    VipNavTab(Routes.ARENA, "Arena", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports, "nav_arena"),
    VipNavTab(Routes.VAULT, "Vault", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_vault"),
    VipNavTab(Routes.PASS, "VIP Pass", Icons.Filled.Badge, Icons.Outlined.Badge, "nav_pass")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            JahanVipTheme {
                JahanWorldApp()
            }
        }
    }
}

@Composable
fun JahanWorldApp(
    viewModel: VipViewModel = viewModel(factory = VipViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isInGame = currentDestination?.route?.startsWith("game/") == true

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeStatusMessage()
        }
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    VipAmbientBackground {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            actionColor = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                bottomBar = {
                    if (!useNavRail && !isInGame) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            tonalElevation = 8.dp
                        ) {
                            VipTabs.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    modifier = Modifier.testTag(tab.testTag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                    if (useNavRail && !isInGame) {
                        NavigationRail(
                            modifier = Modifier.fillMaxHeight(),
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        ) {
                            VipTabs.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    modifier = Modifier.testTag(tab.testTag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (!isInGame) {
                            RoyalTopHeader(
                                profile = uiState.profile,
                                modifier = Modifier.statusBarsPadding()
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            NavHost(
                                navController = navController,
                                startDestination = Routes.LOUNGE,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable(Routes.LOUNGE) {
                                    LoungeScreen(
                                        uiState = uiState,
                                        onNavigateToArena = { navigateToTab(Routes.ARENA) },
                                        onLaunchGame = { game ->
                                            navController.navigate(Routes.gameRoute(game))
                                        },
                                        onClaimDailyVault = { viewModel.claimDailyVault() },
                                        onToggleTournament = { id -> viewModel.toggleTournament(id) }
                                    )
                                }
                                composable(Routes.ARENA) {
                                    ArenaScreen(
                                        uiState = uiState,
                                        onLaunchGame = { game ->
                                            navController.navigate(Routes.gameRoute(game))
                                        }
                                    )
                                }
                                composable(Routes.VAULT) {
                                    VaultScreen(
                                        uiState = uiState,
                                        onUnlockOrEquipPerk = { perkId ->
                                            viewModel.unlockOrEquipPerk(perkId)
                                        }
                                    )
                                }
                                composable(Routes.PASS) {
                                    VipPassScreen(
                                        uiState = uiState,
                                        onUpdateProfile = { tag, avatarIdx ->
                                            viewModel.updateGamerProfile(tag, avatarIdx)
                                        },
                                        onClearMatchHistory = { viewModel.clearMatchHistory() }
                                    )
                                }
                                composable(
                                    route = Routes.GAME_PATTERN,
                                    arguments = listOf(navArgument("gameId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val gameIdStr = backStackEntry.arguments?.getString("gameId") ?: VipGameId.CROWN_REFLEX.id
                                    val gameId = VipGameId.fromId(gameIdStr)
                                    val prevBest = uiState.bestScores[gameId.id] ?: 0
                                    when (gameId) {
                                        VipGameId.CROWN_REFLEX -> CrownReflexGameScreen(
                                            previousBest = prevBest,
                                            onMatchFinished = { score, crowns, xp ->
                                                viewModel.recordMatchResult(gameId, score, crowns, xp)
                                            },
                                            onBack = { navController.popBackStack() }
                                        )
                                        VipGameId.VAULT_BREAKER -> VaultBreakerGameScreen(
                                            previousBest = prevBest,
                                            onMatchFinished = { score, crowns, xp ->
                                                viewModel.recordMatchResult(gameId, score, crowns, xp)
                                            },
                                            onBack = { navController.popBackStack() }
                                        )
                                        VipGameId.RUNIC_MATRIX -> RunicMatrixGameScreen(
                                            previousBest = prevBest,
                                            onMatchFinished = { score, crowns, xp ->
                                                viewModel.recordMatchResult(gameId, score, crowns, xp)
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
    }
}
