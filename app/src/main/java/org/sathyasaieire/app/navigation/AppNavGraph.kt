package org.sathyasaieire.app.navigation

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.sathyasaieire.app.feature.auth.presentation.AuthState
import org.sathyasaieire.app.feature.auth.presentation.GdprConsentScreen
import org.sathyasaieire.app.feature.auth.presentation.SignInScreen
import org.sathyasaieire.app.feature.events.presentation.EventDetailScreen
import org.sathyasaieire.app.feature.events.presentation.EventListScreen
import org.sathyasaieire.app.feature.home.presentation.HomeScreen
import org.sathyasaieire.app.ui.components.SaiBottomBar
import org.sathyasaieire.app.ui.components.bottomBarRoutes

@Composable
fun AppNavGraph(
    authState: AuthState,
    onSignIn: (Context) -> Unit,
    onSignOut: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    val startDestination = when (authState) {
        is AuthState.Loading -> Route.SignIn.path
        is AuthState.NeedsConsent -> Route.GdprConsent.path
        is AuthState.SignedIn -> Route.Home.path
        is AuthState.SignedOut, is AuthState.Error -> Route.SignIn.path
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignedIn -> navController.navigate(Route.Home.path) {
                popUpTo(0) { inclusive = true }
            }
            is AuthState.NeedsConsent -> navController.navigate(Route.GdprConsent.path) {
                popUpTo(Route.SignIn.path) { inclusive = true }
            }
            is AuthState.SignedOut -> navController.navigate(Route.SignIn.path) {
                popUpTo(0) { inclusive = true }
            }
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                SaiBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Route.Home.path) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            // ── Auth ─────────────────────────────────────────────────────────
            composable(Route.SignIn.path) {
                SignInScreen(authState = authState, onSignIn = onSignIn)
            }
            composable(Route.GdprConsent.path) {
                GdprConsentScreen(
                    onConsentGiven = {
                        navController.navigate(Route.Home.path) { popUpTo(0) { inclusive = true } }
                    },
                )
            }

            // ── Main tabs (show bottom bar) ──────────────────────────────────
            composable(Route.Home.path) {
                HomeScreen(
                    onNavigate = { navController.navigate(it) },
                    onSignOut = onSignOut,
                    contentPadding = padding,
                )
            }
            composable(Route.Events.path) {
                EventListScreen(
                    onEventClick = { id -> navController.navigate(Route.EventDetail.createRoute(id)) },
                    contentPadding = padding,
                )
            }
            composable(Route.Polls.path) {
                StubScreen(label = "Polls", emoji = "🗳️", contentPadding = padding)
            }
            composable(Route.News.path) {
                StubScreen(label = "News", emoji = "📰", contentPadding = padding)
            }
            composable(Route.More.path) {
                StubScreen(label = "More", emoji = "⋯", contentPadding = padding)
            }

            // ── Detail screens (no bottom bar) ───────────────────────────────
            composable(
                route = Route.EventDetail.path,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) {
                EventDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
