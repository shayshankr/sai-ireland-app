package org.sathyasaieire.app.feature.contact.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sathyasaieire.app.feature.contact.data.ContactRepository
import javax.inject.Inject

val CONTACT_SUBJECTS = listOf(
    "General Inquiry",
    "Prayer Request",
    "Event Question",
    "Volunteer / Seva",
    "Technical Issue",
    "Other",
)

sealed interface ContactUiState {
    data object Form : ContactUiState
    data object Submitting : ContactUiState
    data class Success(val subject: String, val message: String) : ContactUiState
    data class Error(val message: String) : ContactUiState
}

data class ContactFormState(
    val subject: String = CONTACT_SUBJECTS.first(),
    val message: String = "",
)

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val repository: ContactRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactUiState>(ContactUiState.Form)
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    private val _form = MutableStateFlow(ContactFormState())
    val form: StateFlow<ContactFormState> = _form.asStateFlow()

    fun onSubjectChange(v: String) = _form.update { it.copy(subject = v) }
    fun onMessageChange(v: String) = _form.update { it.copy(message = v) }

    fun send() {
        val f = _form.value
        if (f.message.isBlank()) return
        viewModelScope.launch {
            _uiState.value = ContactUiState.Submitting
            repository.sendMessage(f.subject, f.message.trim()).fold(
                onSuccess = { _uiState.value = ContactUiState.Success(f.subject, f.message.trim()) },
                onFailure = { e -> _uiState.value = ContactUiState.Error(e.message ?: "Failed to send") },
            )
        }
    }

    fun reset() {
        _form.value = ContactFormState()
        _uiState.value = ContactUiState.Form
    }
}
