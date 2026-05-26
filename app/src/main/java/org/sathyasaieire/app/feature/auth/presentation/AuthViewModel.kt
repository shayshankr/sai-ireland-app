package org.sathyasaieire.app.feature.auth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sathyasaieire.app.domain.model.User
import org.sathyasaieire.app.feature.auth.data.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            if (!authRepository.isSignedIn) {
                _state.value = AuthState.SignedOut
                return@launch
            }
            val user = authRepository.getCurrentUser()
            _state.value = when {
                user == null -> AuthState.SignedOut
                !user.gdprConsent -> AuthState.NeedsConsent(user)
                else -> AuthState.SignedIn(user)
            }
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.signInWithGoogle(activityContext).fold(
                onSuccess = { user ->
                    _state.value = if (!user.gdprConsent) {
                        AuthState.NeedsConsent(user)
                    } else {
                        AuthState.SignedIn(user)
                    }
                },
                onFailure = { e ->
                    _state.value = if (e.message == "Sign-in cancelled") {
                        AuthState.SignedOut
                    } else {
                        AuthState.Error(e.message ?: "Sign-in failed")
                    }
                },
            )
        }
    }

    fun grantConsent(analyticsEnabled: Boolean) {
        viewModelScope.launch {
            authRepository.updateConsent(gdprConsent = true, analyticsEnabled = analyticsEnabled)
            // Enable analytics/crashlytics if consented
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(analyticsEnabled)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = analyticsEnabled
            val user = authRepository.getCurrentUser()
            _state.value = if (user != null) AuthState.SignedIn(user) else AuthState.SignedOut
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _state.value = AuthState.SignedOut
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            authRepository.deleteAccount().fold(
                onSuccess = { _state.value = AuthState.SignedOut },
                onFailure = { e -> _state.value = AuthState.Error(e.message ?: "Delete failed") },
            )
        }
    }
}

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class NeedsConsent(val user: User) : AuthState
    data class SignedIn(val user: User) : AuthState
    data class Error(val message: String) : AuthState
}
