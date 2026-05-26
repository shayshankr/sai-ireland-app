package org.sathyasaieire.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.sathyasaieire.app.feature.auth.presentation.AuthState
import org.sathyasaieire.app.feature.auth.presentation.AuthViewModel
import org.sathyasaieire.app.navigation.AppNavGraph
import org.sathyasaieire.app.ui.theme.SaiIrelandTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.state.collectAsState()

            // Keep splash on screen while we resolve auth state
            splashScreen.setKeepOnScreenCondition { authState is AuthState.Loading }

            SaiIrelandTheme {
                AppNavGraph(
                    authState = authState,
                    onSignIn = { authViewModel.signInWithGoogle(this@MainActivity) },
                    onSignOut = { authViewModel.signOut() },
                )
            }
        }
    }
}
