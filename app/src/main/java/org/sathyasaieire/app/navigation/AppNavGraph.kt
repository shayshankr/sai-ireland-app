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
import org.sathyasaieire.app.domain.model.UserRole
import org.sathyasaieire.app.feature.auth.presentation.AuthState
import org.sathyasaieire.app.feature.auth.presentation.GdprConsentScreen
import org.sathyasaieire.app.feature.auth.presentation.SignInScreen
import org.sathyasaieire.app.feature.events.presentation.AdminEventScreen
import org.sathyasaieire.app.feature.events.presentation.EventDetailScreen
import org.sathyasaieire.app.feature.events.presentation.EventListScreen
import org.sathyasaieire.app.feature.bhajans.presentation.BhajanDetailScreen
import org.sathyasaieire.app.feature.bhajans.presentation.BhajanListScreen
import org.sathyasaieire.app.feature.contact.presentation.ContactScreen
import org.sathyasaieire.app.feature.home.presentation.HomeScreen
import org.sathyasaieire.app.feature.more.presentation.MoreScreen
import org.sathyasaieire.app.feature.profile.presentation.ProfileScreen
import org.sathyasaieire.app.feature.polls.presentation.AdminPollScreen
import org.sathyasaieire.app.feature.settings.presentation.SettingsScreen
import org.sathyasaieire.app.feature.polls.presentation.PollDetailScreen
import org.sathyasaieire.app.feature.polls.presentation.PollsListScreen
import org.sathyasaieire.app.feature.whatsapp.presentation.WhatsAppJoinScreen
import org.sathyasaieire.app.ui.components.SaiBottomBar
import org.sathyasaieire.app.ui.components.StubScreen
import org.sathyasaieire.app.ui.components.bottomBarRoutes

@Composable
fun AppNavGraph(
    authState: AuthState,
    onSignIn: (Context) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    val isAdmin = (authState as? AuthState.SignedIn)?.user?.role
        ?.let { it == UserRole.ADMIN || it == UserRole.SUPER_ADMIN } ?: false

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
                    user = (authState as? AuthState.SignedIn)?.user,
                    contentPadding = padding,
                )
            }
            composable(Route.Events.path) {
                EventListScreen(
                    onEventClick = { id -> navController.navigate(Route.EventDetail.createRoute(id)) },
                    onCreateEvent = { navController.navigate(Route.AdminEventCreate.path) },
                    isAdmin = isAdmin,
                    contentPadding = padding,
                )
            }
            composable(Route.Polls.path) {
                PollsListScreen(
                    onPollClick = { id -> navController.navigate(Route.PollDetail.createRoute(id)) },
                    onCreatePoll = { navController.navigate(Route.AdminPollCreate.path) },
                    isAdmin = isAdmin,
                    contentPadding = padding,
                )
            }
            composable(Route.News.path) {
                StubScreen(label = "News", emoji = "📰", contentPadding = padding)
            }
            composable(Route.More.path) {
                MoreScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    contentPadding = padding,
                )
            }

            // ── Detail screens (no bottom bar) ───────────────────────────────
            composable(
                route = Route.EventDetail.path,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) {
                EventDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEditEvent = if (isAdmin) {
                        { navController.navigate(Route.AdminEventEdit.createRoute(it)) }
                    } else null,
                )
            }

            // ── Poll detail (no bottom bar) ──────────────────────────────────
            composable(
                route = Route.PollDetail.path,
                arguments = listOf(navArgument("pollId") { type = NavType.StringType }),
            ) {
                PollDetailScreen(
                    onBack = { navController.popBackStack() },
                    isAdmin = isAdmin,
                )
            }

            // ── WhatsApp join (no bottom bar) ────────────────────────────────
            composable(Route.WhatsApp.path) {
                WhatsAppJoinScreen(onBack = { navController.popBackStack() })
            }

            // ── Stub screens (no bottom bar) ─────────────────────────────────
            composable(Route.Contact.path) {
                ContactScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.Gallery.path) {
                StubScreen(label = "Gallery", emoji = "🖼")
            }
            composable(Route.Bhajans.path) {
                BhajanListScreen(
                    onBack = { navController.popBackStack() },
                    onBhajanClick = { id -> navController.navigate(Route.BhajanDetail.createRoute(id)) },
                )
            }
            composable(
                route = Route.BhajanDetail.path,
                arguments = listOf(navArgument("bhajanId") { type = NavType.StringType }),
            ) {
                BhajanDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.Timer.path) {
                StubScreen(label = "Meditation Timer", emoji = "🧘")
            }
            composable(Route.Seva.path) {
                StubScreen(label = "Seva Sign-up", emoji = "💚")
            }
            composable(Route.Profile.path) {
                ProfileScreen(
                    user = (authState as? AuthState.SignedIn)?.user,
                    onBack = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate(Route.Settings.path) },
                )
            }
            composable(Route.Settings.path) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignOut = onSignOut,
                    onDeleteAccount = onDeleteAccount,
                )
            }

            // ── Admin screens (no bottom bar) ────────────────────────────────
            composable(Route.AdminPollCreate.path) {
                AdminPollScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.AdminEventCreate.path) {
                AdminEventScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Route.AdminEventEdit.path,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) {
                AdminEventScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
