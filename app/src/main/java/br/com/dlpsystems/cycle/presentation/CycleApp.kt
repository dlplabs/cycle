package br.com.dlpsystems.cycle.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.dlpsystems.cycle.presentation.auth.LoginScreen
import br.com.dlpsystems.cycle.presentation.auth.RegisterScreen
import br.com.dlpsystems.cycle.presentation.auth.SessionViewModel
import br.com.dlpsystems.cycle.presentation.components.HealthDisclaimerDialog
import br.com.dlpsystems.cycle.presentation.dashboard.DashboardScreen
import br.com.dlpsystems.cycle.presentation.planner.FutureEventPlannerScreen
import br.com.dlpsystems.cycle.presentation.settings.SettingsScreen
import br.com.dlpsystems.cycle.presentation.sos.SosReliefScreen
import br.com.dlpsystems.cycle.presentation.subscription.PaywallScreen

@Composable
fun CycleApp(
    startPeriodOnOpen: Boolean = false,
    onStartPeriodConsumed: () -> Unit = {},
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val session by sessionViewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    var sawInitialAuth by remember { mutableStateOf(false) }

    LaunchedEffect(session.loading, session.userId) {
        if (session.loading) return@LaunchedEffect
        if (session.userId != null) {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        } else if (sawInitialAuth) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
        sawInitialAuth = true
    }

    if (session.userId != null && !session.disclaimerAccepted) {
        HealthDisclaimerDialog(onAccept = sessionViewModel::acceptDisclaimer)
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onRegister = { navController.navigate("register") })
        }
        composable("register") {
            RegisterScreen(onBack = { navController.popBackStack() })
        }
        composable("dashboard") {
            DashboardScreen(
                onOpenSettings = { navController.navigate("settings") },
                onOpenSos = { navController.navigate("sos") },
                onOpenPlanner = { navController.navigate("planner") },
                startPeriodOnOpen = startPeriodOnOpen,
                onStartPeriodConsumed = onStartPeriodConsumed,
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onPaywall = { navController.navigate("paywall") },
            )
        }
        composable("sos") {
            SosReliefScreen(onBack = { navController.popBackStack() })
        }
        composable("planner") {
            FutureEventPlannerScreen(
                onBack = { navController.popBackStack() },
                onPaywall = { navController.navigate("paywall") },
            )
        }
        composable("paywall") {
            PaywallScreen(onBack = { navController.popBackStack() })
        }
    }
}
