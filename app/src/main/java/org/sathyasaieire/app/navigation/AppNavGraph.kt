package org.sathyasaieire.app.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.sathyasaieire.app.feature.auth.presentation.AuthState
import org.sathyasaieire.app.feature.auth.presentation.GdprConsentScreen
import org.sathyasaieire.app.feature.auth.presentation.SignInScreen
import org.sathyasaieire.app.feature.home.presentation.HomeScreen

@Composable
fun AppNavGraph(
    authState: AuthState,
    onSignIn: (Context) -> Unit,
    onSignOut: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
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

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Route.SignIn.path) {
            SignInScreen(
                authState = authState,
                onSignIn = onSignIn,
            )
        }
        composable(Route.GdprConsent.path) {
            GdprConsentScreen(
                onConsentGiven = { navController.navigate(Route.Home.path) {
                    popUpTo(0) { inclusive = true }
                }},
            )
        }
        composable(Route.Home.path) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) },
                onSignOut = onSignOut,
            )
        }
        // Remaining routes wired in subsequent features
    }
}
