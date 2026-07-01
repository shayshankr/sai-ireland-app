package org.sathyasaieire.app.feature.contact.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sathyasaieire.app.domain.model.ContactMessage
import org.sathyasaieire.app.feature.contact.data.ContactRepository
import javax.inject.Inject

@HiltViewModel
class AdminInboxViewModel @Inject constructor(
    private val repository: ContactRepository,
) : ViewModel() {

    val messages: StateFlow<List<ContactMessage>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun toggleRead(message: ContactMessage) {
        viewModelScope.launch {
            runCatching { repository.markRead(message.id, !message.isRead) }
                .onFailure { _error.value = it.message }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
}
