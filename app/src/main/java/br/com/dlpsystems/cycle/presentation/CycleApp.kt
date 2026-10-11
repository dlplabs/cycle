package br.com.dlpsystems.cycle.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
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
        session.userId == null && !session.welcomeSeen -> {
            var showAuthDestination by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
            if (showAuthDestination != null) {
                AuthSubGraph(
                    startDestination = showAuthDestination ?: "login",
                    onBackToWelcome = { showAuthDestination = null },
                    onAuthenticated = {
                        sessionViewModel.completeWelcome()
                        showAuthDestination = null
                    },
                )
            } else {
                br.com.dlpsystems.cycle.presentation.auth.WelcomeScreen(
                    onExploreApp = { sessionViewModel.completeWelcome() },
                    onLogin = { showAuthDestination = "login" },
                    onCreateAccount = { showAuthDestination = "register" },
                )
            }
        }
        else -> MainGraph(
            startPeriodOnOpen = startPeriodOnOpen,
            onStartPeriodConsumed = onStartPeriodConsumed,
        )
    }
}

@Composable
private fun AuthSubGraph(
    startDestination: String,
    onBackToWelcome: () -> Unit,
    onAuthenticated: () -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onRegister = { navController.navigate("register") },
            )
        }
        composable("register") {
            RegisterScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        onBackToWelcome()
                    }
                },
            )
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
    var pendingActionAfterAuth by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<(() -> Unit)?>(null) }
    var showAuthModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = OffWhiteBackground) {
                    mainTabs.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.openTab(tab.route) },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) HeaderSage else Color.Transparent),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(26.dp),
                                        tint = DeepPlum,
                                    )
                                }
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = DeepPlum,
                                selectedTextColor = DeepPlum,
                                unselectedIconColor = DeepPlum,
                                unselectedTextColor = DeepPlum.copy(alpha = 0.7f),
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable("dashboard") {
                DashboardScreen(
                    startPeriodOnOpen = startPeriodOnOpen,
                    onStartPeriodConsumed = onStartPeriodConsumed,
                    onAccount = { navController.navigate("settings") },
                    onRequireAuth = { action ->
                        pendingActionAfterAuth = action
                        navController.navigate("login")
                    },
                )
            }
            composable("sos") {
                SosReliefScreen(onAccount = { navController.navigate("settings") })
            }
            composable("planner") {
                FutureEventPlannerScreen(
                    onPaywall = { navController.navigate("paywall") },
                    onAccount = { navController.navigate("settings") },
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onPaywall = { navController.navigate("paywall") },
                    onNavigatePregnancy = { navController.navigate("pregnancy") },
                    onNavigateLunar = { navController.navigate("lunar") },
                    onNavigatePill = { navController.navigate("pill") },
                    onNavigateExams = { navController.navigate("exams") },
                    onNavigatePartner = { navController.navigate("partner") },
                    onNavigateBodySymptoms = { navController.navigate("body_symptoms") },
                    onLogin = { navController.navigate("login") },
                )
            }
            composable("login") {
                LoginScreen(
                    onRegister = { navController.navigate("register") },
                )
            }
            composable("register") {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("pregnancy") {
                br.com.dlpsystems.cycle.presentation.pregnancy.PregnancyScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("lunar") {
                br.com.dlpsystems.cycle.presentation.lunar.LunarScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("pill") {
                br.com.dlpsystems.cycle.presentation.pill.PillScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("exams") {
                br.com.dlpsystems.cycle.presentation.exams.ExamsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("partner") {
                br.com.dlpsystems.cycle.presentation.partner.PartnerScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("body_symptoms") {
                br.com.dlpsystems.cycle.presentation.symptoms.BodySymptomsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("paywall") {
                PaywallScreen(onBack = { navController.popBackStack() })
            }
        }
    }

    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(sessionState.userId) {
        if (sessionState.userId != null) {
            val pending = pendingActionAfterAuth
            pendingActionAfterAuth = null
            if (current == "login" || current == "register") {
                navController.popBackStack("dashboard", inclusive = false)
            }
            pending?.invoke()
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
