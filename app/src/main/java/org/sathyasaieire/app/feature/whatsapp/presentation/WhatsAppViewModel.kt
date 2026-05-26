package org.sathyasaieire.app.feature.whatsapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sathyasaieire.app.domain.model.JoinRequest
import org.sathyasaieire.app.feature.whatsapp.data.JoinRequestRepository
import javax.inject.Inject

sealed interface WhatsAppUiState {
    data object Loading : WhatsAppUiState
    data object Form : WhatsAppUiState
    data class Status(val request: JoinRequest) : WhatsAppUiState
    data class Error(val message: String) : WhatsAppUiState
}

data class WhatsAppFormState(
    val name: String = "",
    val phone: String = "",
    val county: String = "",
    val message: String = "",
    val isSubmitting: Boolean = false,
)

@HiltViewModel
class WhatsAppViewModel @Inject constructor(
    private val repository: JoinRequestRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WhatsAppUiState>(WhatsAppUiState.Loading)
    val uiState: StateFlow<WhatsAppUiState> = _uiState.asStateFlow()

    private val _form = MutableStateFlow(WhatsAppFormState())
    val form: StateFlow<WhatsAppFormState> = _form.asStateFlow()

    init { loadRequest() }

    private fun loadRequest() {
        viewModelScope.launch {
            _uiState.value = WhatsAppUiState.Loading
            repository.getMyRequest().fold(
                onSuccess = { request ->
                    _uiState.value = if (request == null) WhatsAppUiState.Form
                    else WhatsAppUiState.Status(request)
                },
                onFailure = { _uiState.value = WhatsAppUiState.Error(it.message ?: "Failed to load") },
            )
        }
    }

    fun onNameChange(v: String) = _form.update { it.copy(name = v) }
    fun onPhoneChange(v: String) = _form.update { it.copy(phone = v) }
    fun onCountyChange(v: String) = _form.update { it.copy(county = v) }
    fun onMessageChange(v: String) = _form.update { it.copy(message = v) }

    fun submit() {
        val f = _form.value
        if (f.name.isBlank() || f.phone.isBlank()) return
        viewModelScope.launch {
            _form.update { it.copy(isSubmitting = true) }
            repository.submit(
                name = f.name.trim(),
                phone = f.phone.trim(),
                county = f.county.trim(),
                message = f.message.trim(),
            ).fold(
                onSuccess = { loadRequest() },
                onFailure = { e ->
                    _form.update { it.copy(isSubmitting = false) }
                    _uiState.value = WhatsAppUiState.Error(e.message ?: "Failed to submit")
                },
            )
        }
    }
}
