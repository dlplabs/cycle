package br.com.dlpsystems.cycle.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.CycleIcons
import br.com.dlpsystems.cycle.presentation.auth.LoginScreen
import br.com.dlpsystems.cycle.presentation.auth.RegisterScreen
import br.com.dlpsystems.cycle.presentation.auth.SessionViewModel
import br.com.dlpsystems.cycle.presentation.components.HealthDisclaimerDialog
import br.com.dlpsystems.cycle.presentation.dashboard.DashboardScreen
import br.com.dlpsystems.cycle.presentation.planner.FutureEventPlannerScreen
import br.com.dlpsystems.cycle.presentation.settings.SettingsScreen
import br.com.dlpsystems.cycle.presentation.sos.SosReliefScreen
import br.com.dlpsystems.cycle.presentation.subscription.PaywallScreen

private val mainTabs = listOf(
    MainTab("dashboard", R.string.nav_today, CycleIcons.AppMark),
    MainTab("sos", R.string.nav_relief, CycleIcons.Menstrual),
    MainTab("planner", R.string.nav_plan, CycleIcons.Follicular),
    MainTab("settings", R.string.nav_account, CycleIcons.Luteal),
)

private data class MainTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

@Composable
fun CycleApp(
    startPeriodOnOpen: Boolean = false,
    onStartPeriodConsumed: () -> Unit = {},
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val session by sessionViewModel.state.collectAsStateWithLifecycle()

    if (session.userId != null && !session.disclaimerAccepted) {
        HealthDisclaimerDialog(onAccept = sessionViewModel::acceptDisclaimer)
    }

    when {
        session.loading -> Box(Modifier.fillMaxSize())
        session.userId == null -> AuthGraph()
        else -> MainGraph(
            startPeriodOnOpen = startPeriodOnOpen,
            onStartPeriodConsumed = onStartPeriodConsumed,
        )
    }
}

@Composable
private fun AuthGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onRegister = { navController.navigate("register") })
        }
        composable("register") {
            RegisterScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainGraph(
    startPeriodOnOpen: Boolean,
    onStartPeriodConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val current = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBar = current in mainTabs.map { it.route }
    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    mainTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.route,
                            onClick = { navController.openTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding),
        ) {
            composable("dashboard") {
                DashboardScreen(
                    startPeriodOnOpen = startPeriodOnOpen,
                    onStartPeriodConsumed = onStartPeriodConsumed,
                )
            }
            composable("sos") {
                SosReliefScreen(onBack = {})
            }
            composable("planner") {
                FutureEventPlannerScreen(onPaywall = { navController.navigate("paywall") })
            }
            composable("settings") {
                SettingsScreen(onPaywall = { navController.navigate("paywall") })
            }
            composable("paywall") {
                PaywallScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.openTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
